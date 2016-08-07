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
) extends TweetSearcher {
  def search(searchTerm: String, sinceId: Option[Long]): Future[TweetSearchResult] = {
    val query = new Query(searchTerm).count(maxCount)
    sinceId.foreach(query.setSinceId)

    BlockingIO.future(toTweetSearchResult(twitter.search(query)))
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
      images = status.getMediaEntities.map(entity => TwitterImage(entity.getMediaURLHttps)),
      createdAt = status.getCreatedAt
    )

    TweetSearchResult(tweets, Option(queryResult.getMaxId))
  }
}

object TweetSearcher {
  val MaxCount = 100

  val GranblueSource =
    """<a href="http://granbluefantasy.jp/" rel="nofollow">グランブルー ファンタジー</a>"""

  def fromSingleton(
    maxCount: Int = MaxCount,
    filterSource: Option[String] = Some(GranblueSource)
  ): TweetSearcher =
    new TweetSearcherImpl(TwitterFactory.getSingleton, maxCount, filterSource)
}

