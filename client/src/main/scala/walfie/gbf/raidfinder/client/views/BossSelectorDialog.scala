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

object BossSelectorDialog {
  @binding.dom
  def dialogElement(client: RaidFinderClient): Binding[Element] = {
    val dialog = dom.document.createElement("dialog")
    dialog.classList.add("mdl-dialog")
    dialog.classList.add("gbfrf-follow__dialog")
    val closeModal = { (e: Event) => dialog.asInstanceOf[js.Dynamic].close(); () }

    val bossListElement = bossList(client).bind

    // TODO: Write a more generic event delegation helper
    bossListElement.addEventListener("click", { e: Event =>
      for {
        target <- e.targetElement
        element <- target.findParent(_.classList.contains("gbfrf-js-bossSelect"))
        bossName <- Option(element.getAttribute("data-bossName"))
      } yield {
        client.follow(bossName)
        closeModal(e)
      }
    })

    val inner =
      <div class="gbfrf-follow mdl-layout mdl-layout--fixed-header">
        { dialogHeader(onClose = closeModal).bind }
        <div class="gbfrf-follow__content">
          { bossListElement }
        </div>
        <hr style="margin: 0;"/>
        { dialogFooter(onCancel = closeModal).bind }
      </div>

    dialog.appendChild(inner)
    dialog
  }

  @binding.dom
  def bossList(client: RaidFinderClient): Binding[HTMLUListElement] = {
    <ul class="mdl-list" style="padding: 0; margin: 0;">
      {
        client.state.allBosses.map { bossColumn =>
          val boss = bossColumn.raidBoss.bind
          val isFollowing = client.state.followedBossNames.bind
          val smallImage = boss.image.map(_ + ":thumb")
          bossListItem(boss.name, smallImage, isFollowing(boss.name)).bind
        }
      }
    </ul>
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
  def bossListItem(bossName: String, image: Option[String], isFollowing: Boolean): Binding[HTMLLIElement] = {
    val elem =
      <li class="gbfrf-js-bossSelect gbfrf-follow__boss-box mdl-list__item mdl-shadow--2dp" data:data-bossName={ bossName }>
        <span class="gbfrf-follow__boss-text mdl-list__item-primary-content">{ bossName }</span>
        <div class="mdl-layout-spacer"></div>
        {
          if (isFollowing) List(<div class="mdl-badge mdl-badge--overlap" data:data-badge="★"></div>)
          else List.empty
        }
      </li>

    image.foreach(elem.backgroundImage(_, 0.25))

    elem
  }
}

