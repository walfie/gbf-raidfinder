package com.github.walfie.granblue.raidfinder

import monix.reactive._

object Application {
  def main(args: Array[String]): Unit = {
    // This is temporary, for testing purposes
    val cancellables = {
      import twitter4j._
      import scala.concurrent.duration._
      import com.github.walfie.granblue.raidfinder.domain._
      import monix.execution.{Cancelable, Scheduler}
      import monix.execution.Scheduler.Implicits.global

      val timer = Observable.timerRepeated(0.seconds, 10.seconds, ())
      val twitter = TwitterFactory.getSingleton
      val statuses = TwitterSearch(twitter).observable(
        TwitterSearch.DefaultSearchTerm, None, TwitterSearch.MaxCount
      )

      val raidInfos = statuses
        .zipWith(timer)((statuses, _) => statuses)
        .flatMap(Observable.fromIterable)
        .collect(Function.unlift(StatusParser.parse))
        .publish

      val (partitioner, partitionerCancelable) = CachedRaidTweetsPartitioner
        .fromUngroupedObservable(raidInfos.map(_.tweet), 50)

      val (knownBosses, knownBossesCancelable) = KnownBossesObserver
        .fromRaidInfoObservable(raidInfos)

      val raidInfosCancelable = raidInfos.connect()

      val cancelable = Cancelable { () =>
        List(
          raidInfosCancelable,
          partitionerCancelable,
          knownBossesCancelable
        ).foreach(_.cancel)
      }

      val bosses = List(
        "Lv60 ユグドラシル・マグナ",
        "Lv60 リヴァイアサン・マグナ",
        "Lv75 シュヴァリエ・マグナ",
        "Lv75 セレスト・マグナ"
      )

      bosses.map { boss =>
        println("Press RETURN to subscribe to " + boss)
        scala.io.StdIn.readLine()
        println(knownBosses.get)
        partitioner.getObservable(boss).foreach(println)
      } :+ cancelable
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

