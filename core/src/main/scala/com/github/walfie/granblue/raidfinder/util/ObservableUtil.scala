package com.github.walfie.granblue.raidfinder.util

import monix.eval.{Callback, Task}
import monix.execution.Ack
import monix.reactive._
import monix.reactive.observers.Subscriber
import scala.concurrent.Future
import scala.util.control.NonFatal

object ObservableUtil {
  /**
    * Aync version of `Observable.fromStateAction`
    * Based on https://gist.github.com/alexandru/c497180db4f4275196f1d73bc62b0cfa
    */
  def fromAsyncStateAction[S, A](initialState: => S)(f: S => Task[(A, S)]): Observable[A] =
    Observable.unsafeCreate[A] { subscriber: Subscriber[A] =>
      import subscriber.scheduler

      def loop(state: S): Task[Unit] = try f(state).flatMap {
        case (a, s) => Task.fromFuture(subscriber.onNext(a)).flatMap {
          case Ack.Continue => loop(s)
          case Ack.Stop => Task.unit
        }
      } catch {
        case NonFatal(ex) => Task.raiseError(ex)
      }

      loop(initialState)
        .onErrorHandle(subscriber.onError)
        .runAsync(Callback.empty)
    }
}

