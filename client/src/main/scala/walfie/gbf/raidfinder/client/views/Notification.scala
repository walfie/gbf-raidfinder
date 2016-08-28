package walfie.gbf.raidfinder.client.views

import com.thoughtworks.binding
import com.thoughtworks.binding.Binding
import com.thoughtworks.binding.Binding._
import org.scalajs.dom.raw._
import scala.scalajs.js
import walfie.gbf.raidfinder.client.syntax.HTMLElementOps

trait Notification {
  def binding: Binding[Element]
  def enqueue(message: String): Unit
}

class SnackbarNotification extends Notification {
  @binding.dom
  val binding: Binding[Element] = {
    <div class="mdl-js-snackbar mdl-snackbar">
      <div class="mdl-snackbar__text"></div>
      <button class="mdl-snackbar__action" type="button"></button>
    </div>
  }.mdl

  def enqueue(message: String): Unit = {
    val data = js.Dictionary("message" -> message, "timeout" -> "1000")
    Binding {
      binding.bind.asInstanceOf[js.Dynamic].MaterialSnackbar.showSnackbar(data)
    }
  }
}

