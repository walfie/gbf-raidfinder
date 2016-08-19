package com.github.walfie.granblue.raidfinder.server

import akka.actor._
import akka.stream.ActorMaterializer
import com.github.walfie.granblue.raidfinder
import com.github.walfie.granblue.raidfinder.RaidFinder
import monix.execution.Scheduler.Implicits.global
import play.api.BuiltInComponents
import play.api.http.DefaultHttpErrorHandler
import play.api.mvc._
import play.api.routing.Router
import play.api.routing.sird._
import play.core.server._
import play.core.server.NettyServerComponents
import scala.concurrent.Future
import scala.util.control.NonFatal

object Application {
  def main(args: Array[String]): Unit = {
    val raidFinder = RaidFinder.default()

    val components = new Components(raidFinder)
    val server = components.server

    waitForStopEvent()
    server.stop()
  }

  /** Temporary thing to allow stopping the application without killing SBT */
  def waitForStopEvent(): Unit = {
    println("Application started. Press RETURN to stop.")
    scala.io.StdIn.readLine()
    println("Stopping application.")
  }
}

