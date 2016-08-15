package com.github.walfie.granblue.raidfinder

import monix.reactive._

object Application {
  def main(args: Array[String]): Unit = {
    // This is temporary, for testing purposes
    val cancellables = {
      import twitter4j._
      import scala.concurrent.duration._
      import com.github.walfie.granblue.raidfinder.domain._
      import monix.execution.Scheduler.Implicits.global
      val timer = Observable.timerRepeated(0.seconds, 10.seconds, ())
      val twitter = TwitterFactory.getSingleton
      val statuses = TwitterSearch(twitter).observable(
        TwitterSearch.DefaultSearchTerm, None, TwitterSearch.MaxCount
      )

      val raidTweets = statuses
        .zipWith(timer)((statuses, _) => statuses)
        .flatMap(Observable.fromIterable)
        .collect(Function.unlift(StatusParser.parse))
        .map(_.tweet)

      val partitioner = CachedRaidTweetsPartitioner
        .fromUngroupedObservable(raidTweets, 50)

      val bosses = List("Lv60 白虎", "Lv60 朱雀")
      bosses.map { boss =>
        println("Press RETURN to subscribe to " + boss)
        scala.io.StdIn.readLine()
        partitioner.getObservable(boss).foreach(println)
      }
    }

    handleStopEvent()
    cancellables.foreach(_.cancel)
  }

  /** Temporary thing to allow stopping the application without killing SBT */
  def handleStopEvent(): Unit = {
    println("Application started. Press RETURN to stop.")
    scala.io.StdIn.readLine()
    println("Stopping application.")
  }
}

