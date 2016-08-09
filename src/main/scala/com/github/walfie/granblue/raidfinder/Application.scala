package com.github.walfie.granblue.raidfinder.domain

import akka.actor._
import akka.stream.ActorMaterializer
import com.github.walfie.granblue.raidfinder.flow._
import twitter4j.TwitterFactory

object Application {
  def main(args: Array[String]): Unit = {
    implicit val system = ActorSystem("granblue-raid-finder")
    implicit val materializer = ActorMaterializer()

    import akka.stream.scaladsl.Sink

    val tweetSource = TwitterSearch.defaultPaginatedSource()

    tweetSource
      .mapConcat(_.toVector)
      .collect(Function.unlift(StatusParser.parseStatus))
      .runWith(Sink.foreach(println))

    println("Application started. Press RETURN to stop.")
    scala.io.StdIn.readLine()
    println("Stopping application.")
    system.terminate()
  }
}

