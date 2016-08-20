package com.github.walfie.granblue.raidfinder.server.controller

import akka.actor._
import akka.stream.Materializer
import com.github.walfie.granblue.raidfinder.domain._
import com.github.walfie.granblue.raidfinder.protocol._
import com.github.walfie.granblue.raidfinder.RaidFinder
import com.github.walfie.granblue.raidfinder.server.actor.WebsocketRaidsHandler
import com.github.walfie.granblue.raidfinder.server.util.MessageFlowTransformerUtil
import play.api.libs.streams._
import play.api.mvc._
import play.api.mvc.WebSocket.MessageFlowTransformer

class WebsocketController(
  raidFinder: RaidFinder
)(implicit system: ActorSystem, materializer: Materializer) extends Controller {
  implicit val messageFlowTransformer =
    MessageFlowTransformerUtil.protobufJsonMessageFlowTransformer

  def raids = WebSocket.accept[RequestMessage, ResponseMessage] { request =>
    ActorFlow.actorRef(out => WebsocketRaidsHandler.props(out, raidFinder))
  }
}

