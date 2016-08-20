package com.github.walfie.granblue.raidfinder.server.util

import akka.stream._
import akka.stream.scaladsl._
import com.github.walfie.granblue.raidfinder.protocol._
import com.trueaccord.scalapb.json.JsonFormat
import play.api.http.websocket._
import play.api.libs.streams._
import play.api.mvc.WebSocket.MessageFlowTransformer
import scala.util.control.NonFatal

object MessageFlowTransformerUtil {
  // TODO: Handle errors
  implicit val protobufJsonMessageFlowTransformer: MessageFlowTransformer[RequestMessage, ResponseMessage] = {
    MessageFlowTransformer.stringMessageFlowTransformer.map(
      JsonFormat.fromJsonString[RequestMessage],
      JsonFormat.toJsonString(_)
    )
  }
}

