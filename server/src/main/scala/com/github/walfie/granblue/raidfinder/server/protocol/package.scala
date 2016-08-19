package com.github.walfie.granblue.raidfinder.server

import com.github.walfie.granblue.raidfinder.domain._

package protocol {
  sealed trait WebsocketRequest[WebsocketResponseT <: WebsocketResponse]
  sealed trait WebsocketResponse

  case class Subscribe(bossName: BossName) extends WebsocketRequest[Subscribed]
  case class Unsubscribe(bossName: BossName) extends WebsocketRequest[Subscribed]
  case object GetRaidBosses extends WebsocketRequest[RaidBosses]

  case class Subscribed(bossNames: Set[BossName]) extends WebsocketResponse
  case class RaidBosses(raidBosses: Seq[RaidBoss]) extends WebsocketResponse
}

package object protocol {
  implicit class RaidTweetWrapper(val underlying: RaidTweet) extends WebsocketResponse
}

