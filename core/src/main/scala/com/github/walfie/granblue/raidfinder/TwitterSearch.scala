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

  def observable(
    searchTerm: String,
    sinceId:    Option[SinceId],
    maxCount:   Int
  ): Observable[Seq[Status]]
}

object TwitterSearch {
  type SinceId = Long

  /** The maximum number of search results returned from the twitter API is 100 */
  val MaxCount = 100
  val DefaultSearchTerm = "参加者募集！参戦ID："

  def apply(twitter: Twitter): Twitter4jSearch = new Twitter4jSearch(twitter)
}

class Twitter4jSearch(twitter: Twitter) extends TwitterSearch {
  import TwitterSearch.SinceId

  /**
    * Create an observable that fetches pages of tweets. On error, returns
    * an empty page and the next attempt will retry the previous request.
    */
  def observable(searchTerm: String, sinceId: Option[SinceId], maxCount: Int): Observable[Seq[Status]] = {
    ObservableUtil.fromAsyncStateAction[Option[SinceId], Seq[Status]](None) { sinceId =>
      Task.fromFuture(search(searchTerm, sinceId, maxCount))
        .onErrorHandle { error: Throwable =>
          System.err.println(error) // TODO: Better handling?
          Seq.empty[Status] -> sinceId
        }
    }
  }

  private def search(
    searchTerm: String,
    sinceId:    Option[SinceId],
    maxCount:   Int
  ): Future[(Seq[Status], Option[SinceId])] = {
    val query = new Query(searchTerm).count(maxCount)
    sinceId.foreach(query.setSinceId)

    // TODO: Refactor this so it doesn't time out on unit tests
    BlockingIO.future {
      val queryResult = twitter.search(query)
      val sortedTweets = queryResult.getTweets.asScala.sortBy(_.getCreatedAt) // Earliest first
      sortedTweets -> Option(queryResult.getMaxId)
    }
  }
}

