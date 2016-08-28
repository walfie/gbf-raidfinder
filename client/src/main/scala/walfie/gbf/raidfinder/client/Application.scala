package walfie.gbf.raidfinder.client

import com.thoughtworks.binding
import com.thoughtworks.binding.Binding._
import org.scalajs.dom
import com.momentjs.Moment
import scala.scalajs.js
import walfie.gbf.raidfinder.protocol._

import dom.raw._
import js.JSApp

object Application extends JSApp {
  def main(): Unit = {
    val url = "ws://localhost:9000/ws/raids"
    val websocket = new BinaryProtobufWebSocketClient(url)
    val client = new WebSocketRaidFinderClient(websocket, dom.window.localStorage)
    client.updateBosses()

    // TODO: Put this somewhere else
    Moment.defineLocale(
      "en-short",
      js.Dictionary(
        "parentLocale" -> "en",
        "relativeTime" -> js.Dictionary(
          "future" -> "in %s",
          "past" -> "%s ago",
          "s" -> "now",
          "ss" -> "%ss",
          "m" -> "1m",
          "mm" -> "%dm",
          "h" -> "1h",
          "hh" -> "%dh",
          "d" -> "1d",
          "dd" -> "%dd",
          "M" -> "1M",
          "MM" -> "%dM",
          "y" -> "1Y",
          "yy" -> "%dY"
        )
      )
    )

    val notification = new views.SnackbarNotification

    // Update currentTime every 30 seconds
    val currentTime: Var[Double] = Var(js.Date.now())
    js.timers.setInterval(30000) {
      client.truncateColumns(50)
      currentTime := js.Date.now()
    }

    binding.dom.render(
      dom.document.body,
      views.MainContent.mainContent(client, notification, currentTime)
    )
  }
}

