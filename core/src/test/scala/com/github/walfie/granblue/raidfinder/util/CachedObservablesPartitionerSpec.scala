package com.github.walfie.granblue.raidfinder.util

import java.util.concurrent.atomic.AtomicInteger
import monix.execution.Ack
import monix.execution.schedulers.ExecutionModel.SynchronousExecution
import monix.execution.schedulers.TestScheduler
import monix.reactive.Observer
import monix.reactive.subjects.{PublishSubject, ConcurrentSubject}
import org.scalatest._
import org.scalatest.Matchers._
import scala.concurrent.{ExecutionContext, Future}

class CachedObservablesPartitionerSpec extends CachedObservablesPartitionerSpecHelpers {
  import IdolColor._

  "getObservable" - {
    "allow getting observable on an unknown key" in new PartitionerFixture {
      partitioner.getObservable(Pink).subscribe(receiver)

      val akari = Idol("Oozora Akari", Pink)
      input.onNext(akari)

      scheduler.tick()
      receiver.received shouldBe Seq(akari)
    }

    "repeat cached elements for new subscribers" in new PartitionerFixture {
      val blueIdols = Seq(
        "Shibuya Rin",
        "Kamiya Nao",
        "Hojo Karen",
        "Toudou Yurika",
        "Hikami Sumire",
        "Nishikino Maki",
        "Sonoda Umi"
      ).map(Idol(_, Blue))

      // First receiver subscribes early on, and gets all the messages
      input.onNext(blueIdols.head)
      partitioner.getObservable(Blue).subscribe(receiver)
      scheduler.tick()

      receiver.received shouldBe blueIdols.take(1)
      blueIdols.tail.foreach(input.onNext)
      scheduler.tick()

      receiver.received shouldBe blueIdols

      // New receiver subscribes later, should get the latest cached messages
      val newReceiver = newTestObserver()
      partitioner.getObservable(Blue).subscribe(newReceiver)
      scheduler.tick()

      receiver.received shouldBe blueIdols
      newReceiver.received shouldBe blueIdols.takeRight(cacheSize)
    }
  }
}

trait CachedObservablesPartitionerSpecHelpers extends FreeSpec {
  sealed trait IdolColor
  object IdolColor {
    case object Pink extends IdolColor
    case object Blue extends IdolColor
    case object Orange extends IdolColor
    case object Purple extends IdolColor
  }
  case class Idol(name: String, color: IdolColor)

  trait PartitionerFixture {
    val cacheSize = 5
    implicit val scheduler = TestScheduler(SynchronousExecution)
    lazy val input = ConcurrentSubject.publish[Idol]
    lazy val partitioner = CachedObservablesPartitioner
      .fromObservable(input, cacheSize)(_.color)
    lazy val receiver = newTestObserver()
  }

  def newTestObserver() = new TestObserver[Idol]

  class TestObserver[T] extends Observer[T] {
    var received = Vector.empty[T]

    def onNext(elem: T): Future[Ack] = {
      received = received :+ elem
      Ack.Continue
    }

    def onError(ex: Throwable): Unit = ()
    def onComplete(): Unit = ()
  }
}

