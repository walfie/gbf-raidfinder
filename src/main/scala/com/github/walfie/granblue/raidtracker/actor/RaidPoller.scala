package com.github.walfie.granblue.raidtracker.actor

import akka.actor._
import akka.cluster.pubsub.DistributedPubSub
import akka.cluster.pubsub.DistributedPubSubMediator.Publish
import com.github.walfie.granblue.raidtracker._
import scala.concurrent.duration._

// This maintains so much state and is annoying to test
class RaidPoller(
  tweetSearcher:       TweetSearcher,
  raidParser:          RaidParser,
  searchTerm:          String,
  pollingInterval:     FiniteDuration,
  raidTtl:             FiniteDuration,
  raidTweetsCacheSize: Int,
  pubSubMediator:      Option[ActorRef]
) extends Actor {
  import scala.concurrent.ExecutionContext.Implicits.global
  import RaidPoller._

  val polling = context.system.scheduler.schedule(
    0.seconds, pollingInterval, self, Tick
  )(context.system.dispatcher)

  // Internal state variables
  type RaidBossName = String
  private var raidBosses: Map[RaidBossName, RaidBoss] = Map.empty
  private var raidTweetsCache: Map[RaidBossName, Seq[RaidTweet]] = Map.empty
  private var latestTweetId: Option[Long] = None

  def receive: Receive = {
    case Tick =>
      tweetSearcher.search(searchTerm, latestTweetId).foreach(self ! _)
      removeOldRaids()

    case searchResult: TweetSearchResult =>
      val newRaidTweets: Seq[RaidTweet] = getRaidsFromSearchResult(searchResult)

      publishRaids(newRaidTweets.map(_.raid))

      latestTweetId = searchResult.maxId
      raidBosses = raidBosses ++ getRaidBosses(newRaidTweets)
      raidTweetsCache = combineRaidTweets(raidTweetsCache, newRaidTweets, raidTweetsCacheSize)
      removeOldRaids()

    case GetRaidBosses =>
      sender ! RaidBossesMessage(raidBosses.values.toSeq)

    case GetCachedRaids(bossName) =>
      sender ! RaidsMessage(bossName, raidTweetsCache.getOrElse(bossName, Seq.empty).map(_.raid))
  }

  private def publishRaids(raids: Seq[Raid]): Unit = pubSubMediator.foreach { mediator =>
    raids.groupBy(_.bossName).foreach {
      case (bossName, raids) =>
        mediator ! Publish(bossName, RaidsMessage(bossName, raids))
    }
  }

  private def getRaidsFromSearchResult(result: TweetSearchResult): Seq[RaidTweet] = {
    result.tweets.flatMap { tweet: Tweet =>
      raidParser.parseText(tweet.text).map(RaidTweet(tweet, _))
    }
  }

  private def combineRaidTweets(
    oldRaidTweets: Map[RaidBossName, Seq[RaidTweet]],
    newRaidTweets: Seq[RaidTweet],
    maxSize:       Int
  ): Map[RaidBossName, Seq[RaidTweet]] = {
    oldRaidTweets ++ newRaidTweets.groupBy(_.raid.bossName).map {
      case (bossName, newTweets) =>
        val oldTweets = oldRaidTweets.getOrElse(bossName, Seq.empty)

        val combinedTweets = (newTweets ++ oldTweets).sortBy(-_.tweet.createdAt.getTime) // latest first

        bossName -> combinedTweets.take(maxSize)
    }
  }

  private def getRaidBosses(
    raidTweets: Seq[RaidTweet]
  ): Map[RaidBossName, RaidBoss] = {
    raidTweets
      .groupBy(_.raid.bossName) // Map[RaidBossName, Seq[RaidTweet]]
      .mapValues { raidTweets: Seq[RaidTweet] =>
        val raidTweet = raidTweets.head
        val image = raidTweet.tweet.images.headOption.map(_.thumb)
        RaidBoss(raidTweet.raid.bossName, image, raidTweet.tweet.createdAt)
      }
  }

  private val raidTtlMillis = raidTtl.toMillis

  private def removeOldRaids(): Unit = {
    val now = System.currentTimeMillis()
    val minTime = now - raidTtlMillis
    raidBosses = raidBosses.filter(_._2.lastSeen.getTime > minTime)
    raidTweetsCache = raidTweetsCache.flatMap {
      case (bossName, raidTweets) =>
        val newRaidTweets = raidTweets.filter(_.tweet.createdAt.getTime > minTime)
        if (newRaidTweets.isEmpty) None else Some(bossName -> newRaidTweets)
    }
  }
}

object RaidPoller {
  val DefaultSearchTerm = "参加者募集！参戦ID："
  val DefaultPollingInterval = 10.seconds // Max 180 queries in 15 mins (average 1 query in 5 seconds)
  val DefaultRaidTtl = 30.minutes
  val DefaultRaidTweetCacheSize = 50

  def defaultProps(mediator: Option[ActorRef]) = Props(
    new RaidPoller(
      TweetSearcher.default,
      RaidParser.Default,
      DefaultSearchTerm,
      DefaultPollingInterval,
      DefaultRaidTtl,
      DefaultRaidTweetCacheSize,
      mediator
    )
  ).withDeploy(Deploy.local)

  private case object Tick

  // TODO: Put this in some kind of protocol object to distinguish between internal domain objects
  case class RaidTweet(tweet: Tweet, raid: Raid)

  case object GetRaidBosses
  case class GetCachedRaids(bossName: String)
  case class RaidBossesMessage(raidBosses: Seq[RaidBoss])
  case class RaidsMessage(bossName: String, raids: Seq[Raid])
}

