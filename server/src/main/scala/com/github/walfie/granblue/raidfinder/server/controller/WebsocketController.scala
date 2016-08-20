package com.github.walfie.granblue.raidfinder.server.controller

import akka.actor._
import akka.stream.Materializer
import akka.stream.scaladsl.Flow
import com.github.walfie.granblue.raidfinder.domain._
import com.github.walfie.granblue.raidfinder.protocol._
import com.github.walfie.granblue.raidfinder.RaidFinder
import com.github.walfie.granblue.raidfinder.server.actor.WebsocketRaidsHandler
import com.github.walfie.granblue.raidfinder.server.util.MessageFlowTransformerUtil
import play.api.http.websocket.Message
import play.api.libs.streams._
import play.api.mvc._
import play.api.mvc.WebSocket.MessageFlowTransformer
import scala.concurrent.Future

class WebsocketController(
  raidFinder: RaidFinder
)(implicit system: ActorSystem, materializer: Materializer) extends Controller {
  private val jsonTransformer = MessageFlowTransformerUtil.protobufJsonMessageFlowTransformer
  private val binaryTransformer = MessageFlowTransformerUtil.protobufBinaryMessageFlowTransformer
  private val defaultTransformer = jsonTransformer

  /**
    * Open a websocket channel, communicating in either binary or JSON protobuf.
    * Accepts subprotocols "binary" and "json" (default to "json" if subprotocol unspecified).
    * If an unknown subprotocol is specified, return status code 400.
    */
  def raids = WebSocket { request =>
    // Subprotocols can either be comma-separated in the same header value,
    // or specified across different header values.
    val requestedProtocols = for {
      headerValue <- request.headers.getAll("Sec-WebSocket-Protocol")
      value <- headerValue.split(",")
    } yield value.trim

    val transformerOpt: Option[MessageFlowTransformer[RequestMessage, ResponseMessage]] =
      if (requestedProtocols.isEmpty) {
        Some(defaultTransformer)
      } else requestedProtocols.collectFirst {
        case "binary" => binaryTransformer
        case "json" => jsonTransformer
      }

    val result: Either[Result, Flow[Message, Message, _]] = transformerOpt match {
      case Some(transformer) => Right {
        val flow = ActorFlow.actorRef(out => WebsocketRaidsHandler.props(out, raidFinder))
        transformer.transform(flow)
      }
      case None => Left {
        val unsupportedProtocols = requestedProtocols.mkString("[", ", ", "]")
        Results.BadRequest("Unsupported websocket subprotocols " + unsupportedProtocols)
      }
    }

    Future.successful(result)
  }
}

