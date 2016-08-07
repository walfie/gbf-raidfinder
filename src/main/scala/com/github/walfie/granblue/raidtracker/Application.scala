package com.github.walfie.granblue.raidtracker

// TODO: Make this not so bad
object Application {
  import scala.concurrent.ExecutionContext.Implicits.global

  def main(args: Array[String]): Unit = {
    val tweetSearcher = TweetSearcher.fromSingleton()
    val raidParser = new RaidParserImpl {}

    val f = tweetSearcher.search("参加者募集！参戦ID：", None).map { result: TweetSearchResult =>
      result.tweets.flatMap { tweet =>
        raidParser.parseText(tweet.text)
      }.groupBy(_.bossName).foreach(println)
    }

    import scala.concurrent.Await
    import scala.concurrent.duration._
    Await.result(f, 5.seconds)
  }
}

