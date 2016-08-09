package com.github.walfie.granblue.raidfinder.flow

import akka.NotUsed
import akka.stream.scaladsl._
import com.github.walfie.granblue.raidfinder.util.BlockingIO
import scala.collection.JavaConverters._
import twitter4j._

object TwitterSearch {
  private type SinceId = Long

  val MaxCount = 100
  val DefaultSearchTerm = "参加者募集！参戦ID："

  /** Create a stream Source that fetches pages of tweets */
  def paginatedSource(
    twitter:    Twitter,
    searchTerm: String,
    maxCount:   Int
  ): Source[Seq[Status], NotUsed] = {
    Source.unfoldAsync(None: Option[SinceId]) { sinceId: Option[SinceId] =>
      val query = new Query(searchTerm).count(maxCount)
      sinceId.foreach(query.setSinceId)

      BlockingIO.future {
        val queryResult = twitter.search(query)
        Some(Option(queryResult.getMaxId) -> queryResult.getTweets.asScala)
      }
    }
  }

  def defaultPaginatedSource(): Source[Seq[Status], NotUsed] =
    paginatedSource(TwitterFactory.getSingleton, DefaultSearchTerm, MaxCount)
}

