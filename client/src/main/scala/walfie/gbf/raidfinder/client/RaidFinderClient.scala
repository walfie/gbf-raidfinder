package walfie.gbf.raidfinder.client

import com.thoughtworks.binding.Binding._
import java.nio.ByteBuffer
import org.scalajs.dom
import org.scalajs.dom.raw.Storage
import scala.scalajs.js
import walfie.gbf.raidfinder.client.syntax.BufferOps
import walfie.gbf.raidfinder.protocol._

trait RaidFinderClient {
  def state: RaidFinderClient.State

  def updateBosses(): Unit
  def follow(bossName: BossName): Unit
  def unfollow(bossName: BossName): Unit
  def clear(bossName: BossName): Unit
  def move(bossName: BossName, displacement: Int): Unit
}

class WebSocketRaidFinderClient(
  websocket: WebSocketClient, storage: Storage
) extends RaidFinderClient with WebSocketSubscriber {
  import RaidFinderClient._

  websocket.setSubscriber(Some(this))

  override def onWebSocketClose(): Unit = {
    println("Websocket closed") // TODO: Better handling
  }

  private var allBossesMap: Map[BossName, RaidBossColumn] = Map.empty

  private val FollowingStorageKey = "following"

  // TODO: Initialize following to value in local storage
  val state = State(allBosses = Vars.empty, following = Vars.empty)
  Option(storage.getItem(FollowingStorageKey)).foreach(_.split(",").foreach(follow))

  private def updateLocalStorage(): Unit = {
    val bossNames = state.following.get.map(_.raidBoss.get.bossName)
    if (bossNames.isEmpty)
      storage.removeItem(FollowingStorageKey)
    else
      storage.setItem(FollowingStorageKey, bossNames.mkString(","))
  }

  def updateBosses(): Unit = {
    websocket.send(RaidBossesRequest())
  }

  def follow(bossName: BossName): Unit = {
    // TODO: Check if already following
    websocket.send(SubscribeRequest(bossNames = List(bossName)))

    // If it's not a boss we know about, create an empty column for it
    val column: RaidBossColumn = allBossesMap.getOrElse(bossName, {
      val newColumn = RaidBossColumn.empty(bossName)
      allBossesMap = allBossesMap.updated(bossName, newColumn)

      state.allBosses.get := allBossesMap.values
      newColumn
    })

    state.following.get += column
    updateLocalStorage()
  }

  def unfollow(bossName: BossName): Unit = {
    websocket.send(UnsubscribeRequest(bossNames = List(bossName)))

    val following = state.following.get
    val index = following.indexWhere(_.raidBoss.get.bossName == bossName)
    if (index >= 0) following.remove(index)
    allBossesMap.get(bossName).foreach(_.clear())

    updateLocalStorage()
  }

  def clear(bossName: BossName): Unit = {
    allBossesMap.get(bossName).foreach(_.clear())
  }

  def move(bossName: BossName, displacement: Int): Unit = {
    val following = state.following.get
    val index = following.indexWhere(_.raidBoss.get.bossName == bossName)
    val destinationIndex = index + displacement

    if (destinationIndex >= 0 && destinationIndex < following.size) {
      val thisColumn = following(index)
      val thatColumn = following(destinationIndex)
      following.update(destinationIndex, thisColumn)
      following.update(index, thatColumn)
    }
  }

  override def onWebSocketMessage(message: Response): Unit = message match {
    case r: RaidBossesResponse =>
      r.raidBosses.foreach { raidBoss =>
        val bossName = raidBoss.bossName
        allBossesMap.get(bossName) match {
          // New raid boss that we don't yet know about
          case None =>
            val column = RaidBossColumn(raidBoss = Var(raidBoss), raidTweets = Vars.empty)
            allBossesMap = allBossesMap.updated(bossName, column)
            state.allBosses.get := allBossesMap.values

          // Update existing raid boss data
          case Some(column) => column.raidBoss := raidBoss
        }
      }

    case r: SubscriptionStatusResponse =>
    // Ignore. Also TODO: Figure out why this doesn't come back consistently

    case r: RaidTweetResponse =>
      allBossesMap.get(r.bossName).foreach(column => r +=: column.raidTweets.get)
  }
}

object RaidFinderClient {
  case class RaidBossColumn(
    raidBoss:   Var[RaidBoss],
    raidTweets: Vars[RaidTweetResponse]
  ) { def clear(): Unit = raidTweets.get.clear() }

  object RaidBossColumn {
    def empty(bossName: BossName): RaidBossColumn = {
      val raidBoss = RaidBoss(bossName = bossName)
      RaidBossColumn(raidBoss = Var(raidBoss), raidTweets = Vars.empty)
    }
  }

  case class State(
    allBosses: Vars[RaidBossColumn],
    following: Vars[RaidBossColumn]
  )
}

