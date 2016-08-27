package walfie.gbf.raidfinder.client.views

import com.thoughtworks.binding
import com.thoughtworks.binding.Binding
import com.thoughtworks.binding.Binding._
import org.scalajs.dom
import org.scalajs.dom.raw._
import scala.scalajs.js
import walfie.gbf.raidfinder.client.RaidBossColumn // TODO: Move this
import walfie.gbf.raidfinder.client.syntax.HTMLElementOps
import walfie.gbf.raidfinder.protocol._

object BossSelectorDialog {
  @binding.dom
  def dialogElement(): Binding[Element] = {
    val elem = dom.document.createElement("dialog")
    elem.classList.add("mdl-dialog")
    elem.classList.add("gbfrf-follow__dialog")
    val closeModal = { (e: Event) => elem.asInstanceOf[js.Dynamic].close(); () }

    val inner =
      <div class="gbfrf-follow mdl-layout mdl-layout--fixed-header">
        { dialogHeader(onClose = closeModal).bind }
        <div class="gbfrf-follow__content">
          <ul class="mdl-list" style="padding: 0; margin: 0;">
            { bossListItem.bind }
          </ul>
        </div>
        <hr style="margin: 0;"/>
        { dialogFooter(onCancel = closeModal).bind }
      </div>

    elem.appendChild(inner)
    elem
  }

  @binding.dom
  def dialogHeader(onClose: Event => Unit): Binding[HTMLElement] = {
    <header class="mdl-layout__header">
      <div class="mdl-layout__header-row gbfrf-column__header-row">
        <div class="mdl-layout-title gbfrf-column__header">Follow</div>
        <div class="mdl-layout-spacer"></div>
        <div class="mdl-button mdl-js-button mdl-button--icon material-icons" onclick={ onClose }>
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

  @binding.dom
  def bossListItem(): Binding[HTMLLIElement] = {
    <li class="gbfrf-follow__boss-box mdl-list__item mdl-shadow--2dp">
      <span class="gbfrf-follow__boss-text mdl-list__item-primary-content">Lvl60 Yggdrasil Omega</span>
    </li>
  }.backgroundImage("https://pbs.twimg.com/media/CT6cDD3UkAEnP8Y.jpg:small", 0.2) // TODO: less hardcoding
}

