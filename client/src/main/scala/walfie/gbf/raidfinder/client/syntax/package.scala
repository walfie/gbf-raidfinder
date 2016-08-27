package walfie.gbf.raidfinder.client

import com.thoughtworks.binding
import com.thoughtworks.binding.Binding
import com.thoughtworks.binding.Binding._
import org.scalajs.dom.raw.HTMLElement
import scala.collection.mutable.Buffer
import scala.scalajs.js

import js.Dynamic.global

package object syntax {
  implicit class HTMLElementOps[T <: HTMLElement](val elem: T) extends AnyVal {
    /** Upgrade element to a Material Design Lite JS-enabled element */
    def mdl(): T = {
      (1 to 10).foreach { i =>
        js.timers.setTimeout(i * 500) { // This is such a hack (and doesn't even work)
          global.componentHandler.upgradeElement(elem)
        }
      }

      elem
    }

    /** Add a cover background image, slightly darkened */
    def backgroundImage(imageUrl: String, opacity: Double, cover: Boolean): T = {
      val color = s"rgba(0, 0, 0, $opacity)"
      elem.style.backgroundImage = s"linear-gradient($color, $color), url('$imageUrl')"
      if (cover) elem.style.backgroundSize = "cover"
      elem
    }
  }

  implicit class BufferOps[T](val buffer: Buffer[T]) extends AnyVal {
    def :=(elements: TraversableOnce[T]) = {
      buffer.clear()
      buffer ++= elements
    }
  }
}

