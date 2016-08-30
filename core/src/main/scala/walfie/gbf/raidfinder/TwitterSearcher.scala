package walfie.gbf.raidfinder

import monix.eval.Task
import monix.reactive._
import scala.collection.JavaConverters._
import scala.concurrent.duration._
import scala.concurrent.Future
import twitter4j._
import walfie.gbf.raidfinder.TwitterSearcher.PaginationType
import walfie.gbf.raidfinder.util.BlockingIO

trait TwitterSearcher {
  import TwitterSearcher.TweetId

  def observable(
    searchTerm:   String,
    initialTweet: Option[TweetId],
    maxCount:     Int
  ): Observable[Seq[Status]]
}

object TwitterSearcher {
  sealed trait PaginationType
  case object Chronological extends PaginationType
  case object ReverseChronological extends PaginationType

  type TweetId = Long

  /** The maximum number of search results returned from the twitter API is 100 */
  val MaxCount = 100
  val DefaultSearchTerm = "参加者募集！参戦ID： OR \"I need backup!\""

  def apply(twitter: Twitter, paginationType: PaginationType): Twitter4jSearcher =
    new Twitter4jSearcher(twitter, paginationType)
}

class Twitter4jSearcher(
  twitter:        Twitter,
  paginationType: PaginationType
) extends TwitterSearcher {
  import TwitterSearcher._

  /**
    * Create an observable that fetches pages of tweets. On error, returns
    * an empty page and the next attempt will retry the previous request.
    */
  def observable(searchTerm: String, initialTweet: Option[TweetId], maxCount: Int): Observable[Seq[Status]] = {
    Observable.fromAsyncStateAction[Option[TweetId], Seq[Status]] { tweetId =>
      val query = new Query(searchTerm).count(maxCount)

      Task.fromFuture(searchFunction(query, tweetId, maxCount))
        .onErrorHandle { error: Throwable =>
          System.err.println(error) // TODO: Better handling?
          Seq.empty[Status] -> tweetId
        }
    }(None)
  }

  private val searchFunction = paginationType match {
    case Chronological => searchChronological _
    case ReverseChronological => searchReverseChronological _
  }

  private def searchChronological(
    query:        Query,
    initialTweet: Option[TweetId],
    maxCount:     Int
  ): Future[(Seq[Status], Option[TweetId])] = {
    initialTweet.foreach(query.setSinceId)

    BlockingIO.future {
      val queryResult = twitter.search(query)
      val tweetsEarliestFirst = queryResult.getTweets.asScala.sortBy(_.getCreatedAt) // Earliest first
      tweetsEarliestFirst -> Option(queryResult.getMaxId)
    }
  }

  private def searchReverseChronological(
    query:        Query,
    initialTweet: Option[TweetId],
    maxCount:     Int
  ): Future[(Seq[Status], Option[TweetId])] = {
    initialTweet.foreach(query.setMaxId)

    BlockingIO.future {
      val queryResult = twitter.search(query)
      val tweetsLatestFirst = queryResult.getTweets.asScala.sortBy(-_.getCreatedAt.getTime) // Latest first
      tweetsLatestFirst -> tweetsLatestFirst.lastOption.map(_.getId) // queryResult.getSinceId returns 0
    }
  }
}

