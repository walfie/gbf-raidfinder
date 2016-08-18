package com.github.walfie.granblue.raidfinder.server

import akka.actor.ActorSystem
import akka.stream.Materializer
import com.github.walfie.granblue.raidfinder.RaidFinder
import com.github.walfie.granblue.raidfinder.server.controller._
import play.api.BuiltInComponents
import play.api.http.DefaultHttpErrorHandler
import play.api.mvc._
import play.api.routing.Router
import play.api.routing.sird._
import play.core.server._
import scala.concurrent.Future

class Components(val raidFinder: RaidFinder) extends NettyServerComponents
  with BuiltInComponents with Controller with RaidFinderControllers {

  lazy val router = Router.from {
    case GET(p"/ws/raids") => websocketController.raids
  }

  override lazy val httpErrorHandler = new ErrorHandler

  // TODO: Shut down actors, etc
  override def serverStopHook = () => Future.successful {
    raidFinder.shutdown()
    actorSystem.terminate()
  }
}

trait RaidFinderControllers {
  def raidFinder: RaidFinder
  implicit def actorSystem: ActorSystem
  implicit def materializer: Materializer

  lazy val websocketController = new WebsocketController(raidFinder)
}

