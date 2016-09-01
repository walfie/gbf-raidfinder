package walfie.gbf.raidfinder.client.views

import com.thoughtworks.binding
import com.thoughtworks.binding.Binding
import com.thoughtworks.binding.Binding._
import org.scalajs.dom
import org.scalajs.dom.raw._
import scala.scalajs.js
import walfie.gbf.raidfinder.client._
import walfie.gbf.raidfinder.protocol._

object MainContent {
  @binding.dom
  def mainContent(
    client:       RaidFinderClient,
    notification: Notification,
    currentTime:  Binding[Double]
  ): Binding[Node] = {
    val dialog = Binding(Dialog.element(client).bind).bind

    val holder = dom.document.createDocumentFragment()

    holder.appendChild(dialog)

    val main =
      <div class="gbfrf-main-content">
        { notification.binding.bind }
        { floatingActionButton(dialog, client).bind }
        <div class="gbfrf-columns">
          {
            client.state.followedBosses.map { column =>
              RaidTweets.raidTweetColumn(column.raidBoss, column.raidTweets, currentTime, client, notification).bind
            }
          }
        </div>
      </div>

    holder.appendChild(main)

    holder
  }

  @binding.dom
  def floatingActionButton(dialog: Element, client: RaidFinderClient): Binding[HTMLDivElement] = {
    val showModal = { e: Event =>
      client.updateBosses()
      dialog.asInstanceOf[js.Dynamic].showModal()
    }

    <div class="gbfrf-settings-fab__container" onclick={ showModal }>
      <button class="mdl-button mdl-js-button mdl-button--fab mdl-js-ripple-effect mdl-button--primary">
        <i class="material-icons">add</i>
      </button>
    </div>
  }
}

