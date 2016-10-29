package walfie.gbf.raidfinder.util

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
  def fromUngroupedObservable[K, InputV, OutputV](
    observable:      Observable[InputV],
    cacheSizePerKey: Int,
    keySelector:     InputV => K,
    mappingFunction: InputV => OutputV
  )(implicit scheduler: Scheduler): (CachedObservablesPartitioner[K, InputV, OutputV], Cancelable) = {
    val partitioner = new CachedObservablesPartitioner[K, InputV, OutputV](cacheSizePerKey, mappingFunction)
    val cancelable = observable.groupBy(keySelector).subscribe(partitioner)
    (partitioner, cancelable)
  }
}

class CachedObservablesPartitioner[K, InputV, OutputV](
  cacheSizePerKey: Int, mappingFunction: InputV => OutputV
)(implicit ec: ExecutionContext)
  extends Observer[GroupedObservable[K, InputV]] with ObservablesPartitioner[K, OutputV] {

  private val observablesByKey = Agent[Map[K, Observable[OutputV]]](Map.empty)
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
  def onNext(elem: GroupedObservable[K, InputV]): Future[Ack] = {
    val key = elem.key
    val cachedObservable = elem.map(mappingFunction).cache(cacheSizePerKey)

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
  def getObservable(key: K): Observable[OutputV] = {
    observablesByKey.get.getOrElse(
      key,
      incomingKeys.findF(_ == key).flatMap(_ => getObservable(key))
    )
  }
}

