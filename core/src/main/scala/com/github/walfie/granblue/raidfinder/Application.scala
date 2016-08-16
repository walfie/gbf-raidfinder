package com.github.walfie.granblue.raidfinder

import monix.execution.Scheduler.Implicits.global

object Application {
  def main(args: Array[String]): Unit = {
    val raidFinder = RaidFinder.default()
    val bosses = List(
      "Lv60 ユグドラシル・マグナ",
      "Lv60 リヴァイアサン・マグナ",
      "Lv75 シュヴァリエ・マグナ",
      "Lv75 セレスト・マグナ"
    )

    bosses.foreach { boss =>
      println("Press RETURN to subscribe to " + boss)
      scala.io.StdIn.readLine()
      println(raidFinder.getKnownBosses())
      raidFinder.getRaidTweets(boss).foreach(println)
    }

    handleStopEvent()
    raidFinder.shutdown()
  }

  /** Temporary thing to allow stopping the application without killing SBT */
  def handleStopEvent(): Unit = {
    println("Application started. Press RETURN to stop.")
    scala.io.StdIn.readLine()
    println("Stopping application.")
  }
}

