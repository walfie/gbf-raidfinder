package com.github.walfie.granblue.raidfinder

import com.github.walfie.granblue.raidfinder.util.{BlockingIO, ObservableUtil}
import monix.eval.Task
import monix.reactive._
import scala.collection.JavaConverters._
import scala.concurrent.duration._
import scala.concurrent.Future
import twitter4j._

trait TwitterSearch {
  import TwitterSearch.SinceId

  def observable(searchTerm: String, maxCount: Int): Observable[Seq[Status]]

  def nextPage(
    sinceId:    Option[Long],
    searchTerm: String,
    maxCount:   Int
  ): Future[(Seq[Status], Option[SinceId])]
}

object TwitterSearch {
  type SinceId = Long

  def apply(twitter: Twitter): Twitter4jSearch = new Twitter4jSearch(twitter)
}

class Twitter4jSearch(twitter: Twitter) extends TwitterSearch {
  import TwitterSearch.SinceId

  /**
    * Create an observable that fetches pages of tweets. On error, returns
    * an empty page and the next attempt will retry the previous request.
    */
  def observable(searchTerm: String, maxCount: Int): Observable[Seq[Status]] = {
    ObservableUtil.fromAsyncStateAction[Option[SinceId], Seq[Status]](None) { sinceId =>
      Task.fromFuture(nextPage(sinceId, searchTerm, maxCount))
        .onErrorHandle { error: Throwable =>
          System.err.println(error) // TODO: Better handling?
          Seq.empty[Status] -> sinceId
        }
    }
  }

  def nextPage(
    sinceId:    Option[SinceId],
    searchTerm: String,
    maxCount:   Int
  ): Future[(Seq[Status], Option[SinceId])] = {
    val query = new Query(searchTerm).count(maxCount)
    sinceId.foreach(query.setSinceId)

    BlockingIO.future {
      val queryResult = twitter.search(query)
      queryResult.getTweets.asScala -> Option(queryResult.getMaxId)
    }
  }
}

