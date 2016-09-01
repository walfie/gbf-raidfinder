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
  def content(client: RaidFinderClient, closeModal: Event => Unit): Binding[Element] = {
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

    <section id="gbfrf-dialog__follow" class="gbfrf-dialog__content mdl-layout__tab-panel is-active">
      <!-- // TODO: is-visible -->
      { bossListElement }
    </section>
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

