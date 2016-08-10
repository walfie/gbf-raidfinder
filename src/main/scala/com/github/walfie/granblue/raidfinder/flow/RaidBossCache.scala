package com.github.walfie.granblue.raidfinder.flow

import akka.NotUsed
import akka.stream.scaladsl._
import com.github.walfie.granblue.raidfinder.domain._
import java.util.Date
import scala.concurrent.duration._

object RaidBossCache {
  type BossName = String

  val DefaultTtl = 30.minutes
  val DefaultCheckInterval = 30.seconds

  private val NeverSeen = new Date(0)

  val aggregate = Flow[RaidBoss].scan(Map.empty[BossName, RaidBoss]) { (acc, raidBoss) =>
    val cachedLastSeen = acc.get(raidBoss.bossName).fold(NeverSeen)(_.lastSeen)
    if (raidBoss.lastSeen.after(cachedLastSeen)) {
      acc + (raidBoss.bossName -> raidBoss)
    } else acc
  }

  // TODO: Add implicit Clock for better testing
  // TODO: This doesn't actually work except on the exact tick. Should rework this.
  def aggregateWithTtl(
    ttl:           FiniteDuration = DefaultTtl,
    checkInterval: FiniteDuration = DefaultCheckInterval
  ): Flow[RaidBoss, Map[BossName, RaidBoss], NotUsed] = {
    val ttlMillis = ttl.toMillis

    val tickSource = Source
      .tick(0.seconds, checkInterval, true)
      .expand(_ => Iterator.continually(false))

    aggregate.zipWith(tickSource) { (bosses, shouldFilter) =>
      if (shouldFilter) bosses.filter {
        case (bossName, boss) =>
          boss.lastSeen.getTime > (new Date().getTime - ttlMillis)
      }
      else bosses
    }
  }
}

