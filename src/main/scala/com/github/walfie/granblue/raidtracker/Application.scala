package com.github.walfie.granblue.raidtracker

import akka.actor._
import com.github.walfie.granblue.raidtracker.actor.RaidPoller

// TODO: Make this not so bad
object Application {
  import scala.concurrent.ExecutionContext.Implicits.global

  def main(args: Array[String]): Unit = {
    val system = ActorSystem()
    system.actorOf(RaidPoller.DefaultProps)
  }
}

