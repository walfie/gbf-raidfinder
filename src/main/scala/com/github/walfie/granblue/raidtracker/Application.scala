package com.github.walfie.granblue.raidtracker

import akka.actor._
import com.github.walfie.granblue.raidtracker.actor.RaidPoller
import akka.cluster.pubsub.DistributedPubSub

// TODO: Make this not so bad
object Application {
  import scala.concurrent.ExecutionContext.Implicits.global

  def main(args: Array[String]): Unit = {
    val system = ActorSystem()

    val mediator = DistributedPubSub(system).mediator

    val raidPoller = system.actorOf(
      RaidPoller.defaultProps(Some(mediator)), "poller"
    )

    val subscriber = system.actorOf(
      Props(new SubscriberActor(raidPoller, mediator)), "subscriber"
    )

    // Temporary way to stop server without killing SBT
    println("Server running. Press RETURN to stop.")
    scala.io.StdIn.readLine()
    println("Stopping server.")
    system.terminate()
  }

  // For temporary debugging
  class SubscriberActor(
    raidPoller: ActorRef, mediator: ActorRef
  ) extends Actor {
    import akka.cluster.pubsub.DistributedPubSubMediator.{Subscribe, SubscribeAck}

    // TODO: Use second param for optional group?
    // Subscribe(topic: String, group: Option[String], ref: ActorRef)
    mediator ! Subscribe("Lv60 ユグドラシル・マグナ", self)

    def receive: Receive = {
      case RaidPoller.RaidsMessage(bossName, raids) =>
        println("Subscriber actor received:")
        raids.foreach(println)
    }
  }
}

