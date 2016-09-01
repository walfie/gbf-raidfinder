package walfie.gbf.raidfinder.client.views

import com.thoughtworks.binding
import com.thoughtworks.binding.Binding
import com.thoughtworks.binding.Binding._
import org.scalajs.dom
import org.scalajs.dom.raw._
import scala.scalajs.js
import walfie.gbf.raidfinder.client._
import walfie.gbf.raidfinder.client.syntax.StringOps
import walfie.gbf.raidfinder.client.ViewModel._
import walfie.gbf.raidfinder.client.ViewModel.ImageQuality._
import walfie.gbf.raidfinder.protocol._

object SettingsMenu {
  @binding.dom
  def content(client: RaidFinderClient, viewState: State): Binding[Element] = {
    <section id="gbfrf-dialog__settings" class={
      val isActive = viewState.currentTab.bind == DialogTab.Settings
      "gbfrf-dialog__content mdl-layout__tab-panel".addIf(isActive, "is-active")
    }>
      <div class="gbfrf-settings__content">
        <ul class="mdl-list" style="padding: 0; margin: 0;">
          { settingsListItem("Boss image quality")(qualitySelector).bind }
          { settingsListItem("Twitter user images")(checkbox("gbfrf-setting__user-image", viewState.showUserImages)).bind }
          { settingsListItem("Relative time")(checkbox("gbfrf-setting__relative-time", viewState.relativeTime)).bind }
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
  def checkbox(id: String, checked: Var[Boolean]): Binding[HTMLLabelElement] = {
    val onClick = { e: dom.Event =>
      checked := e.currentTarget.asInstanceOf[HTMLInputElement].checked
    }

    val input = <input type="checkbox" id={ id } class="mdl-switch__input" onclick={ onClick }/>
    input.checked = checked.get

    <label class="mdl-switch mdl-js-switch mdl-js-ripple-effect" for={ id }>
      { input }
    </label>
  }

  @binding.dom // TODO: OnClick
  def qualitySelector: Binding[HTMLDivElement] = {
    <div class="gbfrf-settings__toggle">
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

