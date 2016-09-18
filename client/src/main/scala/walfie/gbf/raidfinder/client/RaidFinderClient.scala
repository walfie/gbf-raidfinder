package walfie.gbf.raidfinder.client

import com.thoughtworks.binding.Binding
import com.thoughtworks.binding.Binding._
import java.util.Date
import org.scalajs.dom
import org.scalajs.dom.raw.Storage
import scala.scalajs.js
import walfie.gbf.raidfinder.client.syntax.BufferOps
import walfie.gbf.raidfinder.client.util.HtmlHelpers
import walfie.gbf.raidfinder.client.util.time.{Clock, Duration}
import walfie.gbf.raidfinder.client.ViewModel._
import walfie.gbf.raidfinder.protocol._

import js.JSConverters._

trait RaidFinderClient {
  def state: RaidFinderClient.State

  def updateBosses(bossNames: Seq[BossName]): Unit
  def updateAllBosses(): Unit
  def resetBossList(): Unit

  def follow(bossName: BossName): Unit
  def unfollow(bossName: BossName): Unit
  def toggleFollow(bossName: BossName): Unit

  def subscribe(bossName: BossName): Unit
  def unsubscribe(bossName: BossName): Unit
  def toggleSubscribe(bossName: BossName): Unit

  def clear(bossName: BossName): Unit
  def move(bossName: BossName, displacement: Int): Unit

  def truncateColumns(maxColumnSize: Int): Unit
}

