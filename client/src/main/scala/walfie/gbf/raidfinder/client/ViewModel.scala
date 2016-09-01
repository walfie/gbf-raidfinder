package walfie.gbf.raidfinder.client

import com.thoughtworks.binding.Binding._

object ViewModel {
  sealed abstract class DialogTab(val label: String) {
    def id: String = s"gbfrf-dialog__$label"
  }
  object DialogTab {
    case object Follow extends DialogTab("Follow")
    case object Settings extends DialogTab("Settings")

    val all: List[DialogTab] = List(Follow, Settings)
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

  case class State(
    currentTab:     Var[DialogTab]    = Var(DialogTab.Follow),
    imageQuality:   Var[ImageQuality] = Var(ImageQuality.Off),
    showUserImages: Var[Boolean]      = Var(false),
    relativeTime:   Var[Boolean]      = Var(true)
  )
}

