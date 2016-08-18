package com.github.walfie.granblue.raidfinder.server

import play.api.http._
import play.api.libs.json
import play.api.mvc._
import scala.concurrent.Future

class ErrorHandler extends HttpErrorHandler {
  def onClientError(
    request:    RequestHeader,
    statusCode: Int,
    message:    String
  ): Future[Result] = {
    ???
  }

  def onServerError(
    request:   RequestHeader,
    exception: Throwable
  ): Future[Result] = {
    ???
  }
}

