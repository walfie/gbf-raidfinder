package com.github.walfie.granblue.raidfinder

import monix.reactive._

object Application {
  def main(args: Array[String]): Unit = {
    val x = {
      import scala.concurrent.duration._
      import com.github.walfie.granblue.raidfinder.domain._
      import monix.execution.Scheduler.Implicits.global
      val timer = Observable.timerRepeated(0.seconds, 10.seconds, ())
      val raidTweets = timer.map { _ =>
        val bossName = scala.util.Random.nextInt(5)
        RaidTweet(
          bossName.toString, "id", bossName, "screenName",
          "profileImage", "text", new java.util.Date()
        )
      }

      val partitioner = CachedRaidTweetsPartitioner
        .fromUngroupedObservable(raidTweets, 50)

      val z = scala.io.StdIn.readLine().trim
      partitioner.getObservable(z).foreach(println)
    }

    scala.io.StdIn.readLine().trim
    x.cancel()

    handleStopEvent()
  }

  /** Temporary thing to allow stopping the application without killing SBT */
  def handleStopEvent(): Unit = {
    println("Application started. Press RETURN to stop.")
    scala.io.StdIn.readLine()
    println("Stopping application.")
  }
}

