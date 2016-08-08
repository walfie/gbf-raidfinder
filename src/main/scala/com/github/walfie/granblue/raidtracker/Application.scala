package com.github.walfie.granblue.raidtracker

import akka.actor._
import akka.cluster.pubsub.DistributedPubSub
import akka.cluster.pubsub.DistributedPubSubMediator.{Subscribe, SubscribeAck}
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import com.github.walfie.granblue.raidtracker.actor._
import scala.io.StdIn

// TODO: Make this not so bad
object Application {
  import scala.concurrent.ExecutionContext.Implicits.global

  def createRoutes(
    system: ActorSystem, raidPoller: ActorRef, pubSubMediator: ActorRef
  ) = path("ws") {
    handleWebSocketMessages(WebsocketFlow.newSubscriber(system, raidPoller, pubSubMediator))
  } ~ pathPrefix("") {
    encodeResponse(getFromResourceDirectory("static"))
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
}

