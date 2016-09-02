package walfie.gbf.raidfinder.client

import com.thoughtworks.binding.Binding._
import org.scalajs.dom
import scala.scalajs.js
import scala.scalajs.js.annotation.ScalaJSDefined

object ViewModel {
  sealed abstract class DialogTab(val label: String) {
    def id: String = s"gbfrf-dialog__$label"
  }
  object DialogTab {
    case object Follow extends DialogTab("Follow")
    case object Settings extends DialogTab("Settings")

    val all: List[DialogTab] = List(Follow, Settings)
    val fromString: String => Option[DialogTab] =
      all.map(tab => tab.label -> tab).toMap.get _
  }

  sealed abstract class ImageQuality(val label: String)
  object ImageQuality {
    case object Off extends ImageQuality("Off")
    case object Low extends ImageQuality("Low")
    case object High extends ImageQuality("High")

    val Default = Off
    val fromString: String => Option[ImageQuality] =
      List(Off, Low, High).map(q => q.label -> q).toMap.get _
  }

  // TODO: Maybe put this somewhere else
  private val StateStorageKey = "settings"
  private val storage = dom.window.localStorage

  def persistState(state: State): Unit = {
    val jsString = js.JSON.stringify(state.toJsObject)
    storage.setItem(StateStorageKey, jsString)
  }

  def loadState(): State = {
    Option(storage.getItem(StateStorageKey)).map { jsString =>
      val jsState = js.JSON.parse(jsString).asInstanceOf[JsState]
      State.fromJsObject(jsState)
    }.getOrElse(State())
  }

  case class State(
    currentTab:     Var[DialogTab]    = Var(DialogTab.Follow),
    imageQuality:   Var[ImageQuality] = Var(ImageQuality.Off),
    showUserImages: Var[Boolean]      = Var(false),
    relativeTime:   Var[Boolean]      = Var(true)
  ) { state =>
    def toJsObject: JsState = new JsState {
      val currentTab: js.UndefOr[String] = state.currentTab.get.label
      val imageQuality: js.UndefOr[String] = state.imageQuality.get.label
      val showUserImages: js.UndefOr[Boolean] = state.showUserImages.get
      val relativeTime: js.UndefOr[Boolean] = state.relativeTime.get
    }
  }

  object State {
    def fromJsObject(jsState: JsState): State = State(
      currentTab = Var(fromField(jsState.currentTab, DialogTab.fromString, DialogTab.Follow)),
      imageQuality = Var(fromField(jsState.imageQuality, ImageQuality.fromString, ImageQuality.Off)),
      showUserImages = Var(jsState.showUserImages.getOrElse(false)),
      relativeTime = Var(jsState.relativeTime.getOrElse(true))
    )
  }

  private def fromField[T, U](jsField: js.UndefOr[T], f: T => Option[U], default: U): U = {
    jsField.toOption.flatMap(f).getOrElse(default)
  }

  @ScalaJSDefined
  trait JsState extends js.Object {
    def currentTab: js.UndefOr[String]
    def imageQuality: js.UndefOr[String]
    def showUserImages: js.UndefOr[Boolean]
    def relativeTime: js.UndefOr[Boolean]
  }
}

