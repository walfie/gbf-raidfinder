package com.github.walfie.granblue.raidfinder

import com.github.walfie.granblue.raidfinder.util.{BlockingIO, ObservableUtil}
import monix.eval.Task
import monix.reactive._
import scala.collection.JavaConverters._
import scala.concurrent.duration._
import scala.concurrent.Future
import twitter4j._

class TwitterSearch(twitter: Twitter) {
  type SinceId = Long

  /**
    * Create an observable that fetches pages of tweets. On error, returns
    * an empty page and the next attempt will retry the previous request.
    */
  def observable(searchTerm: String, maxCount: Int): Observable[Seq[Status]] = {
    type State = Option[SinceId]
    type Action = Seq[Status]
    ObservableUtil.fromAsyncStateAction[State, Action](None) { sinceId: State =>
      Task.fromFuture(nextPage(sinceId, searchTerm, maxCount))
        .onErrorHandle { error: Throwable =>
          System.err.println(error) // TODO: Better handling?
          Seq.empty[Status] -> sinceId
        }
    }
  }

  private def nextPage(
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

