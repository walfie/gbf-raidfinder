package com.github.walfie.granblue.raidtracker

import akka.actor._
import akka.cluster.pubsub.DistributedPubSub
import akka.cluster.pubsub.DistributedPubSubMediator.{Subscribe, SubscribeAck}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.ws._
import akka.http.scaladsl.server.Directives._
import akka.NotUsed
import akka.stream._
import akka.stream.scaladsl._
import com.github.walfie.granblue.raidtracker.actor._
import java.util.UUID
import scala.io.StdIn

// TODO: Make this not so bad
object Application {
  import scala.concurrent.ExecutionContext.Implicits.global

  def createRoutes(
    system: ActorSystem, raidPoller: ActorRef, pubSubMediator: ActorRef
  ) = path("ws") {
    handleWebSocketMessages(newSubscriber(system, raidPoller, pubSubMediator))
  } ~ pathPrefix("") {
    encodeResponse(getFromResourceDirectory("static"))
  }

  def newSubscriber(
    system: ActorSystem, raidPoller: ActorRef, pubSubMediator: ActorRef
  ): Flow[Message, Message, NotUsed] = {
    val props = Props(
      new WebsocketHandler(raidPoller, pubSubMediator)
    )

    val subscriber = system.actorOf(props, "raid-subscriber-" + UUID.randomUUID())

    val incomingMessages: Sink[Message, NotUsed] = Flow[Message].map {
      case TextMessage.Strict(text) => // parse json
        subscriber ! WebsocketHandler.Protocol.RaidBossesRequest
      case _ => // Ignore
    }.to(Sink.actorRef[Any](subscriber, PoisonPill))

    val outgoingMessages: Source[Message, NotUsed] = {
      val bufferSize = 100
      Source.actorRef[Any](bufferSize, OverflowStrategy.fail)
        .mapMaterializedValue { out =>
          subscriber ! WebsocketHandler.WebsocketConnected(out)
          NotUsed
        }.map { msg: Any =>
          TextMessage(msg.toString) // TODO: Make this not bad
        }
    }

    Flow.fromSinkAndSource(incomingMessages, outgoingMessages)
  }

  def main(args: Array[String]): Unit = {
    implicit val system = ActorSystem("raid-tracker")
    implicit val materializer = ActorMaterializer()(system)

    val mediator = DistributedPubSub(system).mediator

    val raidPoller = system.actorOf(
      RaidPoller.defaultProps(Some(mediator)), "poller"
    )

    val routes = createRoutes(system, raidPoller, mediator)
    val bindingFuture = Http().bindAndHandle(routes, "localhost", 8080)

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
    import RaidPoller._

    raidPoller ! GetRaidBosses

    /*
    val following = Set("Lv60 ユグドラシル・マグナ")
    following.foreach { raidBoss =>
      // TODO: Use second param for optional group?
      // Subscribe(topic: String, group: Option[String], ref: ActorRef)
      mediator ! Subscribe(raidBoss, self)
      raidPoller ! GetCachedRaids(raidBoss)
    }
    */

    def receive: Receive = {
      case RaidBossesMessage(raidBosses) =>
        println("Known raid bosses:")
        raidBosses.foreach(println)
        raidBosses.foreach(raidBoss => mediator ! Subscribe(raidBoss.name, self))

      case RaidsMessage(bossName, raids) =>
        println("Received raids:")
        raids.foreach(println)
    }
  }
}

