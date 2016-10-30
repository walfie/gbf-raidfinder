package walfie.gbf.raidfinder.client

import java.nio.ByteBuffer
import org.scalajs.dom
import scala.scalajs.js
import scala.util.Failure
import walfie.gbf.raidfinder.client.util.time.Duration
import walfie.gbf.raidfinder.protocol._
import walfie.gbf.raidfinder.protocol.syntax._

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
  def onWebSocketReconnect(): Unit
  def onWebSocketClose(): Unit
}

class BinaryProtobufWebSocketClient(
  websocketUrl:         String,
  maxReconnectInterval: Duration
) extends WebSocketClient {
  private var subscriber: Option[WebSocketSubscriber] = None
  private var websocketIsOpen = false
  private var websocketSendQueue = js.Array[ArrayBuffer]()

  /** Like a `foreach` but with a delay between each item */
  private def websocketDelayedDequeue(ws: dom.WebSocket, delay: Duration): Unit = {
    js.timers.setTimeout(delay.milliseconds) {
      if (websocketSendQueue.length > 0) {
        ws.send(websocketSendQueue.shift)
        websocketDelayedDequeue(ws, delay)
      }
    }
  }

  private def connectWebSocket(isReconnect: Boolean): dom.WebSocket = {
    val ws = new dom.WebSocket(websocketUrl, js.Array("binary"))
    val dequeueDelay = Duration(milliseconds = 5) // TODO: Stop hardcoding things

    ws.binaryType = "arraybuffer"

    ws.onopen = { _: dom.Event =>
      websocketIsOpen = true
      websocketDelayedDequeue(ws, dequeueDelay)
      subscriber.foreach { sub =>
        if (isReconnect) sub.onWebSocketReconnect()
        else sub.onWebSocketOpen()
      }
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

      // TODO: Exponential backoff?
      val retryInterval = js.Math.random() * maxReconnectInterval.milliseconds
      js.timers.setTimeout(retryInterval) {
        websocket = Option(connectWebSocket(isReconnect = true))
      }
    }

    ws
  }

  private var websocket: Option[dom.WebSocket] = None

  // Only connect websocket when a subscriber is present
  def setSubscriber(newSubscriber: Option[WebSocketSubscriber]): Unit = {
    if (subscriber.isEmpty && newSubscriber.nonEmpty) {
      subscriber = newSubscriber
      websocket = Option(connectWebSocket(isReconnect = false))
    } else {
      subscriber = newSubscriber
    }
  }

  def send(request: Request): Unit = {
    val messageBytes = request.toMessage.toByteArray.toJSArray
    val buffer = TypedArrayBuffer.wrap(new Int8Array(messageBytes)).arrayBuffer

    if (websocketIsOpen && websocket.nonEmpty) websocket.foreach(_.send(buffer))
    else websocketSendQueue.push(buffer)
  }

  def close(): Unit = websocket.foreach(_.close())
}

