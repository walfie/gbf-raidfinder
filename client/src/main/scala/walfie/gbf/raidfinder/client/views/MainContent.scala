package walfie.gbf.raidfinder.client.views

import com.thoughtworks.binding
import com.thoughtworks.binding.Binding
import com.thoughtworks.binding.Binding._
import org.scalajs.dom
import org.scalajs.dom.raw._
import scala.scalajs.js
import walfie.gbf.raidfinder.client._
import walfie.gbf.raidfinder.client.syntax._
import walfie.gbf.raidfinder.client.ViewModel
import walfie.gbf.raidfinder.client.ViewModel.DialogTab
import walfie.gbf.raidfinder.protocol._

object MainContent {
  @binding.dom
  def mainContent(
    client:       RaidFinderClient,
    viewState:    ViewModel.State,
    notification: Notification,
    currentTime:  Binding[Double],
    isConnected:  Binding[Boolean]
  ): Binding[Constants[HTMLElement]] = {
    val dialog = Dialog.element(client, viewState).bind

    val main =
      <div class="gbfrf-main-content">
        { notification.binding.bind }
        { floatingActionButton(dialog, client, viewState.currentTab).bind }
        <div class="gbfrf-columns">
          {
            client.state.followedBosses.map { column =>
              RaidTweets.raidTweetColumn(
                column.raidBoss, column.raidTweets, currentTime,
                client, notification, viewState
              ).bind
            }
          }
        </div>
      </div>

    Constants(
      loadingBar(isConnected).bind,
      dialog,
      main
    )
  }

  @binding.dom
  def floatingActionButton(
    dialog:     Element,
    client:     RaidFinderClient,
    currentTab: Binding[DialogTab]
  ): Binding[HTMLDivElement] = {
    val showModal = (e: Event) => dialog.asInstanceOf[js.Dynamic].showModal()

    <div class="gbfrf-settings-fab__container" onclick={ showModal }>
      <button class="mdl-button mdl-js-button mdl-button--fab mdl-js-ripple-effect mdl-button--primary">
        <i class="material-icons">
          { currentTab.bind.icon }
        </i>
      </button>
    </div>
  }

  @binding.dom
  def loadingBar(isHidden: Binding[Boolean]): Binding[HTMLDivElement] = {
    <div class={
      "gbfrf-loading-bar mdl-progress mdl-js-progress mdl-progress__indeterminate"
        .addIf(isHidden.bind, "is-hidden")
    }></div>
  }.mdl
}

