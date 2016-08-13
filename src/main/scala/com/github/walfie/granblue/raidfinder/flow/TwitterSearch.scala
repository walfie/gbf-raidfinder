package com.github.walfie.granblue.raidfinder.flow

import akka.NotUsed
import akka.stream.scaladsl._
import com.github.walfie.granblue.raidfinder.util.BlockingIO
import scala.collection.JavaConverters._
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext
import twitter4j._

object TwitterSearch {
  private type SinceId = Long

  /** The maximum number of tweets that can be returned in a single search request */
  val MaxCount = 100

  /** Japanese Granblue tweets always start with this text */
  val DefaultSearchTerm = "参加者募集！参戦ID："

  /** Twitter API's max is 180 queries in 15 mins (average 1 query in 5 seconds) */
  val DefaultPollingInterval = 10.seconds

  /** Create a stream source that fetches pages of tweets periodically */
  def paginatedSource(
    twitter:         Twitter,
    searchTerm:      String,
    maxCount:        Int,
    pollingInterval: FiniteDuration
  )(implicit ec: ExecutionContext): Source[Seq[Status], NotUsed] = {
    val tickSource = Source.tick(0.seconds, pollingInterval, NotUsed)

    // Source that maintains pagination state between search requests
    val tweetSource = Source.unfoldAsync(None: Option[SinceId]) { sinceId: Option[SinceId] =>
      val query = new Query(searchTerm).count(maxCount)
      sinceId.foreach(query.setSinceId)

      BlockingIO.future {
        val queryResult = twitter.search(query)
        Some(Option(queryResult.getMaxId) -> queryResult.getTweets.asScala)
      }.recover {
        // On failure, reuse the last known `sinceId` so the stream doesn't terminate.
        case e: Throwable =>
          System.err.println(e) // TODO: Better logging
          Some(sinceId -> Seq.empty)
      }
    }

    // Force tweetSource to run at the same rate as tickSource
    tweetSource.zipWith(tickSource)((tweets, _) => tweets)
  }

  /** `paginatedSource` with default arguments, using a `twitter4j.Twitter` singleton */
  def defaultPaginatedSource(implicit ec: ExecutionContext): Source[Seq[Status], NotUsed] =
    paginatedSource(TwitterFactory.getSingleton, DefaultSearchTerm, MaxCount, DefaultPollingInterval)
}

