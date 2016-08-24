package walfie.gbf.raidfinder.client.util

import org.scalajs.dom.raw.HTMLElement
import scala.scalajs.js

object MaterialDesignLiteUtil {
  implicit class HTMLElementOps[T <: HTMLElement](val elem: T) extends AnyVal {
    def mdl(): T = {
      js.Dynamic.global.componentHandler.upgradeElement(elem)
      elem
    }
  }
}

