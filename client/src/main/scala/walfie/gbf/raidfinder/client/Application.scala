package walfie.gbf.raidfinder.client

import scala.scalajs.js
import walfie.gbf.raidfinder.protocol._

import js.JSApp

object Application extends JSApp {
  def main(): Unit = {
    import scala.concurrent.ExecutionContext.Implicits.global

    val url = "ws://localhost:9000/ws/raids"
    val client = new WebSocketRaidFinderClient(url, new DefaultResponseHandler)
    client.send(RaidBossesRequest())
    client.send(SubscribeRequest(bossNames = Seq("Lv60 ユグドラシル・マグナ")))
  }
}

