package walfie.gbf.raidfinder.server

import akka.actor.ActorSystem
import akka.stream.Materializer
import play.api.BuiltInComponents
import play.api.http.DefaultHttpErrorHandler
import play.api.mvc._
import play.api.routing.Router
import play.api.routing.sird._
import play.core.server._
import scala.concurrent.Future
import walfie.gbf.raidfinder.RaidFinder
import walfie.gbf.raidfinder.server.controller._

class Components(val raidFinder: RaidFinder, port: Int) extends NettyServerComponents
  with BuiltInComponents with Controller with RaidFinderControllers {

  override lazy val serverConfig = ServerConfig(port = Some(port))

  lazy val router = Router.from {
    case GET(p"/") => controllers.Assets.at(path = "/public", "index.html") // Temporary
    case GET(p"/ws/raids") => websocketController.raids
    case GET(p"/$file*") => controllers.Assets.at(path = "/public", file = file)
  }

  override lazy val httpErrorHandler = new ErrorHandler

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

