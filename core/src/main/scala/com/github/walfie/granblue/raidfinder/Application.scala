package com.github.walfie.granblue.raidfinder

import monix.reactive._

object Application {
  def main(args: Array[String]): Unit = {
    val cancellable = {
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

      partitioner.getObservable("Lv60 白虎").foreach(println)
    }

    handleStopEvent()
    cancellable.cancel()
  }

  /** Temporary thing to allow stopping the application without killing SBT */
  def handleStopEvent(): Unit = {
    println("Application started. Press RETURN to stop.")
    scala.io.StdIn.readLine()
    println("Stopping application.")
  }
}

