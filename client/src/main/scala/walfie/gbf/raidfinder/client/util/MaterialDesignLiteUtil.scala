package walfie.gbf.raidfinder.client.util

import org.scalajs.dom.raw.HTMLElement
import scala.scalajs.js

object MaterialDesignLiteUtil {
  implicit class HTMLElementOps[T <: HTMLElement](val elem: T) extends AnyVal {
    def mdl(): T = {
      // This is such a hack
      js.timers.setTimeout(1000) {
        js.Dynamic.global.componentHandler.upgradeElement(elem)
      }
      elem
    }
  }
}

