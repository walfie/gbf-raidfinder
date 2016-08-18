package com.github.walfie.granblue.raidfinder.server

import com.github.walfie.granblue.raidfinder.domain._

package protocol {
  sealed trait WebsocketRequest[WebsocketResponseT <: WebsocketResponse]
  sealed trait WebsocketResponse

  case class Subscribe(bossName: BossName) extends WebsocketRequest[Subscribed]
  case class Unsubscribe(bossName: BossName) extends WebsocketRequest[Subscribed]
  case object GetBosses extends WebsocketRequest[Bosses]

  case class Subscribed(bossNames: Set[BossName]) extends WebsocketResponse
  case class Bosses(bosses: Seq[RaidBoss]) extends WebsocketResponse
}

