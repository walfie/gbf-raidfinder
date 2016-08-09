package com.github.walfie.granblue.raidfinder.domain

import akka.actor._
import akka.stream.ActorMaterializer
import com.github.walfie.granblue.raidfinder.flow._
import twitter4j.TwitterFactory

object Application {
  def main(args: Array[String]): Unit = {
    implicit val system = ActorSystem("granblue-raid-finder")
    implicit val materializer = ActorMaterializer()

    import akka.NotUsed
    import akka.stream._
    import akka.stream.scaladsl._
    import scala.concurrent.Future
    import scala.concurrent.duration._

    val tweetSource = TwitterSearch.defaultPaginatedSource()
    val tickSource = Source.tick(0.seconds, 10.seconds, NotUsed)

    // Force the tweetSource to the rate specified by tickSource
    val polling = tweetSource.zipWith(tickSource)((status, _) => status)
    polling.runWith(Sink.foreach(println))

    println("Application started. Press RETURN to stop.")
    scala.io.StdIn.readLine()
    println("Stopping application.")
    system.terminate()
  }
}

