package walfie.gbf.raidfinder.client.views

import com.thoughtworks.binding
import com.thoughtworks.binding.Binding
import com.thoughtworks.binding.Binding._
import org.scalajs.dom
import org.scalajs.dom.raw._
import scala.scalajs.js
import scala.util.Try
import walfie.gbf.raidfinder.client._
import walfie.gbf.raidfinder.client.audio._
import walfie.gbf.raidfinder.client.syntax.{ElementOps, EventOps, HTMLElementOps, StringOps}
import walfie.gbf.raidfinder.client.ViewModel._
import walfie.gbf.raidfinder.protocol._

object SoundSelectionDialog {
  @binding.dom
  def element(
    selectedSoundId: Var[Option[NotificationSoundId]],
    onSave:          Option[NotificationSoundId] => Unit
  ): Binding[HTMLElement] = {
    // TODO: This is taken directly from MainDialog. Maybe make a generic version.
    val dialog = dom.document.createElement("dialog").asInstanceOf[HTMLElement]
    dialog.classList.add("mdl-dialog")
    dialog.classList.add("gbfrf-dialog")
    val dynamicDialog = dialog.asInstanceOf[js.Dynamic]

    val closeModal = { (e: Event) => dynamicDialog.close(); () }

    if (js.isUndefined(dynamicDialog.showModal)) {
      js.Dynamic.global.dialogPolyfill.registerDialog(dialog)
    }

    // TODO: Write a more generic event delegation helper
    val soundOptions = listItems(selectedSoundId).bind
    soundOptions.addEventListener("click", { e: Event =>
      val soundIdOpt = for {
        target <- e.targetElement
        element <- target.findParent(_.classList.contains("gbfrf-js-soundSelect"))
        soundIdString <- Option(element.getAttribute("data-soundId"))
        soundId <- Try(soundIdString.toInt).toOption
        sound <- NotificationSounds.findById(soundId)
      } yield {
        sound.play()
        soundId
      }

      selectedSoundId := soundIdOpt
    })

    val inner =
      <div class="gbfrf-sound-selection-dialog gbfrf-dialog__container mdl-layout mdl-js-layout mdl-layout--fixed-header">
        { header(title = "Title", close = closeModal).bind }
        <section class="gbfrf-dialog__content">
          <!-- // TODO: Put this in a method -->
          <div class="gbfrf-settings__content">
            { soundOptions }
          </div>
        </section>
        <hr style="margin: 0;"/>
        { footer(onSave = (e: Event) => { onSave(selectedSoundId.get); closeModal(e) }, onCancel = closeModal).bind }
      </div>

    dialog.appendChild(inner)

    dialog.mdl
  }

  @binding.dom
  def listItems(selectedSoundId: Binding[Option[NotificationSoundId]]): Binding[HTMLUListElement] = {
    <ul class="mdl-list" style="padding: 0; margin: 0;">
      {
        // Using -1 because it doesn't match any sound ID. This is such a hack.
        soundListItem(-1, "None", selectedSoundId).bind
      }
      {
        Constants(NotificationSounds.all: _*).map { sound =>
          soundListItem(sound.id, sound.fileName, selectedSoundId).bind
        }
      }
    </ul>
  }

  @binding.dom
  def soundListItem(id: Int, text: String, selectedSoundId: Binding[Option[NotificationSoundId]]): Binding[HTMLLIElement] = {
    val htmlId = "gbfrf-sound-option--" + id
    val htmlName = "gbfrf-sound-option"
    val mdlIsChecked = "is-checked"

    val radioButton =
      <input class="mdl-radio__button" id={ htmlId } type="radio" value={ id.toString } name={ htmlName }/>

    val labelElement =
      <label for={ htmlId } class="mdl-list__item-primary-content mdl-radio mdl-js-radio mdl-js-ripple-effect">
        { radioButton }
        <span class="mdl-radio__label">{ text }</span>
      </label>

    Binding {
      radioButton.checked = selectedSoundId.bind.contains(id)

      // MDL radio input doesn't update automatically if the real radio button is toggled
      if (radioButton.checked) labelElement.classList.add(mdlIsChecked)
      else labelElement.classList.remove(mdlIsChecked)
    }.watch

    val liClass = "gbfrf-js-soundSelect gbfrf--sound-select__item gbfrf-settings__item mdl-list__item"

    <li class={ liClass } data:data-soundId={ id.toString }>
      { labelElement }
    </li>

  }

  @binding.dom
  def header(title: String, close: Event => Unit): Binding[HTMLElement] = {
    <header class="mdl-layout__header">
      <div class="mdl-layout__header-row gbfrf-column__header-row">
        <span class="mdl-layout-title">Notification Sound</span>
        <div class="mdl-layout-spacer"></div>
        <div class="mdl-button mdl-js-button mdl-button--icon material-icons js-close-dialog" onclick={ close }>
          <i class="material-icons">clear</i>
        </div>
      </div>
    </header>
  }

  @binding.dom
  def footer(onSave: Event => Unit, onCancel: Event => Unit): Binding[HTMLElement] = {
    <div class="mdl-dialog__actions">
      <button type="button" class="mdl-button mdl-button--primary gbfrf-dialog__button" onclick={ onSave }>Save</button>
      <button type="button" class="mdl-button gbfrf-dialog__button" onclick={ onCancel }>Cancel</button>
    </div>
  }
}

