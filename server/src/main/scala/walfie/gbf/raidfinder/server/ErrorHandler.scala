package walfie.gbf.raidfinder.server

import play.api.http.HttpErrorHandler
import play.api.libs.json._
import play.api.mvc._
import play.api.mvc.Results._
import scala.concurrent.Future

class ErrorHandler extends HttpErrorHandler {
  // TODO: Make this more structured
  private def jsonResponse(statusCode: Int, message: String): JsObject =
    Json.obj(
      "errors" -> Json.arr(
        Json.obj(
          "status" -> statusCode,
          "detail" -> message
        )
      )
    )

  def onClientError(
    request:    RequestHeader,
    statusCode: Int,
    message:    String
  ): Future[Result] = Future.successful {
    Status(statusCode)(jsonResponse(statusCode, message))
  }

  def onServerError(
    request:   RequestHeader,
    exception: Throwable
  ): Future[Result] = Future.successful {
    InternalServerError(jsonResponse(500, "Internal server error"))
  }
}

