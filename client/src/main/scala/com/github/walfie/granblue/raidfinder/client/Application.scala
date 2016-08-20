package com.github.walfie.granblue.raidfinder.client

import com.github.walfie.granblue.raidfinder.protocol._
import com.github.walfie.granblue.raidfinder.protocol.implicits._
import java.nio.ByteBuffer
import org.scalajs.dom
import scala.scalajs.js

import js.JSConverters._
import js.typedarray._
import js.typedarray.TypedArrayBufferOps.bufferOps
import js.{ArrayOps, JSApp}

object Application extends JSApp {
  def main(): Unit = {
    val msg = RequestMessage().withRaidBossesMessage(RaidBossesRequest())

    val socket = new dom.WebSocket("ws://localhost:9000/ws/raids", js.Array("binary"))
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
      val message = ResponseMessage.parseFrom(data) // TODO: validate

      message.toResponse.foreach {
        case r: RaidBossesResponse =>
          r.raidBosses.foreach { rb =>
            println(rb.bossName)
          }
      }
    }
  }
}

