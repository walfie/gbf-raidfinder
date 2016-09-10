package walfie.gbf.raidfinder.client

import com.thoughtworks.binding.Binding
import com.thoughtworks.binding.Binding._
import java.util.Date
import org.scalajs.dom
import org.scalajs.dom.raw.Storage
import scala.scalajs.js
import walfie.gbf.raidfinder.client.syntax.BufferOps
import walfie.gbf.raidfinder.client.util.time.{Clock, Duration}
import walfie.gbf.raidfinder.client.ViewModel._
import walfie.gbf.raidfinder.protocol._

trait RaidFinderClient {
  def state: RaidFinderClient.State

  def updateBosses(bossNames: Seq[BossName]): Unit
  def updateAllBosses(): Unit
  def follow(bossName: BossName): Unit
  def unfollow(bossName: BossName): Unit
  def clear(bossName: BossName): Unit
  def move(bossName: BossName, displacement: Int): Unit

  def truncateColumns(maxColumnSize: Int): Unit
}

class WebSocketRaidFinderClient(
  websocket: WebSocketClient, storage: Storage, raidBossTtl: Duration, clock: Clock
) extends RaidFinderClient with WebSocketSubscriber {
  import RaidFinderClient._

  websocket.setSubscriber(Some(this))
  val isConnected: Var[Boolean] = Var(false)

  private var allBossesMap: Map[BossName, RaidBossColumn] = Map.empty

  // Load bosses from localStorage and follow them
  private val followedBossesStorageKey = "followedBosses"
  val state = State(allBosses = Vars.empty, followedBosses = Vars.empty)
  Option(storage.getItem(followedBossesStorageKey))
    .foreach(_.split(",").foreach(follow))

  override def onWebSocketOpen(): Unit = {
    refollowBosses()
    isConnected := true
  }

  override def onWebSocketReconnect(): Unit = {
    refollowBosses()
    isConnected := true
  }

  override def onWebSocketClose(): Unit = {
    isConnected := false
  }

  private def refollowBosses(): Unit = {
    val followedBosses = state.followedBosses.get.map(_.raidBoss.get.name)
    websocket.send(FollowRequest(bossNames = followedBosses))
    updateBosses(followedBosses)
  }

  private def updateLocalStorage(): Unit = {
    val bossNames = state.followedBosses.get.map(_.raidBoss.get.name)
    if (bossNames.isEmpty)
      storage.removeItem(followedBossesStorageKey)
    else
      storage.setItem(followedBossesStorageKey, bossNames.mkString(","))
  }

  def updateBosses(bossNames: Seq[BossName]): Unit =
    websocket.send(RaidBossesRequest(bossNames))
  def updateAllBosses(): Unit =
    websocket.send(AllRaidBossesRequest())

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
    updateLocalStorage()
  }

  def unfollow(bossName: BossName): Unit = {
    websocket.send(UnfollowRequest(bossNames = List(bossName)))

    val followedBosses = state.followedBosses.get
    columnIndex(bossName).foreach(followedBosses.remove)
    allBossesMap.get(bossName).foreach(_.clear())

    updateLocalStorage()
  }

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

    updateLocalStorage()
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

    case r: RaidTweetResponse =>
      allBossesMap.get(r.bossName).foreach { column =>
        val columnTweets = column.raidTweets.get
        val shouldInsert = columnTweets.headOption.forall { firstTweetInColumn =>
          r.createdAt.after(firstTweetInColumn.createdAt)
        }
        if (shouldInsert) r +=: columnTweets
      }

    case r: ErrorResponse =>
      dom.window.console.error(r.message) // TODO: Better error handling

    case r: KeepAliveResponse => // Ignore
  }

  // TODO: Exclude old bosses
  private def handleRaidBossesResponse(
    raidBosses: Seq[RaidBoss]
  ): Unit = {
    var shouldUpdateState = false // Sorry about the var

    raidBosses.foreach { raidBoss =>
      val bossName = raidBoss.name
      val expired = isExpired(raidBoss)

      allBossesMap.get(bossName) match {
        case None if expired => // Do nothing
        case None => // Add to our list of known bosses
          val newColumn = RaidBossColumn(raidBoss = Var(raidBoss), raidTweets = Vars.empty)
          allBossesMap = allBossesMap.updated(bossName, newColumn)
          shouldUpdateState = true

        case Some(column) if expired => // Remove from allBosses list
          allBossesMap = allBossesMap - bossName
          shouldUpdateState = true
        case Some(column) => // Update existing raid boss data
          column.raidBoss := raidBoss
      }
    }

    if (shouldUpdateState) {
      state.allBosses.get := allBossesMap.values.toArray.sortBy { column =>
        val boss = column.raidBoss.get
        (boss.level, boss.name)
      }
    }
  }

  private def isExpired(raidBoss: RaidBoss): Boolean = {
    val minDate = clock.now().getTime - raidBossTtl.milliseconds
    raidBoss.lastSeen.getTime < minDate
  }
}

object RaidFinderClient {
  case class RaidBossColumn(
    raidBoss:   Var[RaidBoss],
    raidTweets: Vars[RaidTweetResponse]
  ) { def clear(): Unit = raidTweets.get.clear() }

  object RaidBossColumn {
    def empty(bossName: BossName): RaidBossColumn = {
      val raidBoss = RaidBoss(name = bossName)
      RaidBossColumn(raidBoss = Var(raidBoss), raidTweets = Vars.empty)
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

