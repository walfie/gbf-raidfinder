package walfie.gbf.raidfinder.client.views

import com.momentjs.Moment
import com.thoughtworks.binding
import com.thoughtworks.binding.Binding
import com.thoughtworks.binding.Binding._
import org.scalajs.dom.raw._
import scala.scalajs.js
import walfie.gbf.raidfinder.client.RaidFinderClient
import walfie.gbf.raidfinder.client.syntax.HTMLElementOps
import walfie.gbf.raidfinder.protocol._

object RaidTweets {
  @binding.dom
  def raidTweetColumn(
    raidBoss:    Binding[RaidBoss],
    raidTweets:  BindingSeq[RaidTweetResponse],
    currentTime: Binding[Double],
    client:      RaidFinderClient
  ): Binding[HTMLDivElement] = {
    <div class="gbfrf-column mdl-shadow--4dp">
      <div class="mdl-layout mdl-layout--fixed-header">
        { raidBossHeader(raidBoss.bind, client).bind }
        <div class="mdl-layout__content">
          { raidTweetList(raidTweets, currentTime).bind }
        </div>
      </div>
    </div>
  }

  @binding.dom
  def raidTweetList(
    raidTweets:  BindingSeq[RaidTweetResponse],
    currentTime: Binding[Double]
  ): Binding[HTMLUListElement] = {
    <ul class="mdl-list gbfrf-tweets">
      { raidTweets.map(raidTweet => raidTweetListItem(raidTweet, currentTime).bind) }
    </ul>
  }

  @binding.dom
  def raidTweetListItem(raidTweet: RaidTweetResponse, currentTime: Binding[Double]): Binding[HTMLLIElement] = {
    val avatar = raidTweet.profileImage.replace("_normal.", "_mini.")

    <li class="gbfrf-tweet mdl-list__item">
      <div class="mdl-list__item-primary-content">
        <img class="gbfrf-tweet__avatar" src={ avatar }/>
        <div class="gbfrf-tweet__content">
          <div>
            <span class="gbfrf-tweet__username">{ raidTweet.screenName }</span>
            <span class="gbfrf-tweet__timestamp">
              { Moment(raidTweet.createdAt.getTime).from(currentTime.bind, true) }
            </span>
          </div>
          {
            if (raidTweet.text.nonEmpty) List(<div class="gbfrf-tweet__text">{ raidTweet.text }</div>)
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
  def raidBossHeader(raidBoss: RaidBoss, client: RaidFinderClient): Binding[HTMLElement] = {
    val headerRow =
      <div class="mdl-layout__header-row gbfrf-column__header-row">
        <div class="mdl-layout-title gbfrf-column__header">{ raidBoss.bossName }</div>
        <div class="mdl-layout-spacer"></div>
        <button class="mdl-button mdl-js-button mdl-button--icon" id={ menuId(raidBoss.bossName) }>
          <i class="material-icons">more_vert</i>
        </button>
        { raidBossHeaderMenu(raidBoss.bossName, client).bind }
      </div>

    raidBoss.image.foreach(image => headerRow.backgroundImage(image + ":thumb", 0.25, cover = true))

    <header class="mdl-layout__header">
      { headerRow }
    </header>
  }.mdl

  @binding.dom
  def raidBossHeaderMenu(bossName: String, client: RaidFinderClient): Binding[HTMLUListElement] = {
    <ul class="mdl-menu mdl-menu--bottom-right mdl-js-menu mdl-js-ripple-effect" data:for={ menuId(bossName) }>
      { menuItem("Clear", "clear_all", _ => client.clear(bossName)).bind }
      { menuItem("Unfollow", "delete", _ => client.unfollow(bossName)).bind }
      { menuItem("Move Left", "keyboard_arrow_left", _ => client.move(bossName, -1)).bind }
      { menuItem("Move Right", "keyboard_arrow_right", _ => client.move(bossName, 1)).bind }
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

