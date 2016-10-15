package walfie.gbf.raidfinder.client.views

import com.thoughtworks.binding
import com.thoughtworks.binding.Binding
import com.thoughtworks.binding.Binding._
import org.scalajs.dom
import org.scalajs.dom.raw._
import scala.scalajs.js
import walfie.gbf.raidfinder.client._
import walfie.gbf.raidfinder.client.audio._
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
    val mainDialog = MainDialog.element(client, viewState).bind

    val selectedBossName = Var[Option[BossName]](None)
    val selectedSoundId = Var[Option[NotificationSoundId]](None)
    val soundSelectionDialog = {
      val onSoundSave = { selectedSoundId: Option[NotificationSoundId] =>
        selectedBossName.get.foreach { name =>
          client.setNotificationSound(name, selectedSoundId)
        }
      }

      SoundSelectionDialog.element(
        selectedSoundId = selectedSoundId,
        onSave = onSoundSave
      ).bind
    }

    val onSoundMenuOpen: BossName => Unit = { bossName: BossName =>
      selectedSoundId := client.getNotificationSound(bossName).map(_.id)
      selectedBossName := Some(bossName)
      soundSelectionDialog.asInstanceOf[js.Dynamic].showModal()
    }

    handleNightMode(viewState.nightMode).watch

    val styleElement = <style></style>
    handleColumnWidth(viewState.columnWidthScale, styleElement).watch

    val main =
      <div class="gbfrf-main-content">
        { styleElement }
        { notification.binding.bind }
        { floatingActionButton(mainDialog, client, viewState.currentTab).bind }
        <div class="gbfrf-columns">
          {
            client.state.followedBosses.map { column =>
              RaidTweets.raidTweetColumn(
                column, currentTime, client, notification, viewState, onSoundMenuOpen
              ).bind
            }
          }
        </div>
      </div>

    Constants(
      loadingBar(isConnected).bind,
      mainDialog,
      soundSelectionDialog,
      main
    )
  }

  /** Handle night mode toggle */
  @binding.dom
  def handleNightMode(nightMode: Var[Boolean]): Binding[Unit] = {
    val bodyClasses = dom.document.body.classList
    val themePrefix = "gbfrf-theme--"
    val lightTheme = themePrefix + "light"
    val darkTheme = themePrefix + "dark"

    if (nightMode.bind) {
      bodyClasses.add(darkTheme)
      bodyClasses.remove(lightTheme)
    } else {
      bodyClasses.add(lightTheme)
      bodyClasses.remove(darkTheme)
    }
  }

  @binding.dom
  def handleColumnWidth(columnWidthScale: Var[Double], style: HTMLStyleElement): Binding[Unit] = {
    val scale = columnWidthScale.bind
    style.innerHTML = js.Array(
      s".gbfrf-column { width: ${240 + scale * 110}px; }",
      s".gbfrf-tweet__text { font-size: ${0.8 + scale * 0.2}em; margin-right: ${5 * (1 - scale)}px; }"
    ).join("\n")
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

