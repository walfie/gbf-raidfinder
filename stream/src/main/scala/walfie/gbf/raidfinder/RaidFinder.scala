package walfie.gbf.raidfinder

import java.util.Date
import monix.eval.Task
import monix.execution.{Cancelable, Scheduler}
import monix.reactive._
import scala.concurrent.duration._
import scala.concurrent.Future
import twitter4j._
import walfie.gbf.raidfinder.domain._
import walfie.gbf.raidfinder.util.CachedObservablesPartitioner

trait RaidFinder[T] {
  def getRaidTweets(bossName: BossName): Observable[T]
  def newBossObservable: Observable[RaidBoss]
  def getKnownBosses(): Map[BossName, RaidBoss]
  def purgeOldBosses(
    minDate:        Date,
    levelThreshold: Option[Int]
  ): Future[Map[BossName, RaidBoss]]

  def shutdown(): Unit
}

object RaidFinder {
  val DefaultCacheSizePerBoss = 20
  val DefaultBackfillSize = 200

  /** Stream tweets without looking up old tweets first */
  def withoutBackfill[T: FromRaidTweet](
    twitterStream:       TwitterStream = TwitterStreamFactory.getSingleton,
    cachedTweetsPerBoss: Int           = DefaultCacheSizePerBoss,
    initialBosses:       Seq[RaidBoss] = Seq.empty
  )(implicit scheduler: Scheduler): RaidFinderImpl[T] = {
    val statuses = TwitterStreamer(twitterStream).observable
    new RaidFinderImpl(statuses, cachedTweetsPerBoss, initialBosses) {
      override def onShutdown(): Unit = {
        twitterStream.cleanUp()
        twitterStream.shutdown()
      }
    }
  }

  /** Search for old tweets first before streaming new tweets */
  def withBackfill[T: FromRaidTweet](
    twitter:             Twitter       = TwitterFactory.getSingleton,
    twitterStream:       TwitterStream = TwitterStreamFactory.getSingleton,
    backfillSize:        Int           = DefaultBackfillSize,
    cachedTweetsPerBoss: Int           = DefaultCacheSizePerBoss,
    initialBosses:       Seq[RaidBoss] = Seq.empty
  )(implicit scheduler: Scheduler): RaidFinderImpl[T] = {
    import TwitterSearcher._

    // Get backfill of tweets, then sort them by earliest first
    // TODO: This is getting kinda complex -- should write a test
    val backfillTask: Task[Seq[Status]] =
      TwitterSearcher(twitter, TwitterSearcher.ReverseChronological)
        .observable(DefaultSearchTerm, None, MaxCount)
        .take(backfillSize / MaxCount)
        .flatMap(Observable.fromIterable)
        .toListL
        .map(_.sortBy(_.getCreatedAt)) // earliest first

    // Once the backfill is populated, new tweets will stream in
    val backfillObservable = Observable.fromTask(backfillTask).flatMap(Observable.fromIterable)
    val newStatusesObservable = TwitterStreamer(twitterStream).observable

    new RaidFinderImpl(
      backfillObservable ++ newStatusesObservable, cachedTweetsPerBoss, initialBosses
    ) {
      override def onShutdown(): Unit = {
        twitterStream.cleanUp()
        twitterStream.shutdown()
      }
    }
  }
}

class RaidFinderImpl[T](
  statusesObservable:  Observable[Status],
  cachedTweetsPerBoss: Int,
  initialBosses:       Seq[RaidBoss]
)(implicit scheduler: Scheduler, fromRaidTweet: FromRaidTweet[T]) extends RaidFinder[T] {
  /** Override this to perform additional cleanup on shutdown */
  protected def onShutdown(): Unit = ()

  // TODO: Parsing happens twice somewhere -- should figure out where
  private val raidInfos = statusesObservable
    .collect(Function.unlift(StatusParser.parse))
    .publish

  private val (partitioner, partitionerCancelable) =
    CachedObservablesPartitioner.fromUngroupedObservable(
      raidInfos.map(_.tweet),
      cachedTweetsPerBoss,
      (_: RaidTweet).bossName,
      fromRaidTweet.from // TODO
    )

  private val (knownBosses, knownBossesCancelable) = KnownBossesObserver
    .fromRaidInfoObservable(raidInfos, initialBosses)

  val newBossObservable = knownBosses.newBossObservable

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
  def getRaidTweets(bossName: BossName): Observable[T] =
    partitioner.getObservable(bossName)

  def purgeOldBosses(
    minDate:        Date,
    levelThreshold: Option[Int]
  ): Future[Map[BossName, RaidBoss]] =
    knownBosses.purgeOldBosses(minDate, levelThreshold)
}

