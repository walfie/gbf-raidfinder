package walfie.gbf.raidfinder.client

import java.util.Date
import org.scalajs.dom.raw._
import org.scalajs.dom.{console, document}
import scala.annotation.tailrec
import scala.util.{Success, Failure, Try}

object Util {
  @tailrec
  def findParent(element: Element, predicate: Element => Boolean): Option[Element] = {
    if (predicate(element)) Some(element)
    else Option(element.parentNode) match {
      case Some(parentElement: Element) => findParent(parentElement, predicate)
      case _ => None
    }
  }

  def copy(stringToCopy: String): Boolean = {
    val textArea = createInvisibleTextArea()
    textArea.value = stringToCopy
    document.body.appendChild(textArea)
    textArea.select()

    val result = try {
      document.execCommand("copy")
    } catch {
      case e: Throwable => false // TODO: Maybe don't swallow this exception
    } finally {
      document.body.removeChild(textArea)
    }

    result
  }

  private def createInvisibleTextArea(): HTMLTextAreaElement = {
    val textArea = document.createElement("textarea").asInstanceOf[HTMLTextAreaElement]
    val s = textArea.style

    s.position = "fixed"
    s.top = "0"
    s.left = "0"
    s.width = "2em"
    s.height = "2em"
    s.padding = "0"
    s.border = "none"
    s.outline = "none"
    s.boxShadow = "none"
    s.background = "transparent"

    textArea
  }

  // TODO: Move this somewhere better
  trait Clock { def now(): Date }
  object SystemClock extends Clock { def now(): Date = new Date() }

  case class Duration(milliseconds: Long) extends AnyVal
  object Duration {
    def seconds(s: Long): Duration = Duration(s * 1000)
    def minutes(m: Long): Duration = Duration(m * 60 * 1000)
    def hours(h: Long): Duration = Duration(h * 3600 * 1000)
    def days(d: Long): Duration = Duration(d * 24 * 3600 * 1000)
  }
}

