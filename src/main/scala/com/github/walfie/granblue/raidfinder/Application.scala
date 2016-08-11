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

    val tweetSource = TwitterSearch.defaultPaginatedSource()

    val raidsSource: Source[(RaidBoss, RaidTweet), NotUsed] = tweetSource
      .mapConcat(_.toVector)
      .collect(Function.unlift(StatusParser.parseStatus))

    val raidBossCache = RaidBossCache.default

    RunnableGraph.fromGraph(GraphDSL.create() { implicit builder =>
      import GraphDSL.Implicits._

      // Unzip raidsSource into two streams
      val unzip = builder.add(Unzip[RaidBoss, RaidTweet]())

      raidsSource ~> unzip.in
      unzip.out0 ~> Sink.foreach(raidBossCache.put)
      unzip.out1 ~> Sink.foreach(println)
      ClosedShape
    }).run()

    println("Application started. Press RETURN to stop.")
    scala.io.StdIn.readLine()
    println("Stopping application.")
    system.terminate()
  }
}

