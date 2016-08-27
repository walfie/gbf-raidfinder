package walfie.gbf.raidfinder.client

import java.nio.ByteBuffer
import org.scalajs.dom
import scala.scalajs.js
import walfie.gbf.raidfinder.protocol._
import walfie.gbf.raidfinder.protocol.implicits._

import js.ArrayOps
import js.JSConverters._
import js.typedarray._
import js.typedarray.TypedArrayBufferOps.bufferOps

trait RaidFinderClient {
  def getBosses(): Unit
  def send(request: Request): Unit
  def follow(bossNames: BossName*): Unit
  def close(): Unit
}

class WebSocketRaidFinderClient(
  websocketUrl:    String,
  responseHandler: ResponseHandler
) extends RaidFinderClient {
  private var websocketIsOpen = false
  private var websocketSendQueue = js.Array[ArrayBuffer]()

  private val websocket = new dom.WebSocket(websocketUrl, js.Array("binary"))
  websocket.binaryType = "arraybuffer"

  websocket.onopen = { _: dom.Event =>
    websocketIsOpen = true
    websocketSendQueue.foreach(websocket.send)
    websocketSendQueue = js.Array()
  }

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

    if (websocketIsOpen) websocket.send(buffer)
    else websocketSendQueue.push(buffer)
  }

  def getBosses(): Unit = {
    send(RaidBossesRequest())
  }

  def follow(bossNames: BossName*): Unit = {
    send(SubscribeRequest(bossNames = bossNames))
  }

  def close(): Unit = websocket.close()
}

