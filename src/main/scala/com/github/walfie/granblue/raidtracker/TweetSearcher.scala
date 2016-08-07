package com.github.walfie.granblue.raidtracker

import com.github.walfie.granblue.raidtracker.TweetSearcher._
import com.github.walfie.granblue.raidtracker.util.BlockingIO
import scala.collection.JavaConverters._
import scala.concurrent.{ExecutionContext, Future}
import twitter4j._

trait TweetSearcher {
  def search(searchTerm: String, sinceId: Option[Long]): Future[TweetSearchResult]
}

class TweetSearcherImpl(
  twitter: Twitter, maxCount: Int, filterSource: Option[String]
)(implicit ec: ExecutionContext) extends TweetSearcher {
  def search(searchTerm: String, sinceId: Option[Long]): Future[TweetSearchResult] = {
    val query = new Query(searchTerm).count(maxCount)
    sinceId.foreach(query.setSinceId)

    BlockingIO.future(twitter.search(query)).map(toTweetSearchResult)
  }

  private def toTweetSearchResult(
    queryResult: QueryResult
  ): TweetSearchResult = {
    val tweets = for {
      status <- queryResult.getTweets.asScala
      if filterSource.forall(_ == status.getSource)
    } yield Tweet(
      id = status.getId,
      screenName = status.getUser.getScreenName,
      text = status.getText,
      imageUrls = status.getMediaEntities.map(_.getMediaURLHttps),
      createdAt = status.getCreatedAt
    )

    TweetSearchResult(tweets, queryResult.getMaxId)
  }
}

object TweetSearcher {
  val MaxCount = 100

  val GranblueSource =
    """<a href="http://granbluefantasy.jp/" rel="nofollow">グランブルー ファンタジー</a>"""

  def fromSingleton(
    maxCount: Int = MaxCount,
    filterSource: Option[String] = Some(GranblueSource)
  )(implicit ec: ExecutionContext): TweetSearcher =
    new TweetSearcherImpl(TwitterFactory.getSingleton, maxCount, filterSource)
}

