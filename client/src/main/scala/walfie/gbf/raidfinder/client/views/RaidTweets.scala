package walfie.gbf.raidfinder.client.views

import com.momentjs.Moment
import com.thoughtworks.binding
import com.thoughtworks.binding.Binding
import com.thoughtworks.binding.Binding._
import walfie.gbf.raidfinder.client.ViewModel
import walfie.gbf.raidfinder.client.ViewModel.ImageQuality
import org.scalajs.dom
import org.scalajs.dom.raw._
import scala.scalajs.js
import walfie.gbf.raidfinder.client.RaidFinderClient
import walfie.gbf.raidfinder.client.syntax.{ElementOps, EventOps, HTMLElementOps, StringOps}
import walfie.gbf.raidfinder.client.util.HtmlHelpers
import walfie.gbf.raidfinder.protocol._

object RaidTweets {
  @binding.dom
  def raidTweetColumn(
    raidBoss:     Binding[RaidBoss],
    raidTweets:   BindingSeq[RaidTweetResponse],
    currentTime:  Binding[Double],
    client:       RaidFinderClient,
    notification: Notification,
    viewState:    ViewModel.State
  ): Binding[HTMLDivElement] = {
    <div class="gbfrf-column mdl-shadow--4dp">
      <div class="mdl-layout mdl-layout--fixed-header">
        { raidBossHeader(raidBoss.bind, client, viewState.imageQuality).bind }
        <div class="mdl-layout__content">
          { raidTweetList(raidTweets, currentTime, notification, viewState).bind }
        </div>
      </div>
    </div>
  }

  @binding.dom
  def raidTweetList(
    raidTweets:   BindingSeq[RaidTweetResponse],
    currentTime:  Binding[Double],
    notification: Notification,
    viewState:    ViewModel.State
  ): Binding[HTMLUListElement] = {
    val list =
      <ul class="mdl-list gbfrf-tweets">
        {
          raidTweets.map { raidTweet =>
            raidTweetListItem(
              raidTweet, currentTime, viewState.showUserImages, viewState.relativeTime
            ).bind
          }
        }
      </ul>

    list.addEventListener("click", { e: Event =>
      // TODO: Put these IDs in a central place instead of hardcoding them
      for {
        target <- e.targetElement
        element <- target.findParent(_.classList.contains("gbfrf-js-tweet"))
        raidId <- Option(element.getAttribute("data-raidId"))
      } yield {
        if (HtmlHelpers.copy(raidId)) {
          element.classList.toggle("gbfrf-tweet--copied")
          notification.enqueue(s"$raidId copied to clipboard")
        }
      }
    })

    list
  }

  @binding.dom
  def raidTweetListItem(
    raidTweet:        RaidTweetResponse,
    currentTime:      Binding[Double],
    showUserImages:   Binding[Boolean],
    showRelativeTime: Binding[Boolean]
  ): Binding[HTMLLIElement] = {
    val hasText = raidTweet.text.nonEmpty
    val avatar = {
      val url = raidTweet.profileImage.replace("_normal.", "_mini.")
      val imageClass = "gbfrf-tweet__avatar".addIf(hasText, "gbfrf-tweet__avatar--offset")
      <img class={ imageClass } src={
        if (showUserImages.bind) url else HtmlHelpers.BlankImage
      }/>
    }

    <li class="gbfrf-tweet gbfrf-js-tweet mdl-list__item" data:data-raidId={ raidTweet.raidId }>
      <div class="mdl-list__item-primary-content">
        { avatar }
        <div class="gbfrf-tweet__content">
          <div>
            <span class="gbfrf-tweet__username">{ raidTweet.screenName }</span>
            <span class="gbfrf-tweet__timestamp">
              {
                if (showRelativeTime.bind) {
                  Moment(raidTweet.createdAt.getTime).from(currentTime.bind, true)
                } else {
                  // TODO: This is deprecated and also doesn't display well. Do it better.
                  val time = raidTweet.createdAt
                  s"${time.getHours}:${time.getMinutes}:${time.getSeconds}"
                }
              }
            </span>
          </div>
          {
            if (hasText) List(<div class="gbfrf-tweet__text mdl-shadow--2dp">{ raidTweet.text }</div>)
            else List.empty
          }
        </div>
      </div>
      <div class="gbfrf-tweet__raid-id">
        { raidTweet.raidId }
      </div>
    </li>
  }

  private def menuId(bossName: String): String = "menu_" + bossName.replace(" ", "_")

  @binding.dom
  def raidBossHeader(
    raidBoss:     RaidBoss,
    client:       RaidFinderClient,
    imageQuality: Binding[ImageQuality]
  ): Binding[HTMLElement] = {
    val headerRow =
      <div class="mdl-layout__header-row gbfrf-column__header-row">
        <div class="mdl-layout-title gbfrf-column__header">{ raidBoss.name }</div>
        <div class="mdl-layout-spacer"></div>
        <button class="mdl-button mdl-js-button mdl-button--icon" id={ menuId(raidBoss.name) }>
          <i class="material-icons">more_vert</i>
        </button>
        { raidBossHeaderMenu(raidBoss.name, client).bind }
      </div>

    headerRow.backgroundImageQuality(raidBoss.image, 0.25, imageQuality.bind)

    <header class="mdl-layout__header">
      { headerRow }
    </header>
  }.mdl

  @binding.dom
  def raidBossHeaderMenu(bossName: String, client: RaidFinderClient): Binding[HTMLUListElement] = {
    <ul class="mdl-menu mdl-menu--bottom-right mdl-js-menu mdl-js-ripple-effect" data:for={ menuId(bossName) }>
      { menuItem("Move Left", "keyboard_arrow_left", _ => client.move(bossName, -1)).bind }
      { menuItem("Move Right", "keyboard_arrow_right", _ => client.move(bossName, 1)).bind }
      { menuItem("Clear", "clear_all", _ => client.clear(bossName)).bind }
      { menuItem("Unfollow", "delete", _ => client.unfollow(bossName)).bind }
    </ul>
  }

  @binding.dom
  def menuItem(text: String, icon: String, onClick: Event => Unit): Binding[HTMLLIElement] = {
    <li class="mdl-menu__item" onclick={ onClick }>
      <i class="gbfrf-column__header-row-icon material-icons">{ icon }</i>
      <span>{ text }</span>
    </li>
  }
}

