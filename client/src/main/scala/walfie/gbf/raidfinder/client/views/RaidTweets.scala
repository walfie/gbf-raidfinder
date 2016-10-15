package walfie.gbf.raidfinder.client.views

import com.momentjs.Moment
import com.thoughtworks.binding
import com.thoughtworks.binding.Binding
import com.thoughtworks.binding.Binding._
import org.scalajs.dom
import org.scalajs.dom.raw._
import scala.scalajs.js
import walfie.gbf.raidfinder.client.RaidFinderClient
import walfie.gbf.raidfinder.client.RaidFinderClient.RaidBossColumn
import walfie.gbf.raidfinder.client.syntax.{ElementOps, EventOps, HTMLElementOps, LanguageOps, StringOps}
import walfie.gbf.raidfinder.client.util.HtmlHelpers
import walfie.gbf.raidfinder.client.ViewModel
import walfie.gbf.raidfinder.client.ViewModel.{ImageQuality, TimeFormat}
import walfie.gbf.raidfinder.protocol._

object RaidTweets {
  @binding.dom
  def raidTweetColumn(
    column:          RaidBossColumn,
    currentTime:     Binding[Double],
    client:          RaidFinderClient,
    notification:    Notification,
    viewState:       ViewModel.State,
    onSoundMenuOpen: BossName => Unit
  ): Binding[HTMLDivElement] = {
    <div class="gbfrf-column mdl-shadow--2dp">
      <div class="mdl-layout mdl-layout--fixed-header">
        { raidBossHeader(column.raidBoss, viewState.imageQuality, column.isSubscribed, onSoundMenuOpen, client).bind }
        {
          <div class="gbfrf-column__notification-banner mdl-shadow--4dp">
            <div class="gbfrf-column__notification-banner-container">
              { notificationBannerItem("Sound on", "volume_up", Binding(column.notificationSound.bind.nonEmpty)).bind }
              { notificationBannerItem("Notifications on", "notifications", column.isSubscribed).bind }
            </div>
          </div>
        }
        <div class="mdl-layout__content">
          { raidTweetList(column.raidTweets, currentTime, notification, viewState).bind }
        </div>
      </div>
    </div>
  }

  @binding.dom
  private def notificationBannerItem(
    text:    String,
    icon:    String,
    isShown: Binding[Boolean]
  ): Binding[HTMLDivElement] = {
    <div class={ "gbfrf-column__notification-banner-item".addIf(!isShown.bind, "is-hidden") }>
      <i class="gbfrf-column__notification-banner-icon material-icons">{ icon }</i>
      { text }
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
              raidTweet, currentTime, viewState.timeFormat, viewState.showUserImages
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
    raidTweet:      RaidTweetResponse,
    currentTime:    Binding[Double],
    timeFormat:     Binding[TimeFormat],
    showUserImages: Binding[Boolean]
  ): Binding[HTMLLIElement] = {
    val hasText = raidTweet.text.nonEmpty
    val avatar = {
      val url = raidTweet.profileImage.replace("_normal.", "_mini.")
      val imageClass = "gbfrf-tweet__avatar"
        .addIf(hasText, "gbfrf-tweet__avatar--offset")
      <img class={ imageClass.addIf(!showUserImages.bind, "is-hidden") } src={
        if (showUserImages.bind) url else HtmlHelpers.BlankImage
      }/>
    }

    <li class="gbfrf-tweet gbfrf-js-tweet mdl-list__item" data:data-raidId={ raidTweet.raidId }>
      <div class="mdl-list__item-primary-content">
        { avatar }
        <div class="gbfrf-tweet__content">
          <div>
            <span class="gbfrf-tweet__username">{ raidTweet.screenName }</span>
            {
              if (raidTweet.language != Language.JAPANESE) {
                val lang = raidTweet.language.shortName.getOrElse("")
                Constants(<span class="gbfrf-tweet__language gbfrf-parentheses">{ lang }</span>)
              } else Constants()
            }
            <span class="gbfrf-tweet__timestamp">
              {
                val moment = Moment(raidTweet.createdAt.getTime)

                // TODO: Put this in a method
                timeFormat.bind match {
                  case TimeFormat.Relative =>
                    moment.from(currentTime.bind, true)
                  case TimeFormat.TwelveHour =>
                    moment.format("h:mm:ssa")
                  case TimeFormat.TwentyFourHour =>
                    moment.format("H:mm:ss")
                }
              }
            </span>
          </div>
          {
            if (hasText) Constants(<div class="gbfrf-tweet__text mdl-shadow--2dp">{ raidTweet.text }</div>)
            else Constants()
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
    raidBoss:        Binding[RaidBoss],
    imageQuality:    Binding[ImageQuality],
    isSubscribed:    Binding[Boolean],
    onSoundMenuOpen: BossName => Unit,
    client:          RaidFinderClient
  ): Binding[HTMLElement] = {
    val bossName = Binding(raidBoss.bind.name)

    val headerRow =
      <div class="mdl-layout__header-row gbfrf-column__header-row">
        <div class="mdl-layout-title gbfrf-column__header">
          <div class="gbfrf-column__header-name">{ bossName.bind }</div>
          {
            raidBoss.bind.translatedName match {
              case Some(translatedName) => Constants(
                <div class="gbfrf-column__header-translatedName">{ translatedName }</div>
              )
              case None => Constants()
            }
          }
        </div>
        <div class="mdl-layout-spacer"></div>
        <button class="mdl-button mdl-js-button mdl-button--icon" id={ menuId(bossName.bind) }>
          <i class="material-icons">more_vert</i>
        </button>
        {
          raidBossHeaderMenu(bossName.bind, isSubscribed, onSoundMenuOpen, client).bind
        }
      </div>

    headerRow.backgroundImageQuality(raidBoss.bind.image, 0.25, imageQuality.bind)

    <header class="mdl-layout__header">
      { headerRow }
    </header>
  }.mdl

  @binding.dom
  def raidBossHeaderMenu(
    bossName:        String,
    isSubscribed:    Binding[Boolean],
    onSoundMenuOpen: BossName => Unit,
    client:          RaidFinderClient
  ): Binding[HTMLUListElement] = {
    <ul class="mdl-menu mdl-menu--bottom-right mdl-js-menu mdl-js-ripple-effect" data:for={ menuId(bossName) }>
      { menuItem("Move Left", "keyboard_arrow_left", _ => client.move(bossName, -1)).bind }
      { menuItem("Move Right", "keyboard_arrow_right", _ => client.move(bossName, 1)).bind }
      { menuItem("Clear", "clear_all", _ => client.clear(bossName)).bind }
      { menuItem("Unfollow", "delete", _ => client.unfollow(bossName)).bind }
      {
        val (text, icon) =
          if (isSubscribed.bind) ("Unsubscribe", "notifications_off")
          else ("Subscribe", "notifications_on")
        menuItem(text, icon, _ => client.toggleSubscribe(bossName)).bind
      }
      { menuItem("Sound", "music_note", _ => onSoundMenuOpen(bossName)).bind }
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

