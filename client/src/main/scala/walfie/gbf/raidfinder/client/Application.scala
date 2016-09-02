package walfie.gbf.raidfinder.client

import com.thoughtworks.binding
import com.thoughtworks.binding.Binding._
import org.scalajs.dom
import com.momentjs.Moment
import scala.scalajs.js
import walfie.gbf.raidfinder.protocol._
import walfie.gbf.raidfinder.client.util.time._

import dom.raw._
import js.JSApp

object Application extends JSApp {
  def main(): Unit = {
    val url = "ws://localhost:9000/ws/raids"
    val bossTtl = Duration.hours(6)

    val websocket = new BinaryProtobufWebSocketClient(url)
    val client = new WebSocketRaidFinderClient(
      websocket, dom.window.localStorage, bossTtl, SystemClock
    )
    client.updateBosses()

    Moment.defineLocale("en-short", MomentShortLocale)

    val notification = new views.SnackbarNotification

    val viewState = ViewModel.loadState()

    // Update currentTime every 30 seconds
    val currentTime: Var[Double] = Var(js.Date.now())
    js.timers.setInterval(30000) {
      client.truncateColumns(50)
      if (viewState.relativeTime.get) {
        currentTime := js.Date.now()
      }
    }

    binding.dom.render(
      dom.document.body,
      // TODO: Load ViewState from local storage
      views.MainContent.mainContent(client, ViewModel.loadState(), notification, currentTime)
    )
  }
}

