package walfie.gbf.raidfinder.client.views

import com.thoughtworks.binding
import com.thoughtworks.binding.Binding
import com.thoughtworks.binding.Binding._
import org.scalajs.dom
import org.scalajs.dom.raw._
import scala.scalajs.js
import walfie.gbf.raidfinder.client._
import walfie.gbf.raidfinder.client.ViewModel._
import walfie.gbf.raidfinder.client.syntax.{ElementOps, EventOps, HTMLElementOps, StringOps}
import walfie.gbf.raidfinder.protocol._

object BossSelectMenu {
  @binding.dom
  def content(
    client:       RaidFinderClient,
    closeModal:   Event => Unit,
    currentTab:   Binding[DialogTab],
    imageQuality: Binding[ImageQuality]
  ): Binding[Element] = {
    val isFollowing = client.state.followedBossNames
    val bossListElement = bossList(client, imageQuality).bind

    // TODO: Write a more generic event delegation helper
    bossListElement.addEventListener("click", { e: Event =>
      for {
        target <- e.targetElement
        element <- target.findParent(_.classList.contains("gbfrf-js-bossSelect"))
        bossName <- Option(element.getAttribute("data-bossName"))
      } yield client.toggleFollow(bossName)
    })

    <section id="gbfrf-dialog__follow" class={
      val isActive = currentTab.bind == DialogTab.Follow
      "gbfrf-dialog__content mdl-layout__tab-panel".addIf(isActive, "is-active")
    }>
      { bossListElement }
    </section>
  }

  @binding.dom
  def bossList(client: RaidFinderClient, imageQuality: Binding[ImageQuality]): Binding[HTMLUListElement] = {
    <ul class="mdl-list" style="padding: 0; margin: 0;">
      {
        client.state.allBosses.map { bossColumn =>
          val boss = bossColumn.raidBoss.bind
          val isFollowing = client.state.followedBossNames.bind(boss.name)

          // Only show Japanese bosses unless there is no translation
          // TODO: This is kinda hacky, maybe think of a better way
          if (boss.language == Language.JAPANESE || boss.translatedName.isEmpty || isFollowing)
            bossListItem(boss, isFollowing, imageQuality).bind
          else <li></li>
        }
      }
    </ul>
  }

  @binding.dom
  def bossListItem(
    boss: RaidBoss, isFollowing: Boolean, imageQuality: Binding[ImageQuality]
  ): Binding[HTMLLIElement] = {
    val elem =
      <li class={
        "gbfrf-js-bossSelect gbfrf-follow__boss-box mdl-list__item".addIf(
          imageQuality.bind != ImageQuality.Off,
          "gbfrf-follow__boss-box--with-image mdl-shadow--2dp"
        ).addIf(boss.translatedName.nonEmpty, "mdl-list__item--two-line")
      } data:data-bossName={ boss.name }>
        <span class="mdl-list__item-primary-content">
          <span>{ boss.name }</span>
          {
            boss.translatedName match {
              case Some(translatedName) => List(
                <span class="gbfrf-follow__boss-box-subtitle mdl-list__item-sub-title">{ translatedName }</span>
              )
              case None => List.empty
            }
          }
        </span>
        <div class="mdl-layout-spacer"></div>
        {
          if (isFollowing) List(<div class="mdl-badge mdl-badge--overlap" data:data-badge="★"></div>)
          else List.empty
        }
      </li>

    elem.backgroundImageQuality(boss.image, 0.25, imageQuality.bind)
  }
}

