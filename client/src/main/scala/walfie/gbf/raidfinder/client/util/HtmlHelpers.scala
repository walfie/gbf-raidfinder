package walfie.gbf.raidfinder.client.util

import org.scalajs.dom.experimental.{Notification, NotificationOptions}
import org.scalajs.dom.raw._
import org.scalajs.dom.{console, document}
import scala.annotation.tailrec
import scala.scalajs.js
import scala.util.{Success, Failure, Try}

object HtmlHelpers {
  val BlankImage = "data:image/gif;base64,R0lGODlhAQABAAAAACH5BAEKAAEALAAAAAABAAEAAAICTAEAOw=="

  @tailrec
  def findParent(element: Element, predicate: Element => Boolean): Option[Element] = {
    if (predicate(element)) Some(element)
    else Option(element.parentNode) match {
      case Some(parentElement: Element) => findParent(parentElement, predicate)
      case _ => None
    }
  }

  def requestNotificationPermission(onSuccess: => Unit = ()): Unit = {
    if (Notification.permission == "granted")
      onSuccess
    else Notification.requestPermission { result: String =>
      if (result == "granted") onSuccess
    }
  }

  def desktopNotification(
    title:        String,
    body:         js.UndefOr[String],
    icon:         js.UndefOr[String],
    tag:          js.UndefOr[String],
    onClick:      Event => Unit,
    closeOnClick: Boolean
  ): Unit = {
    requestNotificationPermission {
      val options = NotificationOptions(body = body, icon = icon, tag = tag)
      val notification = new Notification(title, options)
      notification.asInstanceOf[js.Dynamic].onclick = { e: Event =>
        onClick(e)
        if (closeOnClick) notification.close()
      }
    }
  }

  /** Create an empty textarea, select the text inside, and copy to clipboard */
  def copy(stringToCopy: String): Boolean = {
    // If the user has already selected something, store the selection
    val selection = document.getSelection()
    val selectedRange =
      if (selection.rangeCount > 0) Some(selection.getRangeAt(0))
      else None

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

    // Restore the user's previous selection, if any
    selectedRange.foreach { range =>
      selection.removeAllRanges()
      selection.addRange(range)
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
}

