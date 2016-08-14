package com.github.walfie.granblue.raidfinder.util

import akka.agent.Agent
import monix.execution.{Ack, Cancelable, Scheduler}
import monix.reactive._
import monix.reactive.observables.GroupedObservable
import monix.reactive.observers.Subscriber
import monix.reactive.subjects.PublishSubject
import scala.concurrent.{ExecutionContext, Future}

trait ObservablesPartitioner[K, V] {
  def getObservable(key: K): Observable[V]
}

object CachedObservablesPartitioner {
  def fromUngroupedObservable[K, V](observable: Observable[V], cacheSizePerKey: Int)(
    keySelector: V => K
  )(implicit scheduler: Scheduler): CachedObservablesPartitioner[K, V] = {
    val partitioner = new CachedObservablesPartitioner[K, V](cacheSizePerKey)
    observable.groupBy(keySelector).subscribe(partitioner)
    partitioner
  }
}

class CachedObservablesPartitioner[K, V](cacheSizePerKey: Int)(implicit ec: ExecutionContext)
  extends Observer[GroupedObservable[K, V]] with ObservablesPartitioner[K, V] {

  private val observablesByKey = Agent[Map[K, Observable[V]]](Map.empty)
  private val incomingKeys = PublishSubject[K]()

  def onComplete(): Unit = {
    incomingKeys.onComplete()
  }

  def onError(e: Throwable): Unit = {
    System.err.println(e) // TODO: Better logging?
    incomingKeys.onError(e)
  }

  /**
    * When a new key comes in, add it to the Map of known keys/values, and push
    * the key to the internal `incomingKeys` subject, to inform subscribers
    * that attempted to get an observable on an unknown key.
    */
  def onNext(elem: GroupedObservable[K, V]): Future[Ack] = {
    val key = elem.key
    val cachedObservable = elem.cache(cacheSizePerKey)

    for {
      _ <- observablesByKey.alter(_.updated(key, cachedObservable))
      _ <- incomingKeys.onNext(key)
      ack <- Ack.Continue
    } yield ack
  }

  /**
    * Get the Observable for a specific boss. If it doesn't exist, wait until
    * it comes in, and try again.
    *
    * NOTE: This may be a cold observable, so it should not be shared by subscribers
    * unless explicitly turned into a hot observable (e.g., with `.publish`)
    */
  def getObservable(key: K): Observable[V] = {
    observablesByKey.get.getOrElse(
      key,
      incomingKeys.findF(_ == key).flatMap(_ => getObservable(key))
    )
  }
}

