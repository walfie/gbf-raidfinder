package walfie.gbf.raidfinder.server

import akka.actor.ActorSystem
import akka.stream.Materializer
import com.trueaccord.scalapb.json.JsonFormat
import play.api.BuiltInComponents
import play.api.http.{ContentTypes, DefaultHttpErrorHandler}
import play.api.Mode.Mode
import play.api.mvc._
import play.api.routing.Router
import play.api.routing.sird._
import play.core.server._
import play.filters.gzip.GzipFilterComponents
import scala.concurrent.duration.FiniteDuration
import scala.concurrent.Future
import walfie.gbf.raidfinder.protocol.RaidBossesResponse
import walfie.gbf.raidfinder.RaidFinder
import walfie.gbf.raidfinder.server.controller._
import walfie.gbf.raidfinder.server.syntax.ProtocolConverters.RaidBossDomainOps

class Components(
  raidFinder:                 RaidFinder,
  translator:                 BossNameTranslator,
  port:                       Int,
  mode:                       Mode,
  websocketKeepAliveInterval: FiniteDuration
) extends NettyServerComponents
  with BuiltInComponents with GzipFilterComponents with Controller {

  override lazy val serverConfig = ServerConfig(port = Some(port), mode = mode)

  override lazy val httpFilters = List(gzipFilter)

  lazy val websocketController = new WebsocketController(
    raidFinder, translator, websocketKeepAliveInterval
  )(actorSystem, materializer)

  lazy val router = Router.from {
    case GET(p"/") =>
      controllers.Assets.at(path = "/public", "index.html")

    case GET(p"/api/bosses.json") =>
      val bosses = raidFinder.getKnownBosses.values.map(_.toProtocol(translator))
      val responseProtobuf = RaidBossesResponse(raidBosses = bosses.toSeq)
      val responseJson = JsonFormat.toJsonString(responseProtobuf)
      Action(Ok(responseJson).as(ContentTypes.JSON))

    case GET(p"/ws/raids" ? q_o"keepAlive=${ bool(keepAlive) }") =>
      websocketController.raids(keepAlive = keepAlive.getOrElse(false))

    case GET(p"/$file*") =>
      controllers.Assets.at(path = "/public", file = file)
  }

  override lazy val httpErrorHandler = new ErrorHandler

  override def serverStopHook = () => Future.successful {
    raidFinder.shutdown()
    actorSystem.terminate()
  }
}

