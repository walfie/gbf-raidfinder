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

trait WebSocketClient {
  def send(request: Request): Unit
  def close(): Unit
  def setSubscriber(subscriber: Option[WebSocketSubscriber]): Unit
}

trait WebSocketSubscriber {
  def onWebSocketMessage(message: Response): Unit
  def onWebSocketOpen(): Unit
  def onWebSocketClose(): Unit
}

class BinaryProtobufWebSocketClient(websocketUrl: String) extends WebSocketClient {
  private var subscriber: Option[WebSocketSubscriber] = None
  private var websocketIsOpen = false
  private var websocketSendQueue = js.Array[ArrayBuffer]()

  private def connectWebSocket(): dom.WebSocket = {
    val ws = new dom.WebSocket(websocketUrl, js.Array("binary"))

    ws.binaryType = "arraybuffer"

    ws.onopen = { _: dom.Event =>
      websocketIsOpen = true
      websocketSendQueue.foreach(ws.send)
      websocketSendQueue = js.Array()
      subscriber.foreach(_.onWebSocketOpen())
    }

    ws.onmessage = { e: dom.MessageEvent =>
      val data = e.data match {
        case buffer: ArrayBuffer => new Int8Array(buffer).toArray
      }
      val parsedMessage = ResponseMessage.validate(data)

      for {
        sub <- subscriber
        message <- parsedMessage.toOption // TODO: Log error
        response <- message.toResponse
      } sub.onWebSocketMessage(response)
    }

    ws.onclose = { _: dom.CloseEvent =>
      websocketIsOpen = false
      subscriber.foreach(_.onWebSocketClose())
    }

    ws
  }

  private var websocket = connectWebSocket()

  def setSubscriber(newSubscriber: Option[WebSocketSubscriber]): Unit =
    subscriber = newSubscriber

  def send(request: Request): Unit = {
    val messageBytes = request.toMessage.toByteArray.toJSArray
    val buffer = TypedArrayBuffer.wrap(new Int8Array(messageBytes)).arrayBuffer

    if (websocketIsOpen) websocket.send(buffer)
    else websocketSendQueue.push(buffer)
  }

  def close(): Unit = websocket.close()
}

