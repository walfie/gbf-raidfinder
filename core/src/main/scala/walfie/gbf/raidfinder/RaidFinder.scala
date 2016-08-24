package walfie.gbf.raidfinder

import monix.execution.{Cancelable, Scheduler}
import monix.reactive._
import scala.concurrent.duration._
import twitter4j._
import walfie.gbf.raidfinder.domain._

trait RaidFinder {
  def getRaidTweets(bossName: BossName): Observable[RaidTweet]
  def getKnownBosses(): Map[BossName, RaidBoss]
  def shutdown(): Unit
}

object RaidFinder {
  def default(
    twitterStream:    TwitterStream = TwitterStreamFactory.getSingleton,
    cacheSizePerBoss: Int           = 50
  )(implicit scheduler: Scheduler): DefaultRaidFinder =
    new DefaultRaidFinder(twitterStream, cacheSizePerBoss)
}

class DefaultRaidFinder(
  twitterStream:    TwitterStream,
  cacheSizePerBoss: Int
)(implicit scheduler: Scheduler) extends RaidFinder {
  private val statuses = TwitterStreamer(
    twitterStream,
    TwitterStreamer.DefaultFilterTerms
  ).observable

  private val raidInfos = statuses
    .collect(Function.unlift(StatusParser.parse))
    .publish

  private val (partitioner, partitionerCancelable) = CachedRaidTweetsPartitioner
    .fromUngroupedObservable(raidInfos.map(_.tweet), cacheSizePerBoss)

  private val (knownBosses, knownBossesCancelable) = KnownBossesObserver
    .fromRaidInfoObservable(raidInfos)

  private val raidInfosCancelable = raidInfos.connect()

  private val cancelable = Cancelable { () =>
    List(
      raidInfosCancelable,
      partitionerCancelable,
      knownBossesCancelable
    ).foreach(_.cancel)
  }

  def shutdown(): Unit = cancelable.cancel()
  def getKnownBosses(): Map[BossName, RaidBoss] =
    knownBosses.get()
  def getRaidTweets(bossName: BossName): Observable[RaidTweet] =
    partitioner.getObservable(bossName)
}

