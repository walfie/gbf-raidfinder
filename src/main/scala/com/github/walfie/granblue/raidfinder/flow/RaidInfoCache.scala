package com.github.walfie.granblue.raidfinder.flow

import akka.actor.Cancellable
import akka.agent.Agent
import akka.NotUsed
import akka.stream.scaladsl._
import com.github.walfie.granblue.raidfinder.domain._
import java.util.Date
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext

trait RaidInfoCache {
  import RaidInfoCache.BossName

  def getTweets(bossName: BossName): Seq[RaidTweet]
  def getNames(): Set[BossName]
  def getImages(): Map[BossName, Option[RaidImage]]

  def put(raidInfos: Seq[RaidInfo]): Unit

  def evictOldItems(minDate: Date): Unit
}

object RaidInfoCache {
  type BossName = String

  val DefaultCacheSizePerBoss = 50

  def default(implicit ec: ExecutionContext): RaidInfoAgentCache =
    new RaidInfoAgentCache(DefaultCacheSizePerBoss)

  def cacheEvictionScheduler(
    cache:        RaidInfoCache,
    ttl:          FiniteDuration,
    tickInterval: FiniteDuration
  ): RunnableGraph[Cancellable] = {
    val ttlMillis = ttl.toMillis
    Source.tick(tickInterval, tickInterval, NotUsed).to(Sink.foreach { _ =>
      // TODO: Use clock for easier testing
      val minDate = new Date(System.currentTimeMillis - ttlMillis)
      cache.evictOldItems(minDate)
    })
  }
}

class RaidInfoAgentCache(
  cacheSizePerBoss: Int
)(implicit ec: ExecutionContext) extends RaidInfoCache {
  import RaidInfoCache.BossName

  private val tweetsAgent: Agent[Map[BossName, Seq[RaidTweet]]] = Agent(Map.empty)
  private val imagesAgent: Agent[Map[BossName, Option[RaidImage]]] = Agent(Map.empty)

  def put(raidInfos: Seq[RaidInfo]): Unit = {
    val raidInfosByBossName: Map[BossName, Seq[RaidInfo]] =
      raidInfos.groupBy(_.tweet.bossName)

    updateTweetsAgent(raidInfosByBossName.mapValues(_.map(_.tweet)))
    updateImagesAgent(raidInfosByBossName)
  }

  def getTweets(bossName: BossName): Seq[RaidTweet] =
    tweetsAgent.get().getOrElse(bossName, Seq.empty)

  def getNames(): Set[BossName] =
    imagesAgent.get().keys.toSet

  def getImages(): Map[BossName, Option[RaidImage]] =
    imagesAgent.get()

  def evictOldItems(minDate: Date): Unit = {
    val alteredTweetsF = tweetsAgent.alter {
      _.mapValues { raidTweets: Seq[RaidTweet] =>
        raidTweets.filter(_.createdAt.after(minDate))
      }.filter(_._2.nonEmpty)
    }

    alteredTweetsF.map { tweets: Map[BossName, Seq[RaidTweet]] =>
      val bossNames = tweets.keys.toSet
      imagesAgent.send(_.filterKeys(bossNames))
    }
  }

  private def updateTweetsAgent(
    raidTweetsByBossName: Map[BossName, Seq[RaidTweet]]
  ): Unit = {
    tweetsAgent.send(combineRaidTweets(_, raidTweetsByBossName))
  }

  private def updateImagesAgent(
    raidInfosByBossName: Map[BossName, Seq[RaidInfo]]
  ): Unit = {
    imagesAgent.send(_ ++ raidInfosByBossName.mapValues(_.head.image))
  }

  // Kinda inefficient
  private def combineRaidTweets(
    oldRaidTweets: Map[BossName, Seq[RaidTweet]],
    newRaidTweets: Map[BossName, Seq[RaidTweet]]
  ): Map[BossName, Seq[RaidTweet]] = {
    oldRaidTweets ++ newRaidTweets.map {
      case (bossName, newTweets) =>
        val oldTweets = oldRaidTweets.getOrElse(bossName, Seq.empty)

        val combinedTweets = (newTweets ++ oldTweets)
          .sortBy(-_.createdAt.getTime) // latest first

        bossName -> combinedTweets.take(cacheSizePerBoss)
    }
  }
}

