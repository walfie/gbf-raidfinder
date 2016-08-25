package walfie.gbf.raidfinder.client.util

import org.scalajs.dom.raw.HTMLElement
import scala.scalajs.js

object MaterialDesignLiteUtil {
  implicit class HTMLElementOps[T <: HTMLElement](val elem: T) extends AnyVal {
    def mdl(): T = {
      // This is such a hack
      (1 to 10).foreach { i =>
        js.timers.setTimeout(i * 500) {
          js.Dynamic.global.componentHandler.upgradeElement(elem)
        }
      }

      elem
    }
  }
}

