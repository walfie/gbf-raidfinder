package walfie.gbf.raidfinder.util

import monix.execution.Ack
import monix.reactive.Observer
import scala.concurrent.Future

class TestObserver[T] extends Observer[T] {
  var received = Vector.empty[T]
  var errorsReceived = Vector.empty[Throwable]
  var isComplete: Boolean = false

  def onNext(elem: T): Future[Ack] = {
    received = received :+ elem
    Ack.Continue
  }

  def onError(e: Throwable): Unit = {
    errorsReceived = errorsReceived :+ e
  }

  def onComplete(): Unit = {
    isComplete = true
  }
}

