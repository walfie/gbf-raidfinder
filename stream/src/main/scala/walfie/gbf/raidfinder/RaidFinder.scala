package walfie.gbf.raidfinder

import java.util.Date
import monix.eval.Task
import monix.execution.{Cancelable, Scheduler}
import monix.reactive._
import scala.concurrent.duration._
import scala.concurrent.Future
import twitter4j._
import walfie.gbf.raidfinder.domain._

trait RaidFinder {
  def getRaidTweets(bossName: BossName): Observable[RaidTweet]
  def newBossObservable: Observable[RaidBoss]
  def getKnownBosses(): Map[BossName, RaidBoss]
  def purgeOldBosses(minDate: Date, levelThreshold: Int): Future[Map[BossName, RaidBoss]]
  def shutdown(): Unit
}

object RaidFinder {
  val DefaultCacheSizePerBoss = 20
  val DefaultBackfillSize = 200

  /** Stream tweets without looking up old tweets first */
  def withoutBackfill(
    twitterStream:       TwitterStream = TwitterStreamFactory.getSingleton,
    cachedTweetsPerBoss: Int           = DefaultCacheSizePerBoss,
    initialBosses:       Seq[RaidBoss] = Seq.empty
  )(implicit scheduler: Scheduler): DefaultRaidFinder = {
    val statuses = TwitterStreamer(twitterStream).observable
    new DefaultRaidFinder(statuses, cachedTweetsPerBoss, initialBosses)
  }

  /** Search for old tweets first before streaming new tweets */
  def withBackfill(
    twitter:             Twitter       = TwitterFactory.getSingleton,
    twitterStream:       TwitterStream = TwitterStreamFactory.getSingleton,
    backfillSize:        Int           = DefaultBackfillSize,
    cachedTweetsPerBoss: Int           = DefaultCacheSizePerBoss,
    initialBosses:       Seq[RaidBoss] = Seq.empty
  )(implicit scheduler: Scheduler): DefaultRaidFinder = {
    import TwitterSearcher._

    // Get backfill of tweets, then sort them by earliest first
    // TODO: This is getting kinda complex -- should write a test
    val backfillTask: Task[Seq[Status]] =
      TwitterSearcher(twitter, TwitterSearcher.ReverseChronological)
        .observable(DefaultSearchTerm, None, MaxCount)
        .flatMap(Observable.fromIterable)
        .take(backfillSize)
        .toListL
        .map(_.sortBy(_.getCreatedAt)) // earliest first

    // Once the backfill is populated, new tweets will stream in
    val backfillObservable = Observable.fromTask(backfillTask).flatMap(Observable.fromIterable)
    val newStatusesObservable = TwitterStreamer(twitterStream).observable

    new DefaultRaidFinder(
      backfillObservable ++ newStatusesObservable, cachedTweetsPerBoss, initialBosses
    ) {
      override def onShutdown(): Unit = {
        twitterStream.cleanUp()
        twitterStream.shutdown()
      }
    }
  }
}

class DefaultRaidFinder(
  statusesObservable:  Observable[Status],
  cachedTweetsPerBoss: Int,
  initialBosses:       Seq[RaidBoss]
)(implicit scheduler: Scheduler) extends RaidFinder {
  /** Override this to perform additional cleanup on shutdown */
  protected def onShutdown(): Unit = ()

  private val raidInfos = statusesObservable
    .collect(Function.unlift(StatusParser.parse))
    .publish

  // Whenever a new boss comes in, info gets published here
  // TODO: Maybe add test?
  val newBossObservable = raidInfos
    .map(_.boss)
    .groupBy(_.name)
    .flatMap(_.headF)
    .publish

  private val (partitioner, partitionerCancelable) = CachedRaidTweetsPartitioner
    .fromUngroupedObservable(raidInfos.map(_.tweet), cachedTweetsPerBoss)

  private val (knownBosses, knownBossesCancelable) = KnownBossesObserver
    .fromRaidInfoObservable(raidInfos, initialBosses)

  private val raidInfosCancelable = raidInfos.connect()
  private val newBossCancelable = newBossObservable.connect()

  private val cancelable = Cancelable { () =>
    List(
      raidInfosCancelable,
      partitionerCancelable,
      knownBossesCancelable,
      newBossCancelable
    ).foreach(_.cancel)
    onShutdown()
  }

  def shutdown(): Unit = cancelable.cancel()
  def getKnownBosses(): Map[BossName, RaidBoss] =
    knownBosses.get()
  def getRaidTweets(bossName: BossName): Observable[RaidTweet] =
    partitioner.getObservable(bossName)
  def purgeOldBosses(minDate: Date, levelThreshold: Int): Future[Map[BossName, RaidBoss]] =
    knownBosses.purgeOldBosses(minDate, levelThreshold)
}

