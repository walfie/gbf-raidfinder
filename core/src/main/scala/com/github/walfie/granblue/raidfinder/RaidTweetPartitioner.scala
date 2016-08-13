package com.github.walfie.granblue.raidfinder

import akka.agent.Agent
import com.github.walfie.granblue.raidfinder.domain._
import monix.execution.{Ack, Cancelable, Scheduler}
import monix.reactive._
import monix.reactive.observables.GroupedObservable
import monix.reactive.observers.Subscriber
import monix.reactive.subjects.PublishSubject
import scala.concurrent.{ExecutionContext, Future}

trait RaidTweetPartitioner {
  def getObservable(bossName: BossName): Observable[RaidTweet]
}

object CachedRaidTweetPartitioner {
  def fromObservable(
    observable:       Observable[RaidTweet],
    cacheSizePerBoss: Int
  )(implicit scheduler: Scheduler): CachedRaidTweetPartitioner = {
    val partitioner = new CachedRaidTweetPartitioner(cacheSizePerBoss)
    observable.groupBy(_.bossName).subscribe(partitioner)
    partitioner
  }
}

class CachedRaidTweetPartitioner(cacheSizePerBoss: Int)(implicit ec: ExecutionContext)
  extends Observer[GroupedObservable[BossName, RaidTweet]] with RaidTweetPartitioner {

  private val raidInfoObservables =
    Agent[Map[BossName, Observable[RaidTweet]]](Map.empty)
  private val incomingBosses = PublishSubject[BossName]()

  def onComplete(): Unit = {
    incomingBosses.onComplete()
  }

  def onError(e: Throwable): Unit = {
    System.err.println(e) // TODO: Better logging?
  }

  /**
    * When a new raid boss comes in, add it to the Map of known raid bosses,
    * and push the boss name to the `incomingBosses` subject.
    */
  def onNext(elem: GroupedObservable[BossName, RaidTweet]): Future[Ack] = {
    val bossName = elem.key
    val newStream = elem.cache(cacheSizePerBoss)

    for {
      _ <- raidInfoObservables.alter { observables =>
        observables.updated(bossName, newStream)
      }
      _ <- incomingBosses.onNext(bossName)
      ack <- Ack.Continue
    } yield ack
  }

  /**
    * Get the Observable for a specific boss. If it doesn't exist, wait until
    * it comes in, and try again.
    */
  def getObservable(bossName: BossName): Observable[RaidTweet] = {
    raidInfoObservables.get.getOrElse(
      bossName,
      incomingBosses.findF(_ == bossName).flatMap(_ => getObservable(bossName))
    )
  }
}

