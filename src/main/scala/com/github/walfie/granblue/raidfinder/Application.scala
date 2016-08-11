package com.github.walfie.granblue.raidfinder.domain

import akka.actor._
import akka.NotUsed
import akka.stream.scaladsl._
import akka.stream.{ActorMaterializer, ClosedShape}
import com.github.walfie.granblue.raidfinder.flow._
import scala.concurrent.duration._
import twitter4j.TwitterFactory

object Application {
  def main(args: Array[String]): Unit = {
    implicit val system = ActorSystem("granblue-raid-finder")
    implicit val materializer = ActorMaterializer()
    import scala.concurrent.ExecutionContext.Implicits.global

    val raidInfoCache = RaidInfoCache.default
    val scheduler = RaidInfoCache.defaultCacheEvictionScheduler(raidInfoCache)

    val graph = RaidFinderGraph.default(raidInfoCache)
    graph.run()

    println("Application started. Press RETURN to stop.")
    scala.io.StdIn.readLine()
    println("Stopping application.")
    system.terminate()
  }
}

