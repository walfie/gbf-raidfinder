package com.momentjs

import scala.scalajs.js
import scala.scalajs.js.annotation.JSName

@js.native
@JSName("moment")
object Moment extends js.Object {
  def apply(millis: Double): Date = js.native

  def locale(localeName: String): Unit = js.native
  def defineLocale(localeName: String, settings: js.Dictionary[js.Any]): Unit = js.native
}

@js.native
trait Date extends js.Object {
  def fromNow(): String = js.native
  def fromNow(withoutSuffix: Boolean): String = js.native
  def from(millis: Double, withoutSuffix: Boolean): String = js.native
  def format(stringFormat: String): String = js.native
}