class WebSocketRaidFinderClient(
  websocket: WebSocketClient, storage: Storage, clock: Clock
) extends RaidFinderClient with WebSocketSubscriber {
  import RaidFinderClient._

  websocket.setSubscriber(Some(this))
  val isConnected: Var[Boolean] = Var(false)

  private var allBossesMap: Map[BossName, RaidBossColumn] = Map.empty

  // Load bosses from localStorage and refollow/resubscribe
  private val FollowedBossesStorageKey = "followedBosses"
  private val SubscribedBossesStorageKey = "subscribedBosses"
  val state = State(allBosses = Vars.empty, followedBosses = Vars.empty)

  fetchLocalStorageCsv(FollowedBossesStorageKey).foreach(follow)
  fetchLocalStorageCsv(SubscribedBossesStorageKey).foreach(subscribe)

  override def onWebSocketOpen(): Unit = {
    resetBossList()
    isConnected := true
  }

  override def onWebSocketReconnect(): Unit = {
    refollowBosses()
    isConnected := true
  }

  override def onWebSocketClose(): Unit = {
    isConnected := false
  }

  def updateBosses(bossNames: Seq[BossName]): Unit =
    websocket.send(RaidBossesRequest(bossNames))
  def updateAllBosses(): Unit =
    websocket.send(AllRaidBossesRequest())

  def resetBossList(): Unit = {
    val followed = state.followedBosses.get
    state.allBosses.get := followed
    allBossesMap = followed.map(column => column.raidBoss.get.name -> column).toMap

    updateAllBosses()
  }

  /** Get the column number associated with a raid boss */
  private def columnIndex(bossName: BossName): Option[Int] = {
    val index = state.followedBosses.get.indexWhere(_.raidBoss.get.name == bossName)
    if (index < 0) None else Some(index)
  }

  def follow(bossName: BossName): Unit = if (columnIndex(bossName).isEmpty) {
    websocket.send(FollowRequest(bossNames = List(bossName)))

    // If it's not a boss we know about, create an empty column for it
    val column: RaidBossColumn = allBossesMap.getOrElse(bossName, {
      val newColumn = RaidBossColumn.empty(bossName)
      allBossesMap = allBossesMap.updated(bossName, newColumn)
      newColumn
    })

    state.followedBosses.get += column
    updateLocalStorageFollowed()
  }

  def unfollow(bossName: BossName): Unit = {
    columnIndex(bossName).foreach { index =>
      allBossesMap.get(bossName).foreach { column =>
        // Only unfollow in the backend if we're not explicitly following
        // both the English+Japanese versions of the same boss
        if (column.raidBoss.get.translatedName.flatMap(columnIndex).isEmpty) {
          websocket.send(UnfollowRequest(bossNames = List(bossName)))
        }
        column.clear()
      }

      val followedBosses = state.followedBosses.get
      followedBosses.remove(index)
      updateLocalStorageFollowed()
    }
  }

  def toggleFollow(bossName: BossName): Unit = {
    if (columnIndex(bossName).isDefined) unfollow(bossName)
    else follow(bossName)
  }

  private def setSubscription(bossName: BossName, changeState: Boolean => Boolean): Unit = {
    HtmlHelpers.requestNotificationPermission { () =>
      allBossesMap.get(bossName).foreach { boss =>
        boss.isSubscribed := changeState(boss.isSubscribed.get)
      }
      updateLocalStorageSubscribed()
    }
  }

  def subscribe(bossName: BossName): Unit = setSubscription(bossName, _ => true)
  def unsubscribe(bossName: BossName): Unit = setSubscription(bossName, _ => false)
  def toggleSubscribe(bossName: BossName): Unit = setSubscription(bossName, !_)

  def clear(bossName: BossName): Unit = {
    allBossesMap.get(bossName).foreach(_.clear())
  }

  def move(bossName: BossName, displacement: Int): Unit = {
    val followedBosses = state.followedBosses.get
    columnIndex(bossName).foreach { index =>
      val destinationIndex = index + displacement

      if (destinationIndex >= 0 && destinationIndex < followedBosses.size) {
        val thisColumn = followedBosses(index)
        val thatColumn = followedBosses(destinationIndex)
        followedBosses.update(destinationIndex, thisColumn)
        followedBosses.update(index, thatColumn)
      }
    }

    updateLocalStorageFollowed()
  }

  def truncateColumns(maxColumnSize: Int): Unit = {
    allBossesMap.values.foreach { column =>
      val tweets = column.raidTweets.get
      if (tweets.length > maxColumnSize) {
        tweets := tweets.take(maxColumnSize)
      }
    }
  }

  override def onWebSocketMessage(message: Response): Unit = message match {
    case r: RaidBossesResponse =>
      handleRaidBossesResponse(r.raidBosses)

    case r: FollowStatusResponse =>
    // Ignore. Also TODO: Figure out why this doesn't come back consistently

    case tweet: RaidTweetResponse =>
      allBossesMap.get(tweet.bossName).foreach { column =>
        addRaidTweetToColumn(tweet, column)

        for {
          translatedName <- column.raidBoss.get.translatedName
          if columnIndex(translatedName).nonEmpty
          column <- allBossesMap.get(translatedName)
        } yield addRaidTweetToColumn(tweet, column)
      }

    case r: ErrorResponse =>
      dom.window.console.error(r.message) // TODO: Better error handling

    case r: KeepAliveResponse => // Ignore
  }

  private def addRaidTweetToColumn(tweet: RaidTweetResponse, column: RaidBossColumn): Unit = {
    val columnTweets = column.raidTweets.get
    val shouldInsert = columnTweets.headOption.forall { firstTweetInColumn =>
      tweet.createdAt.after(firstTweetInColumn.createdAt)
    }
    if (shouldInsert) tweet +=: columnTweets

    // Show desktop notification, if subscribed
    if (column.isSubscribed.get) {
      val image = column.raidBoss.get.image.map(_ + ":thumb")
      desktopNotification(tweet, image)
    }
  }

  private def desktopNotification(tweet: RaidTweetResponse, image: Option[String]) = {
    val body = Seq(
      s"@${tweet.screenName}: ${tweet.raidId}",
      tweet.text,
      "\n(Click to copy raid ID)"
    ).filter(_.nonEmpty).mkString("\n")

    val onClick = { event: dom.Event =>
      event.preventDefault()
      HtmlHelpers.copy(tweet.raidId)
      ()
    }

    HtmlHelpers.desktopNotification(
      title = tweet.bossName,
      body = body,
      icon = image.orUndefined,
      onClick = onClick,
      tag = tweet.bossName,
      closeOnClick = true
    )
  }

  private def handleRaidBossesResponse(raidBosses: Seq[RaidBoss]): Unit = {
    raidBosses.foreach { raidBoss =>
      val bossName = raidBoss.name

      allBossesMap.get(bossName) match {
        case None => // Add to our list of known bosses
          val newColumn = RaidBossColumn(
            raidBoss = Var(raidBoss),
            raidTweets = Vars.empty,
            isSubscribed = Var(false)
          )
          allBossesMap = allBossesMap.updated(bossName, newColumn)

          state.allBosses.get := allBossesMap.values.toArray.sortBy { column =>
            val boss = column.raidBoss.get
            (boss.level, boss.name)
          }

        case Some(column) => // Update existing raid boss data
          column.raidBoss := raidBoss
      }
    }
  }

  private def refollowBosses(): Unit = {
    val followedBosses = state.followedBosses.get.map(_.raidBoss.get.name)
    websocket.send(FollowRequest(bossNames = followedBosses))
    updateBosses(followedBosses)
  }

  private def updateLocalStorageFollowed(): Unit = {
    val followedBossNames = state.followedBosses.get.map(_.raidBoss.get.name)
    updateLocalStorageCsv(FollowedBossesStorageKey, followedBossNames)
  }

  private def updateLocalStorageSubscribed(): Unit = {
    val subscribedBossNames = state.allBosses.get.collect {
      case column if column.isSubscribed.get => column.raidBoss.get.name
    }
    updateLocalStorageCsv(SubscribedBossesStorageKey, subscribedBossNames)
  }

  private def updateLocalStorageCsv(key: String, values: Seq[String]): Unit = {
    if (values.isEmpty)
      storage.removeItem(key)
    else
      storage.setItem(key, values.mkString(","))
  }

  private def fetchLocalStorageCsv(key: String): Seq[String] = {
    Option(storage.getItem(key)).fold(Seq.empty[String])(_.split(","))
  }

}

object RaidFinderClient {
  case class RaidBossColumn(
    raidBoss:     Var[RaidBoss],
    raidTweets:   Vars[RaidTweetResponse],
    isSubscribed: Var[Boolean]
  ) { def clear(): Unit = raidTweets.get.clear() }

  object RaidBossColumn {
    def empty(bossName: BossName): RaidBossColumn = {
      val raidBoss = RaidBoss(name = bossName)
      RaidBossColumn(
        raidBoss = Var(raidBoss),
        raidTweets = Vars.empty,
        isSubscribed = Var(false)
      )
    }
  }

  case class State(
    allBosses:      Vars[RaidBossColumn],
    followedBosses: Vars[RaidBossColumn]
  ) {
    lazy val followedBossNames: Binding[Set[BossName]] = Binding {
      followedBosses.bind.map(_.raidBoss.get.name).toSet
    }
  }
}

