package walfie.gbf.raidfinder.server

import akka.actor.ActorSystem
import akka.stream.Materializer
import play.api.BuiltInComponents
import play.api.http.DefaultHttpErrorHandler
import play.api.Mode.Mode
import play.api.mvc._
import play.api.routing.Router
import play.api.routing.sird._
import play.core.server._
import play.filters.gzip.GzipFilterComponents
import scala.concurrent.Future
import walfie.gbf.raidfinder.RaidFinder
import walfie.gbf.raidfinder.server.controller._

class Components(val raidFinder: RaidFinder, port: Int, mode: Mode) extends NettyServerComponents
  with BuiltInComponents with GzipFilterComponents with Controller with RaidFinderControllers {

  override lazy val serverConfig = ServerConfig(port = Some(port), mode = mode)

  override lazy val httpFilters = List(gzipFilter)

  lazy val router = Router.from {
    case GET(p"/") => controllers.Assets.at(path = "/public", "index.html")
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

