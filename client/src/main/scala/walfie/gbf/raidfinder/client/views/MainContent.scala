package walfie.gbf.raidfinder.client.views

import com.thoughtworks.binding
import com.thoughtworks.binding.Binding
import com.thoughtworks.binding.Binding._
import org.scalajs.dom.raw._
import scala.scalajs.js
import walfie.gbf.raidfinder.client.RaidBossColumn // TODO: Move this
import walfie.gbf.raidfinder.protocol._
import walfie.gbf.raidfinder.protocol.RaidBossesResponse.RaidBoss

object MainContent {
  @binding.dom
  def mainContent(bossColumns: BindingSeq[RaidBossColumn]): Binding[HTMLDivElement] = {
    val dialog = BossSelectorDialog.dialogElement.bind

    <div class="gbfrf-main-content">
      { dialog }
      { floatingActionButton(dialog).bind }
      <div class="gbfrf-columns">
        {
          bossColumns.map { bossColumn =>
            RaidTweets.raidTweetColumn(
              bossColumn.raidBoss,
              bossColumn.raidTweets
            ).bind
          }.bind
        }
      </div>
    </div>
  }

  @binding.dom
  def floatingActionButton(dialog: Element): Binding[HTMLDivElement] = {
    val showModal = (e: Event) => dialog.asInstanceOf[js.Dynamic].showModal()
    <div class="gbfrf-settings-fab__container" onclick={ showModal }>
      <button class="mdl-button mdl-js-button mdl-button--fab mdl-js-ripple-effect mdl-button--primary">
        <i class="material-icons">add</i>
      </button>
    </div>
  }
}

