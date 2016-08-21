package walfie.gbf.raidfinder.server.util

import akka.stream._
import akka.stream.scaladsl._
import com.trueaccord.scalapb.json.JsonFormat
import play.api.http.websocket._
import play.api.libs.streams._
import play.api.mvc.WebSocket.MessageFlowTransformer
import scala.util.control.NonFatal
import scala.util.Try
import walfie.gbf.raidfinder.protocol._

object MessageFlowTransformerUtil {
  private type ProtobufMessageFlowTransformer = MessageFlowTransformer[RequestMessage, ResponseMessage]

  // Throwing a WebSocketCloseException doesn't seem to actually propagate the
  // close reason to the client, despite what the ScalaDoc page says.
  // https://www.playframework.com/documentation/2.5.x/api/scala/index.html#play.api.http.websocket.WebSocketCloseException
  private val inputError = ErrorResponse(message = "Invalid input")
  private def closeWebsocket(binary: Boolean): WebSocketCloseException = {
    val reason = if (binary) new String(inputError.toByteArray) else JsonFormat.toJsonString(inputError)
    val closeMessage = CloseMessage(Some(CloseCodes.InconsistentData), reason)
    WebSocketCloseException(closeMessage)
  }

  implicit val protobufJsonMessageFlowTransformer: ProtobufMessageFlowTransformer = {
    MessageFlowTransformer.stringMessageFlowTransformer.map(
      s => Try(JsonFormat.fromJsonString[RequestMessage](s))
        .getOrElse(throw closeWebsocket(binary = false)),
      JsonFormat.toJsonString(_)
    )
  }

  implicit val protobufBinaryMessageFlowTransformer: ProtobufMessageFlowTransformer = {
    MessageFlowTransformer.byteArrayMessageFlowTransformer.map(
      RequestMessage.validate(_).getOrElse(throw closeWebsocket(binary = true)),
      _.toByteArray
    )
  }
}

