package walfie.gbf.raidfinder.client

import com.thoughtworks.binding.Binding
import com.thoughtworks.binding.Binding._
import java.util.Date
import org.scalajs.dom
import org.scalajs.dom.raw.Storage
import scala.scalajs.js
import walfie.gbf.raidfinder.BuildInfo
import walfie.gbf.raidfinder.client.audio._
import walfie.gbf.raidfinder.client.syntax.BufferOps
import walfie.gbf.raidfinder.client.util.HtmlHelpers
import walfie.gbf.raidfinder.client.util.time.{Clock, Duration}
import walfie.gbf.raidfinder.client.ViewModel._
import walfie.gbf.raidfinder.protocol._

import js.annotation.ScalaJSDefined
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

  def getNotificationSound(bossName: BossName): Option[NotificationSound]
  def setNotificationSound(
    bossName:            BossName,
    notificationSoundId: Option[NotificationSoundId]
  ): Unit

  def isConnected: Var[Boolean]
}

class WebSocketRaidFinderClient(
  websocket: WebSocketClient, storage: Storage, clock: Clock
) extends RaidFinderClient with WebSocketSubscriber {
  import RaidFinderClient._

  var isConnected: Var[Boolean] = Var(false)
  private var isStartingUp = true

  private var allBossesMap: Map[BossName, RaidBossColumn] = Map.empty

  // Load bosses from localStorage and refollow/resubscribe
  private val FollowedBossesStorageKey = "followedBosses"
  val state = State(allBosses = Vars.empty, followedBosses = Vars.empty)

  {
    resetBossList()
    fetchFollowedBossesLocalStorage(FollowedBossesStorageKey).foreach { boss =>
      follow(boss.name)
      if (boss.isSubscribed) { subscribe(boss.name) }
      if (boss.soundId.nonEmpty) { setNotificationSound(boss.name, boss.soundId.toOption) }
    }
    websocket.setSubscriber(Some(this))
  }

  override def onWebSocketOpen(): Unit = {
    isConnected := true

    // TODO: Maybe don't hardcode this
    js.timers.setTimeout(Duration.seconds(1).milliseconds) {
      isStartingUp = false
    }
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
    allBossesMap = followed.map(column => column.raidBoss.get.name -> column)(scala.collection.breakOut)

    updateAllBosses()
  }

  /** Get the column number associated with a raid boss */
  private def columnIndex(bossName: BossName): Option[Int] = {
    val index = state.followedBosses.get.indexWhere(_.raidBoss.get.name == bossName)
    if (index < 0) None else Some(index)
  }

  def follow(
    bossName: BossName
  ): Unit = if (columnIndex(bossName).isEmpty) {
    websocket.send(FollowRequest(bossNames = List(bossName)))

    // If it's not a boss we know about, create an empty column for it
    val column: RaidBossColumn = allBossesMap.getOrElse(bossName, {
      val newColumn = RaidBossColumn.empty(bossName)
      allBossesMap = allBossesMap.updated(bossName, newColumn)
      newColumn
    })

    state.followedBosses.get += column
    updateFollowedBossesLocalStorage()
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
      updateFollowedBossesLocalStorage()
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
      updateFollowedBossesLocalStorage()
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

    updateFollowedBossesLocalStorage()
  }

  def truncateColumns(maxColumnSize: Int): Unit = {
    state.allBosses.get.foreach { column =>
      val tweets = column.raidTweets.get

      if (tweets.length > maxColumnSize) {
        tweets.trimEnd(tweets.length - maxColumnSize)
      }
    }
  }

  def setNotificationSound(
    bossName:            BossName,
    notificationSoundId: Option[NotificationSoundId]
  ): Unit = {
    allBossesMap.get(bossName).foreach { column =>
      column.notificationSound := notificationSoundId.flatMap(NotificationSounds.findById)
      updateFollowedBossesLocalStorage()
    }
  }

  def getNotificationSound(bossName: BossName): Option[NotificationSound] = {
    allBossesMap.get(bossName).flatMap(_.notificationSound.get)
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

    case r: WelcomeResponse =>
      val isOutdatedOpt = for {
        clientVersion <- VersionString(BuildInfo.version).parse
        serverVersion <- r.serverVersion.parse
      } yield {
        serverVersion > clientVersion
      }

      if (isOutdatedOpt.getOrElse(false)) {
        HtmlHelpers.desktopNotification(
          title = s"gbf-raidfinder v${r.serverVersion.value} is out!",
          body = s"You are on v${BuildInfo.version}\n\n(Click to reload page)",
          icon = "/icons/android-chrome-192x192.png", // TODO: Don't hardcode this
          tag = "update",
          onClick = (e: dom.Event) => dom.window.location.reload(),
          closeOnClick = true
        )
      }

    case r: KeepAliveResponse => // Ignore
  }

  private def addRaidTweetToColumn(tweet: RaidTweetResponse, column: RaidBossColumn): Unit = {
    val columnTweets = column.raidTweets.get

    val shouldInsertAtBeginning = columnTweets.headOption.forall { firstTweetInColumn =>
      tweet.createdAt.after(firstTweetInColumn.createdAt)
    }

    if (shouldInsertAtBeginning) {
      tweet +=: columnTweets

      // Suppress notifications for backfill tweets when starting up
      if (!isStartingUp) {
        // Play notification sound for the column, if one is set
        column.notificationSound.get.foreach(_.play)

        // Show desktop notification, if subscribed
        if (column.isSubscribed.get) {
          val image = column.raidBoss.get.image.map(_ + ":thumb")
          desktopNotification(tweet, image)
        }
      }
    } else if (!columnTweets.exists(_.tweetId == tweet.tweetId)) {
      val insertIndex = columnTweets.indexWhere { existingTweet =>
        tweet.createdAt.after(existingTweet.createdAt)
      }

      if (insertIndex >= 0) {
        columnTweets.insert(insertIndex, tweet)
      } else columnTweets += tweet
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

  private def handleRaidBossesResponse(
    raidBosses: Seq[RaidBoss]
  ): Unit = {
    raidBosses.foreach { raidBoss =>
      val bossName = raidBoss.name

      allBossesMap.get(bossName) match {
        case None => // Add to our list of known bosses
          val newColumn = RaidBossColumn(
            raidBoss = Var(raidBoss),
            raidTweets = Vars.empty,
            isSubscribed = Var(false),
            notificationSound = Var(None)
          )
          allBossesMap = allBossesMap.updated(bossName, newColumn)

        case Some(column) => // Update existing raid boss data
          column.raidBoss := raidBoss
      }
    }

    state.allBosses.get := allBossesMap.values.toArray.sortBy { column =>
      val boss = column.raidBoss.get
      (boss.level, boss.name)
    }
  }

  private def refollowBosses(): Unit = {
    val followedBosses = state.followedBosses.get.map(_.raidBoss.get.name)
    websocket.send(FollowRequest(bossNames = followedBosses))
    updateBosses(followedBosses)
  }

  //
  // Persistence in LocalStorage
  //
  @ScalaJSDefined
  private trait JsFollowedBoss extends js.Object {
    def name: String
    def isSubscribed: Boolean
    def soundId: js.UndefOr[NotificationSoundId]
  }

  private def updateFollowedBossesLocalStorage(): Unit = {
    val jsFollowedBosses = state.followedBosses.get.map { column =>
      new JsFollowedBoss {
        val name = column.raidBoss.get.name
        val isSubscribed = column.isSubscribed.get
        val soundId = column.notificationSound.get.map(_.id).orUndefined
      }
    }

    val jsString = js.JSON.stringify(jsFollowedBosses.toJSArray)
    storage.setItem(FollowedBossesStorageKey, jsString)
  }

  private def fetchFollowedBossesLocalStorage(key: String): Seq[JsFollowedBoss] = {
    Option(storage.getItem(key)).fold(Seq.empty[JsFollowedBoss]) { jsString =>
      try {
        js.JSON.parse(jsString).asInstanceOf[js.Array[JsFollowedBoss]]
      } catch {
        case _: Throwable =>
          // If failed to parse as JSON
          val followedBossNames = jsString.split(",")
          val subscribedBossNames = legacyFetchLocalStorageCsv(LegacySubscribedBossesStorageKey)

          followedBossNames.map { bossName =>
            new JsFollowedBoss {
              val name = bossName
              val isSubscribed = subscribedBossNames.contains(bossName)
              val soundId = None.orUndefined
            }
          }
      }
    }
  }

  //
  // Legacy LocalStorage methods
  //
  private val LegacySubscribedBossesStorageKey = "subscribedBosses"
  private def legacyFetchLocalStorageCsv(key: String): Seq[String] = {
    Option(storage.getItem(key)).fold(Seq.empty[String])(_.split(","))
  }
  private def legacyUpdateLocalStorageFollowed(): Unit = {
    val followedBossNames = state.followedBosses.get.map(_.raidBoss.get.name)
    legacyUpdateLocalStorageCsv(FollowedBossesStorageKey, followedBossNames)
  }
  private def legacyUpdateLocalStorageSubscribed(): Unit = {
    val subscribedBossNames = state.allBosses.get.collect {
      case column if column.isSubscribed.get => column.raidBoss.get.name
    }
    legacyUpdateLocalStorageCsv(LegacySubscribedBossesStorageKey, subscribedBossNames)
  }
  private def legacyUpdateLocalStorageCsv(key: String, values: Seq[String]): Unit = {
    if (values.isEmpty)
      storage.removeItem(key)
    else
      storage.setItem(key, values.mkString(","))
  }
}

object RaidFinderClient {
  case class RaidBossColumn(
    raidBoss:          Var[RaidBoss],
    raidTweets:        Vars[RaidTweetResponse],
    isSubscribed:      Var[Boolean],
    notificationSound: Var[Option[NotificationSound]]
  ) { def clear(): Unit = raidTweets.get.clear() }

  object RaidBossColumn {
    def empty(bossName: BossName): RaidBossColumn = {
      val raidBoss = RaidBoss(name = bossName)
      RaidBossColumn(
        raidBoss = Var(raidBoss),
        raidTweets = Vars.empty,
        isSubscribed = Var(false),
        notificationSound = Var(None)
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

