package com.github.walfie.granblue.raidfinder

object Application {
  def main(args: Array[String]): Unit = {
    handleStopEvent()
  }

  /** Temporary thing to allow stopping the application without killing SBT */
  def handleStopEvent(): Unit = {
    println("Application started. Press RETURN to stop.")
    scala.io.StdIn.readLine()
    println("Stopping application.")
  }
}

