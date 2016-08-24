package walfie.gbf.raidfinder.client.views

import com.thoughtworks.binding
import com.thoughtworks.binding.Binding
import com.thoughtworks.binding.Binding._
import org.scalajs.dom.raw._
import org.widok.moment._
import walfie.gbf.raidfinder.client.util.MaterialDesignLiteUtil.HTMLElementOps
import walfie.gbf.raidfinder.protocol._
import walfie.gbf.raidfinder.protocol.RaidBossesResponse.RaidBoss

object RaidTweets {
  @binding.dom
  def raidTweetColumn(bossName: String, raidTweets: BindingSeq[RaidTweetResponse]): Binding[HTMLDivElement] = {
    <div class="gbfrf-column mdl-shadow--4dp">
      <div class="mdl-layout mdl-layout--fixed-header">
        { raidBossHeader(bossName).bind }
        <div class="mdl-layout__content">
          { raidTweetList(raidTweets).bind }
        </div>
      </div>
    </div>
  }.mdl

  @binding.dom
  def raidTweetList(
    raidTweets: BindingSeq[RaidTweetResponse]
  ): Binding[HTMLUListElement] = {
    <ul class="mdl-list gbfrf-tweets">
      {
        for (rt <- raidTweets) yield raidTweetListItem(rt).bind
      }
    </ul>
  }

  @binding.dom
  def raidTweetListItem(raidTweet: RaidTweetResponse): Binding[HTMLLIElement] = {
    <li class="mdl-list__item mdl-list__item--two-line gbfrf-tweet">
      <span class="mdl-list__item-primary-content">
        <!-- // TODO: Handle long names. Exclude second row if no extra text. -->
        <img class="gbfrf-tweet__avatar mdl-list__item-avatar" src={ raidTweet.profileImage }/>
        <span>{ raidTweet.screenName }</span>
        <!-- // TODO: Better relative datetime -->
        <span class="gbfrf-tweet__timestamp">{ Moment(raidTweet.createdAt.getTime).fromNow(true) }</span>
        <span class="mdl-list__item-sub-title gbfrf-tweet__text">{ raidTweet.text }</span>
      </span>
      <span class="mdl-list__item-secondary-content gbfrf-tweet__raid-id">
        { raidTweet.raidId }
      </span>
    </li>
  }

  private def menuId(bossName: String): String = "menu_" + bossName.replace(" ", "_")

  @binding.dom
  def raidBossHeader(bossName: String): Binding[HTMLElement] = {
    <header class="mdl-layout__header">
      <div class="mdl-layout__header-row gbfrf-column__header-row">
        <div class="mdl-layout-title gbfrf-column__header">{ bossName }</div>
        <div class="mdl-layout-spacer"></div>
        <button class="mdl-button mdl-js-button mdl-button--icon" id={ menuId(bossName) }>
          <i class="material-icons">more_vert</i>
        </button>
        { raidBossHeaderMenu(bossName).bind }
      </div>
    </header>
  }

  @binding.dom
  def raidBossHeaderMenu(bossName: String): Binding[HTMLUListElement] = {
    <ul class="mdl-menu mdl-menu--bottom-right mdl-js-menu mdl-js-ripple-effect" data:for={ menuId(bossName) }>
      { menuItem("Delete", "delete").bind }
      { menuItem("Clear", "clear_all").bind }
      { menuItem("Move Left", "keyboard_arrow_left").bind }
      { menuItem("Move Right", "keyboard_arrow_right").bind }
    </ul>
  }

  @binding.dom
  def menuItem(text: String, icon: String): Binding[HTMLLIElement] = {
    <li class="mdl-menu__item">
      <i class="material-icons">{ icon }</i>
      <span>{ text }</span>
    </li>
  }
}

