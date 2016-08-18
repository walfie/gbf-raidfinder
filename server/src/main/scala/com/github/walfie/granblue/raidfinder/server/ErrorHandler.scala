package com.github.walfie.granblue.raidfinder.server

import play.api.http.HttpErrorHandler
import play.api.libs.json._
import play.api.mvc._
import play.api.mvc.Results._
import scala.concurrent.Future

class ErrorHandler extends HttpErrorHandler {
  // TODO: Make this more structured
  private def jsonResponse(error: String): JsObject =
    Json.obj("errors" -> Seq(error))

  def onClientError(
    request:    RequestHeader,
    statusCode: Int,
    message:    String
  ): Future[Result] = Future.successful {
    Status(statusCode)(jsonResponse(message))
  }

  def onServerError(
    request:   RequestHeader,
    exception: Throwable
  ): Future[Result] = Future.successful {
    InternalServerError(jsonResponse("Internal server error"))
  }
}

