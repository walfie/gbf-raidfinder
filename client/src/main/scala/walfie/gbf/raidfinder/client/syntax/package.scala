package walfie.gbf.raidfinder.client

import org.scalajs.dom.raw.HTMLElement
import scala.scalajs.js

import js.Dynamic.global

package object syntax {
  implicit class HTMLElementOps[T <: HTMLElement](val elem: T) extends AnyVal {
    /** Upgrade element to a Material Design Lite JS-enabled element */
    def mdl(): T = {
      (1 to 10).foreach { i =>
        js.timers.setTimeout(i * 500) { // This is such a hack
          global.componentHandler.upgradeElement(elem)
        }
      }

      elem
    }

    /** Add a cover background image, slightly darkened */
    def backgroundImage(imageUrl: String, opacity: Double): T = {
      val color = s"rgba(0, 0, 0, $opacity)"
      elem.style.background = s"linear-gradient($color, $color), url('$imageUrl')"
      elem.style.backgroundSize = "cover"
      elem
    }
  }
}

