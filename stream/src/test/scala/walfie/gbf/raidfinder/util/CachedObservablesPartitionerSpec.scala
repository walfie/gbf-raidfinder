package walfie.gbf.raidfinder.util

import monix.execution.Ack
import monix.execution.schedulers.ExecutionModel.SynchronousExecution
import monix.execution.schedulers.TestScheduler
import monix.reactive.Observer
import monix.reactive.subjects._
import org.scalatest._
import org.scalatest.Matchers._
import scala.concurrent.{ExecutionContext, Future}

class CachedObservablesPartitionerSpec extends CachedObservablesPartitionerSpecHelpers {
  import IdolColor._

  "getObservable" - {
    "allow getting observables on unknown keys" in new PartitionerFixture {
      Seq(Pink, Orange, Blue).foreach { color =>
        partitioner.getObservable(color).subscribe(receiver)
      }

      val akari = Idol("Oozora Akari", Pink)
      input.onNext(akari)
      scheduler.tick()
      receiver.received shouldBe Seq(akari)

      val hinaki = Idol("Shinjou Hinaki", Orange)
      input.onNext(hinaki)
      scheduler.tick()
      receiver.received shouldBe Seq(akari, hinaki)

      val sumire = Idol("Hikami Sumire", Blue)
      input.onNext(sumire)
      scheduler.tick()
      receiver.received shouldBe Seq(akari, hinaki, sumire)

      cancelable.cancel()
    }

    "repeat cached elements for new subscribers" in new PartitionerFixture {
      val blueIdols = Seq(
        "Shibuya Rin", "Kamiya Nao", "Hojo Karen", "Toudou Yurika",
        "Hikami Sumire", "Nishikino Maki", "Sonoda Umi"
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
      val newReceiver = new TestObserver[Idol]()
      partitioner.getObservable(Blue).subscribe(newReceiver)
      scheduler.tick()

      receiver.received shouldBe blueIdols
      newReceiver.received shouldBe blueIdols.takeRight(cacheSize)

      // More elements get pushed, all receivers should get them
      val moreBlueIdols = Seq(
        "Kiriya Aoi", "Kurosawa Rin", "Hayami Kanade"
      ).map(Idol(_, Blue))
      moreBlueIdols.foreach(input.onNext)
      scheduler.tick()

      receiver.received shouldBe (blueIdols ++ moreBlueIdols)
      newReceiver.received shouldBe (blueIdols.takeRight(cacheSize) ++ moreBlueIdols)

      cancelable.cancel()
    }

    "cache even when there are no subscribers" in new PartitionerFixture {
      val purpleIdols = Seq(
        "Kanzaki Mizuki", "Shibuki Ran", "Kurebayashi Juri"
      ).map(Idol(_, Purple))

      purpleIdols.foreach(input.onNext)
      scheduler.tick()

      partitioner.getObservable(Purple).subscribe(receiver)
      scheduler.tick()
      receiver.received shouldBe purpleIdols

      cancelable.cancel()
    }

    // TODO: Add test for partitioner with mapping function
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
    lazy val input = ConcurrentSubject.replay[Idol]
    lazy val (partitioner, cancelable) = CachedObservablesPartitioner
      .fromUngroupedObservable(input, cacheSize, (_: Idol).color, identity[Idol])
    lazy val receiver = new TestObserver[Idol]
  }
}

