package com.github.walfie.granblue.raidtracker

import akka.actor._
import com.github.walfie.granblue.raidtracker.actor.RaidPoller

// TODO: Make this not so bad
object Application {
  import scala.concurrent.ExecutionContext.Implicits.global

  def main(args: Array[String]): Unit = {
    val system = ActorSystem()
    system.actorOf(RaidPoller.defaultProps)

    // Temporary way to stop server without killing SBT
    println("Server running. Press RETURN to stop.")
    scala.io.StdIn.readLine()
    println("Stopping server.")
    system.terminate()
  }
}

