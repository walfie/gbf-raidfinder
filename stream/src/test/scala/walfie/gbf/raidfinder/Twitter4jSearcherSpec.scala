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
    "return tweets in pages" - {
      "chronologically" in new TwitterFixture(TwitterSearcher.Chronological) {
        val searchTerm = "searchy-search"
        val maxCount = 5

        val query1 = new Query(searchTerm).count(maxCount)
        val statuses1 = mockStatuses(5)
        val result1 = mockQueryResult(maxId = Some(123), tweets = statuses1)
        when(twitter.search(query1)) thenReturn result1

        val query2 = new Query(searchTerm).count(maxCount).sinceId(123)
        val statuses2 = mockStatuses(4)
        val result2 = mockQueryResult(maxId = Some(456), tweets = statuses2)
        when(twitter.search(query2)) thenReturn result2

        val observable = search.observable(searchTerm, None, maxCount)

        val resultF = observable.take(2).toListL.runAsync
        resultF.futureValue shouldBe Seq(
          statuses1.sortBy(_.getCreatedAt),
          statuses2.sortBy(_.getCreatedAt)
        )
      }

      "reverse chronologically" in new TwitterFixture(TwitterSearcher.ReverseChronological) {
        val searchTerm = "searchy-search-in-reverse"
        val maxCount = 5

        val query1 = new Query(searchTerm).count(maxCount)
        val statuses1 = (6L to 10L).map(mockStatus)
        val result1 = mockQueryResult(tweets = statuses1)
        when(twitter.search(query1)) thenReturn result1

        val query2 = new Query(searchTerm).count(maxCount).maxId(6)
        val statuses2 = (1L to 5L).map(mockStatus)
        val result2 = mockQueryResult(tweets = statuses2)
        when(twitter.search(query2)) thenReturn result2

        val observable = search.observable(searchTerm, None, maxCount)

        val resultF = observable.take(2).toListL.runAsync
        resultF.futureValue shouldBe Seq(
          statuses1.sortBy(-_.getCreatedAt.getTime),
          statuses2.sortBy(-_.getCreatedAt.getTime)
        )
      }
    }

    "continue on error" in new TwitterFixture(TwitterSearcher.Chronological) {
      val searchTerm = "ai! katsu!"
      val maxCount = 10

      // First query returns success
      val query1 = new Query(searchTerm).count(maxCount)
      val statuses1 = mockStatuses(5)
      val result1 = mockQueryResult(maxId = Some(5), tweets = statuses1)
      when(twitter.search(query1)).thenReturn(result1)

      // Second query fails once and then succeeds the second try
      val query2 = new Query(searchTerm).count(maxCount).sinceId(5)
      val statuses2 = mockStatuses(2)
      val result2 = mockQueryResult(maxId = Some(7), tweets = statuses2)
      when(twitter.search(query2))
        .thenThrow(new RuntimeException("Don't worry, this is supposed to happen."))
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

  class TwitterFixture(paginationType: TwitterSearcher.PaginationType) {
    implicit val scheduler = Scheduler.Implicits.global
    val twitter = mock[Twitter]
    lazy val search = TwitterSearcher(twitter, paginationType)
  }

  def mockQueryResult(
    maxId:  Option[Long] = None,
    tweets: Seq[Status]
  ): QueryResult = {
    val result = mock[QueryResult]
    when(result.getTweets) thenReturn tweets.asJava
    maxId.foreach(when(result.getMaxId) thenReturn _)
    result
  }

  def mockStatus(tweetId: Long): Status = {
    val status = mock[Status]
    when(status.getId) thenReturn tweetId
    when(status.getCreatedAt) thenReturn (new Date(tweetId))
    when(status.toString) thenReturn s"Status($tweetId)"
    status
  }
  def mockStatuses(count: Int): Seq[Status] = (1 to count).map(i => mockStatus(i))
}

