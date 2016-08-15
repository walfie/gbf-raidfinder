package com.github.walfie.granblue.raidfinder

import com.github.walfie.granblue.raidfinder.util.TestObserver
import monix.execution.schedulers.TestScheduler
import monix.reactive.{Observable, Observer}
import org.mockito.Mockito._
import org.scalatest.{Status => ScalaTestStatus, _}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time._
import org.scalatest.Matchers._
import org.scalatest.mockito.MockitoSugar
import scala.collection.JavaConverters._
import scala.concurrent.Future
import twitter4j._

class Twitter4jSearchSpec extends Twitter4jSearchSpecHelpers {
  "observable" - {
    "return tweets in pages" in new TwitterFixture {
      val searchTerm = "searchy-search"
      val maxCount = 5

      val query1 = new Query(searchTerm).count(maxCount)
      val statuses1 = mockStatuses(3)
      val result1 = mockQueryResult(Some(123), statuses1)
      when(twitter.search(query1)) thenReturn result1

      val query2 = new Query(searchTerm).count(maxCount).sinceId(123)
      val statuses2 = mockStatuses(1)
      val result2 = mockQueryResult(Some(456), statuses2)
      when(twitter.search(query2)) thenReturn result2

      val observable = search.observable(searchTerm, None, maxCount)

      val resultF = observable.take(2).toListL.runAsync
      scheduler.tick()
      resultF.futureValue shouldBe Seq(statuses1, statuses2)
    }

    "continue on error" in new TwitterFixture {
      pending
    }
  }
}

trait Twitter4jSearchSpecHelpers extends FreeSpec with ScalaFutures with MockitoSugar {
  /* Since Twitter4jSearch uses BlockingIO, tests could take slightly longer
     even though everything is mocked (the default PatienceConfig is 150ms) */
  override implicit val patienceConfig = PatienceConfig(
    timeout = Span(3, Seconds),
    interval = Span(50, Millis)
  )

  trait TwitterFixture {
    implicit val scheduler = TestScheduler()
    val twitter = mock[Twitter]
    val search = TwitterSearch(twitter)
  }

  def mockQueryResult(maxId: Option[Long], tweets: Seq[Status]): QueryResult = {
    val result = mock[QueryResult]
    when(result.getTweets) thenReturn tweets.asJava
    maxId.foreach(when(result.getMaxId) thenReturn _)
    result
  }

  def mockStatuses(count: Int): Seq[Status] = (1 to count).map(_ => mock[Status])
}

