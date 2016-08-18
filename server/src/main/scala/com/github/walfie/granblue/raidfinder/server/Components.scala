package com.github.walfie.granblue.raidfinder.server

import play.api.BuiltInComponents
import play.api.http.DefaultHttpErrorHandler
import play.api.mvc._
import play.api.routing.Router
import play.api.routing.sird._
import play.core.server._
import scala.concurrent.Future

class Components extends NettyServerComponents with BuiltInComponents {
  lazy val router = Router.from {
    case GET(p"/ws/raids") => ???
  }

  override lazy val httpErrorHandler = new ErrorHandler

  // TODO: Shut down actors, etc
  override def serverStopHook = () => Future.successful(())
}

