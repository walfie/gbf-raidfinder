package com.github.walfie.granblue.raidtracker.actor

import akka.actor._
import scala.concurrent.duration._
import com.github.walfie.granblue.raidtracker._

class RaidPoller(
  tweetSearcher:   TweetSearcher,
  raidParser:      RaidParser,
  searchTerm:      String,
  pollingInterval: FiniteDuration
) extends Actor {
  import scala.concurrent.ExecutionContext.Implicits.global
  import RaidPoller._

  val polling = context.system.scheduler.schedule(
    0.seconds, pollingInterval, self, GetLatestRaids
  )(context.system.dispatcher)

  // Internal state variables
  type RaidBossName = String
  private var raidBosses: Map[RaidBossName, RaidBoss] = Map.empty
  private var latestTweetId: Option[Long] = None

  def receive: Receive = {
    case GetLatestRaids =>
      tweetSearcher.search(searchTerm, latestTweetId).foreach(self ! _)

    case searchResult: TweetSearchResult =>
      val raidTweets: Seq[RaidTweet] = getRaidsFromSearchResult(searchResult)
      latestTweetId = searchResult.maxId
      raidBosses = raidBosses ++ getRaidBosses(raidTweets)

      // TODO: Change this
      raidTweets.foreach(println)
      raidBosses.foreach(println)
  }

  private case class RaidTweet(tweet: Tweet, raid: Raid)
  private def getRaidsFromSearchResult(result: TweetSearchResult): Seq[RaidTweet] = {
    result.tweets.flatMap { tweet: Tweet =>
      raidParser.parseText(tweet.text).map(RaidTweet(tweet, _))
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
}

object RaidPoller {
  val DefaultSearchTerm = "参加者募集！参戦ID："
  val DefaultPollingInterval = 20.seconds

  val DefaultProps = Props(
    new RaidPoller(
      TweetSearcher.fromSingleton(),
      RaidParser.Default,
      DefaultSearchTerm,
      DefaultPollingInterval
    )
  ).withDeploy(Deploy.local)

  private case object GetLatestRaids
  private case class RaidBoss(name: String, image: Option[String], lastSeen: java.util.Date)
}

