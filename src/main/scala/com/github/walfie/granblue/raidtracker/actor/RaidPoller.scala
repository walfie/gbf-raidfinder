package com.github.walfie.granblue.raidtracker.actor

import akka.actor._
import scala.concurrent.duration._
import com.github.walfie.granblue.raidtracker._

// This maintains so much state and is annoying to test
class RaidPoller(
  tweetSearcher:       TweetSearcher,
  raidParser:          RaidParser,
  searchTerm:          String,
  pollingInterval:     FiniteDuration,
  raidTtl:             FiniteDuration,
  raidTweetsCacheSize: Int
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
      latestTweetId = searchResult.maxId
      raidBosses = raidBosses ++ getRaidBosses(newRaidTweets)
      raidTweetsCache = combineRaidTweets(raidTweetsCache, newRaidTweets, raidTweetsCacheSize)

      // TODO: Change this
      raidTweetsCache.foreach(println)
      raidBosses.foreach(println)
  }

  private case class RaidTweet(tweet: Tweet, raid: Raid)
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
    newRaidTweets.groupBy(_.raid.bossName).map {
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
  val DefaultPollingInterval = 20.seconds
  val DefaultRaidTtl = 30.minutes
  val DefaultRaidTweetCacheSize = 50

  def defaultProps = Props(
    new RaidPoller(
      TweetSearcher.default,
      RaidParser.Default,
      DefaultSearchTerm,
      DefaultPollingInterval,
      DefaultRaidTtl,
      DefaultRaidTweetCacheSize
    )
  ).withDeploy(Deploy.local)

  private case object Tick
  private case class RaidBoss(name: String, image: Option[String], lastSeen: java.util.Date)
}

