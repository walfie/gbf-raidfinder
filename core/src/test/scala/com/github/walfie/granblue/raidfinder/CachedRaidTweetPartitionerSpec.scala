package com.github.walfie.granblue.raidfinder

import com.github.walfie.granblue.raidfinder.domain._
import java.util.concurrent.atomic.AtomicInteger
import monix.execution.Ack
import monix.execution.schedulers.ExecutionModel.SynchronousExecution
import monix.execution.schedulers.TestScheduler
import monix.reactive.Observer
import monix.reactive.subjects.{PublishSubject, ConcurrentSubject}
import org.scalatest._
import org.scalatest.Matchers._
import scala.concurrent.{ExecutionContext, Future}

class CachedRaidTweetPartitionerSpec extends CachedRaidTweetPartitionerSpecHelpers {
  "getObservable" - {
    "allow getting observable on an unknown boss" in new PartitionerFixture {
      val boss = "Egglord"
      partitioner.getObservable(boss).subscribe(receiver)

      val tweet = newTweet(boss)
      input.onNext(tweet)

      scheduler.tick()
      receiver.received shouldBe Seq(tweet)
    }

    "repeat cached elements for new subscribers" in new PartitionerFixture {
      val boss = "Usamin"
      val allTweets = 1.to(cacheSize * 2).map(_ => newTweet(boss))

      // First receiver subscribes early on, and gets all the messages
      input.onNext(allTweets.head)
      partitioner.getObservable(boss).subscribe(receiver)
      scheduler.tick()

      receiver.received shouldBe allTweets.take(1)
      allTweets.tail.foreach(input.onNext)
      scheduler.tick()

      receiver.received shouldBe allTweets

      // New receiver subscribes later, should get the latest cached messages
      val newReceiver = newTestObserver()
      partitioner.getObservable(boss).subscribe(newReceiver)
      scheduler.tick()

      receiver.received shouldBe allTweets
      newReceiver.received shouldBe allTweets.takeRight(cacheSize)
    }
  }
}

trait CachedRaidTweetPartitionerSpecHelpers extends FreeSpec {
  trait PartitionerFixture {
    val cacheSize = 5
    implicit val scheduler = TestScheduler(SynchronousExecution)
    lazy val input = ConcurrentSubject.publish[RaidTweet]
    lazy val partitioner = CachedRaidTweetPartitioner.fromObservable(input, cacheSize)
    lazy val receiver = newTestObserver()

    private val latestTweetId = new AtomicInteger(0)
    def newTweet(bossName: String): RaidTweet = {
      val id = latestTweetId.getAndIncrement()
      RaidTweet(bossName, id.toString, id, "", "", "", new java.util.Date(0))
    }
  }

  def newTestObserver() = new TestObserver[RaidTweet]

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

