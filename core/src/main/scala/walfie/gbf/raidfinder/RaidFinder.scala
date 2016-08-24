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
    twitter:          Twitter        = TwitterFactory.getSingleton,
    cacheSizePerBoss: Int            = 50,
    pollingInterval:  FiniteDuration = 10.seconds,
    searchTerm:       String         = TwitterSearcher.DefaultSearchTerm
  )(implicit scheduler: Scheduler): DefaultRaidFinder =
    new DefaultRaidFinder(twitter, cacheSizePerBoss, pollingInterval, searchTerm)
}

class DefaultRaidFinder(
  twitter:          Twitter,
  cacheSizePerBoss: Int,
  pollingInterval:  FiniteDuration,
  searchTerm:       String
)(implicit scheduler: Scheduler) extends RaidFinder {
  private val timer = Observable.timerRepeated(0.seconds, pollingInterval, ())
  private val statuses = TwitterSearcher(twitter).observable(
    searchTerm, None, TwitterSearcher.MaxCount
  )

  private val raidInfos = statuses
    .zipMap(timer)((statuses, _) => statuses)
    .flatMap(Observable.fromIterable)
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

