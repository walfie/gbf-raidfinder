package walfie.gbf.raidfinder.client

import com.thoughtworks.binding.Binding._
import org.scalajs.dom
import scala.scalajs.js
import scala.scalajs.js.annotation.ScalaJSDefined
import walfie.gbf.raidfinder.protocol.BossName

object ViewModel {
  trait Labeled {
    def label: String
    def id: String
  }

  sealed abstract class DialogTab(val label: String, val icon: String) extends Labeled {
    def id: String = s"gbfrf-dialog__$label"
  }
  object DialogTab {
    case object Follow extends DialogTab("Follow", "add")
    case object Settings extends DialogTab("Settings", "settings")

    val all: List[DialogTab] = List(Follow, Settings)
    val fromString: String => Option[DialogTab] =
      all.map(tab => tab.label -> tab).toMap.get _
  }

  sealed abstract class ImageQuality(val label: String, val suffix: String) extends Labeled {
    def id: String = s"gbfrf-settings__image-quality--$label"
  }
  object ImageQuality {
    case object Off extends ImageQuality("Off", "")
    case object Low extends ImageQuality("Low", ":thumb")
    case object High extends ImageQuality("High", ":small")

    val Default = Off
    val all = List(Off, Low, High)
    val fromString: String => Option[ImageQuality] =
      all.map(q => q.label -> q).toMap.get _
  }

  sealed abstract class TimeFormat(val label: String) extends Labeled {
    def id: String = s"gbfrf-settings__time-format--$label"
  }
  object TimeFormat {
    case object Relative extends TimeFormat("Relative")
    case object TwelveHour extends TimeFormat("12H")
    case object TwentyFourHour extends TimeFormat("24H")

    val Default = Relative
    val all: List[TimeFormat] = List(Relative, TwelveHour, TwentyFourHour)
    val fromString: String => Option[TimeFormat] =
      all.map(format => format.label -> format).toMap.get _
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
    currentTab:       Var[DialogTab]    = Var(DialogTab.Follow),
    imageQuality:     Var[ImageQuality] = Var(ImageQuality.Default),
    timeFormat:       Var[TimeFormat]   = Var(TimeFormat.Default),
    showUserImages:   Var[Boolean]      = Var(false),
    nightMode:        Var[Boolean]      = Var(false),
    columnWidthScale: Var[Double]       = Var(1.0)
  ) { state =>
    def toJsObject: JsState = new JsState {
      val currentTab: js.UndefOr[String] = state.currentTab.get.label
      val imageQuality: js.UndefOr[String] = state.imageQuality.get.label
      val timeFormat: js.UndefOr[String] = state.timeFormat.get.label
      val showUserImages: js.UndefOr[Boolean] = state.showUserImages.get
      val nightMode: js.UndefOr[Boolean] = state.nightMode.get
      val columnWidthScale: js.UndefOr[Double] = state.columnWidthScale.get
    }
  }

  object State {
    def fromJsObject(jsState: JsState): State = State(
      currentTab = Var(fromField(jsState.currentTab, DialogTab.fromString, DialogTab.Follow)),
      imageQuality = Var(fromField(jsState.imageQuality, ImageQuality.fromString, ImageQuality.Default)),
      timeFormat = Var(fromField(jsState.timeFormat, TimeFormat.fromString, TimeFormat.Default)),
      showUserImages = Var(jsState.showUserImages.getOrElse(false)),
      nightMode = Var(jsState.nightMode.getOrElse(false)),
      columnWidthScale = Var(jsState.columnWidthScale.getOrElse(1.0))
    )
  }

  private def fromField[T, U](jsField: js.UndefOr[T], f: T => Option[U], default: U): U = {
    jsField.toOption.flatMap(f).getOrElse(default)
  }

  @ScalaJSDefined
  trait JsState extends js.Object {
    def currentTab: js.UndefOr[String]
    def imageQuality: js.UndefOr[String]
    def timeFormat: js.UndefOr[String]
    def showUserImages: js.UndefOr[Boolean]
    def nightMode: js.UndefOr[Boolean]
    def columnWidthScale: js.UndefOr[Double]
  }
}

