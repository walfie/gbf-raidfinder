package com.github.walfie.granblue.raidfinder.actor

import akka.actor._
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.ws._
import akka.NotUsed
import akka.stream._
import akka.stream.scaladsl._
import com.github.walfie.granblue.raidfinder.actor.WebsocketHandler.Protocol._
import com.github.walfie.granblue.raidfinder.json._
import java.util.UUID
import play.api.libs.json._

object WebsocketFlow {
  def newSubscriber(
    system: ActorSystem, raidPoller: ActorRef, pubSubMediator: ActorRef
  ): Flow[Message, Message, NotUsed] = {
    val props = Props(
      new WebsocketHandler(raidPoller, pubSubMediator)
    )

    val subscriber = system.actorOf(props, "raid-subscriber-" + UUID.randomUUID())

    val incomingMessages: Sink[Message, NotUsed] = Flow[Message].map {
      case TextMessage.Strict(text) => // TODO: parse json
        parseStringAsJson(text)
          .flatMap(_.validate[WebsocketHandler.Protocol.Request])
          .foreach(subscriber ! _)
      case _ => // Ignore
    }.to(Sink.actorRef[Any](subscriber, PoisonPill))

    val outgoingMessages: Source[Message, NotUsed] = {
      val bufferSize = 100
      Source.actorRef[Response](bufferSize, OverflowStrategy.fail)
        .mapMaterializedValue { out =>
          subscriber ! WebsocketHandler.WebsocketConnected(out)
          NotUsed
        }.map { msg: Response =>
          TextMessage(Json.stringify(Json.toJson(msg)))
        }
    }

    Flow.fromSinkAndSource(incomingMessages, outgoingMessages)
  }
}

