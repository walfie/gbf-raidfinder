package walfie.gbf.raidfinder.client.util

import java.util.Date
import scala.scalajs.js

package time {
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

package object time {
  val MomentShortLocale: js.Dictionary[js.Any] = js.Dictionary(
    "parentLocale" -> "en",
    "relativeTime" -> js.Dictionary(
      "future" -> "in %s",
      "past" -> "%s ago",
      "s" -> "now",
      "ss" -> "%ss",
      "m" -> "1m",
      "mm" -> "%dm",
      "h" -> "1h",
      "hh" -> "%dh",
      "d" -> "1d",
      "dd" -> "%dd",
      "M" -> "1M",
      "MM" -> "%dM",
      "y" -> "1Y",
      "yy" -> "%dY"
    )
  )
}

