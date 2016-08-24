package walfie.gbf.raidfinder

import java.util.Date
import monix.execution.Scheduler
import monix.reactive.{Observable, Observer}
import org.mockito.Matchers.any
import org.mockito.Mockito._
import org.scalatest.concurrent.{PatienceConfiguration, ScalaFutures}
import org.scalatest.Matchers._
import org.scalatest.mockito.MockitoSugar
import org.scalatest.time._
import org.scalatest.{Status => ScalaTestStatus, _}
import scala.collection.JavaConverters._
import scala.concurrent.Future
import scala.util.Random
import twitter4j._
import walfie.gbf.raidfinder.util.TestObserver

class Twitter4jSearcherSpec extends Twitter4jSearcherSpecHelpers {
  "observable" - {
    "return tweets in pages" in new TwitterFixture {
      val searchTerm = "searchy-search"
      val maxCount = 5

      val query1 = new Query(searchTerm).count(maxCount)
      val statuses1 = mockStatuses(5)
      val result1 = mockQueryResult(Some(123), statuses1)
      when(twitter.search(query1)) thenReturn result1

      val query2 = new Query(searchTerm).count(maxCount).sinceId(123)
      val statuses2 = mockStatuses(4)
      val result2 = mockQueryResult(Some(456), statuses2)
      when(twitter.search(query2)) thenReturn result2

      val observable = search.observable(searchTerm, None, maxCount)

      val resultF = observable.take(2).toListL.runAsync
      resultF.futureValue shouldBe Seq(
        statuses1.sortBy(_.getCreatedAt),
        statuses2.sortBy(_.getCreatedAt)
      )
    }

    "continue on error" in new TwitterFixture {
      val searchTerm = "ai! katsu!"
      val maxCount = 10

      // First query returns success
      val query1 = new Query(searchTerm).count(maxCount)
      val statuses1 = mockStatuses(5)
      val result1 = mockQueryResult(Some(5), statuses1)
      when(twitter.search(query1)).thenReturn(result1)

      // Second query fails once and then succeeds the second try
      val query2 = new Query(searchTerm).count(maxCount).sinceId(5)
      val statuses2 = mockStatuses(2)
      val result2 = mockQueryResult(Some(7), statuses2)
      when(twitter.search(query2))
        .thenThrow(new RuntimeException("Oh no!!"))
        .thenReturn(result2)

      val observable = search.observable(searchTerm, None, maxCount)

      val resultF = observable.take(3).toListL.runAsync
      resultF.futureValue shouldBe Seq(
        statuses1.sortBy(_.getCreatedAt),
        Seq.empty,
        statuses2.sortBy(_.getCreatedAt)
      )

      verify(twitter).search(query1)
      verify(twitter, times(2)).search(query2)
      verifyNoMoreInteractions(twitter)
    }
  }
}

trait Twitter4jSearcherSpecHelpers extends FreeSpec
  with ScalaFutures with MockitoSugar with PatienceConfiguration {

  implicit override val patienceConfig = PatienceConfig(
    timeout = Span(5, Seconds), interval = Span(100, Millis)
  )

  trait TwitterFixture {
    implicit val scheduler = Scheduler.Implicits.global
    val paginationType = TwitterSearcher.Chronological
    val twitter = mock[Twitter]
    lazy val search = TwitterSearcher(twitter, paginationType)
  }

  def mockQueryResult(maxId: Option[Long], tweets: Seq[Status]): QueryResult = {
    val result = mock[QueryResult]
    when(result.getTweets) thenReturn tweets.asJava
    maxId.foreach(when(result.getMaxId) thenReturn _)
    result
  }

  def mockStatus(): Status = {
    val status = mock[Status]
    when(status.getCreatedAt) thenReturn (new Date(Random.nextInt.abs.toLong * 1000))
    status
  }
  def mockStatuses(count: Int): Seq[Status] = (1 to count).map(_ => mockStatus())
}

