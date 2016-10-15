package walfie.gbf.raidfinder.client

import com.momentjs.Moment
import com.thoughtworks.binding
import com.thoughtworks.binding.Binding._
import org.scalajs.dom
import org.scalajs.dom.raw._
import scala.scalajs.js
import scala.scalajs.js.annotation._
import walfie.gbf.raidfinder.client.util.time._
import walfie.gbf.raidfinder.client.ViewModel.TimeFormat
import walfie.gbf.raidfinder.protocol._

@JSExport("GbfRaidFinder")
object Application {
  @JSExport
  def init(url: String): Unit = {
    val maxReconnectInterval = Duration.seconds(10)

    val websocket = new BinaryProtobufWebSocketClient(url, maxReconnectInterval)

    val client: RaidFinderClient = new WebSocketRaidFinderClient(
      websocket, dom.window.localStorage, SystemClock
    )

    Moment.defineLocale("en-short", MomentShortLocale)

    val notification = new views.SnackbarNotification

    val viewState = ViewModel.loadState()

    // Update currentTime every 30 seconds
    val currentTime: Var[Double] = Var(js.Date.now())
    js.timers.setInterval(Duration.seconds(30).milliseconds) {
      client.truncateColumns(50)
      currentTime := js.Date.now()
    }

    val div = dom.document.createElement("div")
    div.classList.add("gbfrf-container")
    val mainContent = views.MainContent.mainContent(
      client, ViewModel.loadState(), notification, currentTime, client.isConnected
    )

    binding.dom.render(div, mainContent)
    dom.document.body.appendChild(div)
  }
}

