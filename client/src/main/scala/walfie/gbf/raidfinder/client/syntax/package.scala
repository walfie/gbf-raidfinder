package walfie.gbf.raidfinder.client

import com.thoughtworks.binding
import com.thoughtworks.binding.Binding
import com.thoughtworks.binding.Binding._
import org.scalajs.dom
import org.scalajs.dom.raw._
import scala.collection.mutable.Buffer
import scala.scalajs.js
import walfie.gbf.raidfinder.client.ViewModel.ImageQuality
import walfie.gbf.raidfinder.protocol._

import js.Dynamic.global

package object syntax {
  implicit class HTMLElementOps[T <: HTMLElement](val elem: T) extends AnyVal {
    /** Upgrade element to a Material Design Lite JS-enabled element */
    def mdl(): T = {
      // This is such a hack
      js.timers.setTimeout(1000)(global.componentHandler.upgradeAllRegistered())

      elem
    }

    /** Add a cover background image, slightly darkened */
    def backgroundImage(image: Option[String], opacity: Double): T = {
      image match {
        case None => elem.style.backgroundImage = ""
        case Some(imageUrl) =>
          val img = dom.document.createElement("img").asInstanceOf[HTMLImageElement]
          val color = s"rgba(0, 0, 0, $opacity)"
          elem.style.backgroundImage = s"linear-gradient($color, $color), url('$imageUrl')"
      }

      elem
    }

    def backgroundImageQuality(image: Option[String], opacity: Double, quality: ImageQuality): T = {
      val imageOpt = if (quality == ImageQuality.Off) None else image
      backgroundImage(imageOpt.map(_ + quality.suffix), opacity)
    }
  }

  implicit class ElementOps[T <: Element](val elem: T) extends AnyVal {
    import walfie.gbf.raidfinder.client.util.HtmlHelpers

    def findParent(predicate: Element => Boolean): Option[Element] =
      HtmlHelpers.findParent(elem, predicate)
  }

  implicit class EventOps(val event: Event) extends AnyVal {
    def targetElement(): Option[Element] = Option(event.target) match {
      case Some(e: Element) => Some(e)
      case _ => None
    }
  }

  implicit class BufferOps[T](val buffer: Buffer[T]) extends AnyVal {
    /** Overwrite the contents of a buffer */
    def :=(elements: TraversableOnce[T]): Buffer[T] = {
      buffer.clear()
      buffer ++= elements
    }
  }

  implicit class StringOps(val string: String) extends AnyVal {
    def addIf(condition: Boolean, s: String): String =
      if (condition) s"$string $s" else string
  }

  implicit class LanguageOps(val language: Language) extends AnyVal {
    def shortName: Option[String] = language match {
      case Language.JAPANESE => Some("JP")
      case Language.ENGLISH => Some("EN")
      case _ => None
    }
  }
}

