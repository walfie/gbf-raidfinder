package com.github.walfie.granblue.raidfinder.domain

import akka.actor._
import akka.stream.ActorMaterializer
import com.github.walfie.granblue.raidfinder.flow._
import twitter4j.TwitterFactory

object Application {
  def main(args: Array[String]): Unit = {
    implicit val system = ActorSystem("granblue-raid-finder")
    implicit val materializer = ActorMaterializer()
    import scala.concurrent.ExecutionContext.Implicits.global

    import akka.NotUsed
    import akka.stream.ClosedShape
    import akka.stream.scaladsl._

    val raidInfoCache = RaidInfoCache.default
    val tweetSource: Source[Seq[twitter4j.Status], NotUsed] =
      TwitterSearch.defaultPaginatedSource()

    tweetSource
      .map(_.flatMap(StatusParser.parseStatus))
      .runForeach(raidInfoCache.put)

    println("Application started. Press RETURN to stop.")
    scala.io.StdIn.readLine()
    println("Stopping application.")
    system.terminate()
  }
}

