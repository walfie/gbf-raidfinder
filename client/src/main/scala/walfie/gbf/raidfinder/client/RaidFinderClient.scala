package walfie.gbf.raidfinder.client

import java.nio.ByteBuffer
import org.scalajs.dom
import scala.concurrent.{ExecutionContext, Future, Promise}
import scala.scalajs.js
import walfie.gbf.raidfinder.protocol._
import walfie.gbf.raidfinder.protocol.implicits._

import js.ArrayOps
import js.JSConverters._
import js.typedarray._
import js.typedarray.TypedArrayBufferOps.bufferOps

trait RaidFinderClient {
  def send(request: Request): Unit
  def close(): Unit
}

class WebSocketRaidFinderClient(
  websocketUrl:    String,
  responseHandler: ResponseHandler
)(implicit ec: ExecutionContext) {
  private val websocketP = Promise[dom.WebSocket]
  private val websocketF = websocketP.future

  private val websocket = new dom.WebSocket(websocketUrl, js.Array("binary"))
  websocket.binaryType = "arraybuffer"

  websocket.onopen = (_: dom.Event) => websocketP.success(websocket)

  websocket.onmessage = { e: dom.MessageEvent =>
    val data = e.data match {
      case buffer: ArrayBuffer => new Int8Array(buffer).toArray
    }
    val message = ResponseMessage.parseFrom(data) // TODO: validate

    message.toResponse.foreach(responseHandler.handleResponse)
  }

  def send(request: Request): Unit = {
    val messageBytes = request.toMessage.toByteArray.toJSArray
    val buffer = TypedArrayBuffer.wrap(new Int8Array(messageBytes)).arrayBuffer
    websocketF.foreach(_.send(buffer))
  }

  def close(): Unit = websocket.close()
}

