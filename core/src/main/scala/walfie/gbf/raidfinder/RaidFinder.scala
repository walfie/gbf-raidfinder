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
  val DefaultCacheSizePerBoss = 50
  val DefaultBacklogSize = 100

  /** Stream tweets without looking up old tweets first */
  def withoutBacklog(
    twitterStream:    TwitterStream = TwitterStreamFactory.getSingleton,
    cacheSizePerBoss: Int           = DefaultCacheSizePerBoss
  )(implicit scheduler: Scheduler): DefaultRaidFinder = {
    val statuses = TwitterStreamer(twitterStream).observable
    new DefaultRaidFinder(statuses, cacheSizePerBoss)
  }

  /** Search for old tweets first before streaming new tweets */
  def withBacklog(
    twitter:          Twitter       = TwitterFactory.getSingleton,
    twitterStream:    TwitterStream = TwitterStreamFactory.getSingleton,
    backlogSize:      Int           = DefaultBacklogSize,
    cacheSizePerBoss: Int           = DefaultCacheSizePerBoss
  )(implicit scheduler: Scheduler): DefaultRaidFinder = {
    import TwitterSearcher._

    val backlog = TwitterSearcher(twitter, TwitterSearcher.ReverseChronological)
      .observable(DefaultSearchTerm, None, MaxCount)
      .flatMap(Observable.fromIterable)
      .take(backlogSize)
    val newStatuses = TwitterStreamer(twitterStream).observable
    new DefaultRaidFinder(backlog ++ newStatuses, cacheSizePerBoss) {
      override def onShutdown(): Unit = {
        twitterStream.cleanUp()
        twitterStream.shutdown()
      }
    }
  }
}

class DefaultRaidFinder(
  statusesObservable: Observable[Status],
  cacheSizePerBoss:   Int
)(implicit scheduler: Scheduler) extends RaidFinder {
  /** Override this to perform additional cleanup on shutdown */
  protected def onShutdown(): Unit = ()

  private val raidInfos = statusesObservable
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
    onShutdown()
  }

  def shutdown(): Unit = cancelable.cancel()
  def getKnownBosses(): Map[BossName, RaidBoss] =
    knownBosses.get()
  def getRaidTweets(bossName: BossName): Observable[RaidTweet] =
    partitioner.getObservable(bossName)
}

