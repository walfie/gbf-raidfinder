package com.github.walfie.granblue.raidfinder.flow

import akka.agent.Agent
import akka.NotUsed
import akka.stream.Materializer
import akka.stream.scaladsl._
import com.github.walfie.granblue.raidfinder.domain._
import java.util.Date
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext

trait RaidBossCache {
  import RaidBossCache.BossName

  def get(): Map[BossName, RaidBoss]
  def put(raidBoss: RaidBoss): Unit
}

object RaidBossCache {
  type BossName = String

  private[flow] val NeverSeen = new Date(0)
  val DefaultTtl = 3.hours
  val DefaultCheckInterval = 30.seconds

  def default(implicit ec: ExecutionContext, materializer: Materializer): RaidBossAgentCache =
    new RaidBossAgentCache(DefaultTtl, DefaultCheckInterval)
}

class RaidBossAgentCache(
  ttl: FiniteDuration, checkInterval: FiniteDuration
)(implicit ec: ExecutionContext, materializer: Materializer) extends RaidBossCache {
  import RaidBossCache.{BossName, NeverSeen}

  private val ttlMillis = ttl.toMillis

  private val agent: Agent[Map[BossName, RaidBoss]] = Agent(Map.empty)

  def get(): Map[BossName, RaidBoss] = agent.get()

  def put(raidBoss: RaidBoss): Unit = {
    val latestInCache: Option[RaidBoss] = get().get(raidBoss.bossName)
    val cachedLastSeen: Date = latestInCache.fold(NeverSeen)(_.lastSeen)

    if (raidBoss.lastSeen.after(cachedLastSeen)) {
      agent.send(_ + (raidBoss.bossName -> raidBoss))
    }
  }

  // TODO: Use clock for testing
  private def removeOldBosses(): Unit = {
    val filteredBosses = get().filter {
      case (bossName, boss) =>
        boss.lastSeen.getTime > (new Date().getTime - ttlMillis)
    }

    agent.send(_ => filteredBosses)
  }

  /** Periodically remove old bosses */
  val tickSource = Source
    .tick(checkInterval, checkInterval, NotUsed)
    .runForeach(_ => removeOldBosses())
}

