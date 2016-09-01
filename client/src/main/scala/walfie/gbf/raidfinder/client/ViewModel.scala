package walfie.gbf.raidfinder.client

import com.thoughtworks.binding.Binding._

object ViewModel {
  sealed abstract class DialogTab(val label: String)
  object DialogTab {
    case object Follow extends DialogTab("Follow")
    case object Settings extends DialogTab("Settings")
  }

  sealed abstract class ImageQuality(val label: String)
  object ImageQuality {
    case object Off extends ImageQuality("Off")
    case object Low extends ImageQuality("Low")
    case object High extends ImageQuality("High")
  }

  case class State(
    currentTab:        Var[DialogTab],
    imageQuality:      Var[ImageQuality],
    displayUserImages: Var[Boolean],
    relativeTime:      Var[Boolean]
  )
}

