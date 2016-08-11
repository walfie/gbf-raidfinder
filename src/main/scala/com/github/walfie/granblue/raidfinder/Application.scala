package com.github.walfie.granblue.raidfinder.domain

import akka.actor._
import akka.NotUsed
import akka.stream.scaladsl._
import akka.stream.ActorMaterializer
import com.github.walfie.granblue.raidfinder.flow._
import scala.concurrent.duration._
import twitter4j.TwitterFactory

object Application {
  def main(args: Array[String]): Unit = {
    implicit val system = ActorSystem("granblue-raid-finder")
    implicit val materializer = ActorMaterializer()
    import scala.concurrent.ExecutionContext.Implicits.global

    val raidInfoCache = RaidInfoCache.default
    val raidInfoSource: Source[Seq[RaidInfo], NotUsed] =
      TwitterSearch.defaultPaginatedSource()
        .map(_.flatMap(StatusParser.parseStatus))

    raidInfoSource
      .alsoTo(Sink.foreach(println))
      .runForeach(raidInfoCache.put)

    RaidInfoCache.cacheEvictionScheduler(
      cache = raidInfoCache, ttl = 3.hours, tickInterval = 30.seconds
    ).run()

    println("Application started. Press RETURN to stop.")
    scala.io.StdIn.readLine()
    println("Stopping application.")
    system.terminate()
  }
}

