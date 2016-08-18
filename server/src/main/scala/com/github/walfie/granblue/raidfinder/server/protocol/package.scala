package com.github.walfie.granblue.raidfinder.server

import com.github.walfie.granblue.raidfinder.domain._

package protocol {
  sealed trait Request[ResponseT <: Response]
  sealed trait Response

  case class Subscribe(bossName: BossName) extends Request[Subscribed]
  case class Unsubscribe(bossName: BossName) extends Request[Subscribed]
  case object GetBosses extends Request[Bosses]

  case class Subscribed(bossNames: Set[BossName]) extends Response
  case class Bosses(bosses: Seq[RaidBoss]) extends Response
}

