package walfie.gbf.raidfinder.client

import com.thoughtworks.binding
import com.thoughtworks.binding.Binding._
import org.scalajs.dom
import org.widok.moment._
import scala.scalajs.js
import walfie.gbf.raidfinder.protocol._

import dom.raw._
import js.JSApp

object Application extends JSApp {
  def main(): Unit = {
    val url = "ws://localhost:9000/ws/raids"
    val handler = new DefaultResponseHandler
    val client = new WebSocketRaidFinderClient(url, handler)
    client.getBosses()
    client.follow("Lv60 ユグドラシル・マグナ", "Lv75 シュヴァリエ・マグナ")

    js.Dynamic.global.moment.updateLocale(
      "en",
      js.Dictionary(
        "relativeTime" -> js.Dictionary(
          "future" -> "in %s",
          "past" -> "%s ago",
          "s" -> "1s",
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

    binding.dom.render(
      dom.document.body,
      views.MainContent.mainContent(handler, client)
    )
  }
}

