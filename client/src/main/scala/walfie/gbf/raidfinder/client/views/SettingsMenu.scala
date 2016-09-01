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

object SettingsMenu {
  @binding.dom
  def content(client: RaidFinderClient): Binding[Element] = {
    // TODO: is-active
    <section id="gbfrf-dialog__settings" class="gbfrf-dialog__content mdl-layout__tab-panel">
      <div class="gbfrf-settings__content">
        <ul class="mdl-list" style="padding: 0; margin: 0;">
          { settingsListItem("Boss image quality")(qualitySelector).bind }
          { settingsListItem("Twitter user images")(checkbox("abc")).bind }
          { settingsListItem("Relative time")(checkbox("def")).bind }
        </ul>
        <div style="margin-top: auto; align-self: flex-end; padding: 5px; color: gray;">github.com/walfie/gbf-raidfinder</div>
      </div>
    </section>
  }

  @binding.dom
  def settingsListItem(text: String)(secondaryAction: Binding[Element]): Binding[Element] = {
    <li class="gbfrf-settings__item mdl-list__item">
      <div class="mdl-list__item-primary-content">{ text }</div>
      <div class="mdl-list__item-secondary-action">
        { secondaryAction.bind /* TODO: extra classes */ }
      </div>
    </li>
  }

  @binding.dom
  def checkbox(id: String): Binding[HTMLLabelElement] = {
    <label class="mdl-switch mdl-js-switch mdl-js-ripple-effect" for={ id }>
      <input type="checkbox" id={ id } class="mdl-switch__input"/><!-- // TODO: Checked -->
    </label>
  }

  sealed abstract class ImageQuality(val label: String)
  case object Off extends ImageQuality("Off")
  case object Low extends ImageQuality("Low")
  case object High extends ImageQuality("High")

  @binding.dom // TODO: OnClick
  def qualitySelector: Binding[HTMLDivElement] = {
    <div class="gbfrf-settings__toggle mdl-list__item-secondary-action">
      {
        Constants(Off, Low, High).map { quality =>
          val id = "gbfrf-settings__image-quality--" + quality.label

          <label class="mdl-radio mdl-js-radio mdl-js-ripple-effect" for={ id }>
            <input type="radio" id={ id } class="mdl-radio__button" name="options" value={ quality.label }/>
            <span class="mdl-radio__label">{ quality.label }</span>
          </label>
        }
      }
    </div>
  }

}

