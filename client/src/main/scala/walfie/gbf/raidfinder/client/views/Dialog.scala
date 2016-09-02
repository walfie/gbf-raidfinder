package walfie.gbf.raidfinder.client.views

import com.thoughtworks.binding
import com.thoughtworks.binding.Binding
import com.thoughtworks.binding.Binding._
import org.scalajs.dom
import org.scalajs.dom.raw._
import scala.scalajs.js
import walfie.gbf.raidfinder.client._
import walfie.gbf.raidfinder.client.syntax.{ElementOps, EventOps, HTMLElementOps, StringOps}
import walfie.gbf.raidfinder.client.ViewModel._
import walfie.gbf.raidfinder.protocol._

object Dialog {
  @binding.dom
  def element(client: RaidFinderClient, viewState: ViewModel.State): Binding[HTMLElement] = {
    val dialog = dom.document.createElement("dialog").asInstanceOf[HTMLElement]
    dialog.classList.add("mdl-dialog")
    dialog.classList.add("gbfrf-dialog")
    val dynamicDialog = dialog.asInstanceOf[js.Dynamic]

    if (js.isUndefined(dynamicDialog.showModal)) {
      js.Dynamic.global.dialogPolyfill.registerDialog(dialog)
    }

    val closeModal = { (e: Event) => dynamicDialog.close(); () }

    val onTabChange = () => ViewModel.persistState(viewState)

    val currentTab = viewState.currentTab

    val inner =
      <div class="gbfrf-dialog__container mdl-layout mdl-js-layout mdl-layout--fixed-header mdl-layout--fixed-tabs">
        { dialogHeader(viewState.currentTab, onClose = closeModal, onTabChange = onTabChange).bind }
        { BossSelectMenu.content(client, closeModal, currentTab, viewState.imageQuality).bind }
        { SettingsMenu.content(client, viewState).bind }
        <hr style="margin: 0;"/>
        { dialogFooter(onCancel = closeModal).bind }
      </div>

    dialog.appendChild(inner)
    dialog.mdl
  }

  @binding.dom
  def dialogHeader(currentTab: Var[ViewModel.DialogTab], onClose: Event => Unit, onTabChange: () => Unit): Binding[HTMLElement] = {
    <header class="mdl-layout__header">
      <div class="mdl-layout__header-row gbfrf-column__header-row">
        {
          Constants(DialogTab.all: _*).map { tab =>
            val classList = "gbfrf-dialog__tab-bar-item mdl-layout__tab".addIf(currentTab.bind == tab, "is-active")
            val onClick = { (e: Event) => currentTab := tab; onTabChange() }
            <div class={ classList } onclick={ onClick }>{ tab.label }</div>
          }
        }
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

