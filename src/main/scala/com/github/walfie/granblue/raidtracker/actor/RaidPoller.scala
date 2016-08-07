package com.github.walfie.granblue.raidtracker.actor

import akka.actor._
import scala.concurrent.duration._
import com.github.walfie.granblue.raidtracker._

class RaidPoller(
  tweetSearcher: TweetSearcher,
  raidParser: RaidParser,
  searchTerm: String,
  pollingInterval: FiniteDuration
) extends Actor {
  import scala.concurrent.ExecutionContext.Implicits.global
  import RaidPoller._

  var latestTweetId: Option[Long] = None

  override def preStart(): Unit = self ! GetLatestRaids

  def receive: Receive = {
    case GetLatestRaids =>
      tweetSearcher.search(searchTerm, latestTweetId).foreach(self ! _)

    case result: TweetSearchResult =>
      latestTweetId = result.maxId
      val raids: Seq[Raid] = getRaidsFromSearchResult(result)
      raids.foreach(println) // TODO: Change this

      context.system.scheduler.scheduleOnce(
        pollingInterval, self, GetLatestRaids
      )(context.system.dispatcher)
  }

  def getRaidsFromSearchResult(result: TweetSearchResult): Seq[Raid] = {
    result.tweets.flatMap { tweet: Tweet =>
      raidParser.parseText(tweet.text)
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
}

