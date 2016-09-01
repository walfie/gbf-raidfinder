package walfie.gbf.raidfinder.client.views

import com.thoughtworks.binding
import com.thoughtworks.binding.Binding
import com.thoughtworks.binding.Binding._
import org.scalajs.dom
import org.scalajs.dom.raw._
import scala.scalajs.js
import walfie.gbf.raidfinder.client._
import walfie.gbf.raidfinder.client.syntax.{ElementOps, EventOps, HTMLElementOps}
import walfie.gbf.raidfinder.protocol._

object Dialog {
  @binding.dom
  def element(client: RaidFinderClient): Binding[HTMLElement] = {
    val dialog = dom.document.createElement("dialog").asInstanceOf[HTMLElement]
    dialog.classList.add("mdl-dialog")
    dialog.classList.add("gbfrf-dialog")
    val dynamicDialog = dialog.asInstanceOf[js.Dynamic]

    if (js.isUndefined(dynamicDialog.showModal)) {
      js.Dynamic.global.dialogPolyfill.registerDialog(dialog)
    }

    val closeModal = { (e: Event) => dynamicDialog.close(); () }

    val inner =
      <div class="gbfrf-dialog__container mdl-layout mdl-js-layout mdl-layout--fixed-header mdl-layout--fixed-tabs">
        { dialogHeader(onClose = closeModal).bind }
        { BossSelectorDialog.content(client, closeModal).bind }
        { SettingsMenu.content(client).bind }
        <hr style="margin: 0;"/>
        { dialogFooter(onCancel = closeModal).bind }
      </div>

    dialog.appendChild(inner)
    dialog.mdl
  }

  @binding.dom
  def dialogHeader(onClose: Event => Unit): Binding[HTMLElement] = {
    <header class="mdl-layout__header">
      <!-- // TODO: Handle tabs -->
      <div class="mdl-layout__header-row gbfrf-column__header-row">
        <a href="#gbfrf-dialog__follow" class="mdl-layout__tab is-active">Follow</a>
        <a href="#gbfrf-dialog__settings" class="mdl-layout__tab">Settings</a>
        <div class="mdl-layout-spacer"></div>
        <div class="mdl-button mdl-js-button mdl-button--icon material-icons js-close-dialog" onclick={ onClose }>
          <i class="material-icons">clear</i>
        </div>
      </div>
    </header>
  }

  @binding.dom
  def dialogFooter(onCancel: Event => Unit): Binding[HTMLDivElement] = {
    <div class="mdl-dialog__actions">
      <button type="button" class="mdl-button" onclick={ onCancel }>Cancel</button>
    </div>
  }

}

