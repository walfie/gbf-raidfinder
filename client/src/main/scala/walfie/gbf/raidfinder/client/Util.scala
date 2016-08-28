package walfie.gbf.raidfinder.client

import scala.annotation.tailrec
import org.scalajs.dom.raw._

object Util {
  @tailrec
  def findParent(element: Element, predicate: Element => Boolean): Option[Element] = {
    if (predicate(element)) Some(element)
    else Option(element.parentNode) match {
      case Some(parentElement: Element) => findParent(parentElement, predicate)
      case _ => None
    }
  }
}

