package com.github.walfie.granblue.raidfinder.client

import com.github.walfie.granblue.raidfinder.protocol._
import scala.scalajs.js.JSApp
import org.scalajs.dom
import scala.scalajs.js
import js.typedarray.TypedArrayBufferOps.bufferOps
import js.typedarray._
import js.ArrayOps
import js.JSConverters._
import java.nio.ByteBuffer
import org.scalajs.dom.raw.Blob

object Application extends JSApp {
  def main(): Unit = {
    val msg = RequestMessage().withRaidBossesMessage(RaidBossesRequest())

    val socket = new dom.WebSocket("ws://localhost:9000/ws/raids")
    socket.binaryType = "arraybuffer"

    val bytes = {
      TypedArrayBuffer.wrap(new Int8Array(msg.toByteArray.toJSArray)).arrayBuffer
    }

    socket.onopen = { e: dom.Event =>
      socket.send(bytes)
    }

    socket.onmessage = { e: dom.MessageEvent =>
      val data = e.data match {
        case buffer: ArrayBuffer =>
          new Int8Array(buffer).toArray
      }
      val message = ResponseMessage.parseFrom(data)

      import ResponseMessage.Data._
      message.data match {
        case RaidBossesMessage(r) =>
          r.raidBosses.foreach { rb =>
            println(rb.bossName)
          }
      }
    }
  }
}

