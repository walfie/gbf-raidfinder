package walfie.gbf.raidfinder.server

import akka.actor._
import akka.stream.ActorMaterializer
import com.typesafe.config.ConfigFactory
import java.util.concurrent.TimeUnit
import monix.execution.Scheduler.Implicits.global
import play.api.http.DefaultHttpErrorHandler
import play.api.mvc._
import play.api.routing.Router
import play.api.routing.sird._
import play.api.{BuiltInComponents, Logger, Mode}
import play.core.server._
import play.core.server.NettyServerComponents
import scala.concurrent.duration._
import scala.concurrent.Future
import scala.util.control.NonFatal
import walfie.gbf.raidfinder
import walfie.gbf.raidfinder.RaidFinder

object Application {
  def main(args: Array[String]): Unit = {
    val raidFinder = RaidFinder.withBacklog()

    val config = ConfigFactory.load()
    val port = config.getInt("http.port")

    val mode = getMode(config.getString("application.mode"))
    val keepAliveInterval = config.getDuration(
      "application.websocket.keepAliveInterval",
      TimeUnit.MILLISECONDS
    ).milliseconds
    val components = new Components(raidFinder, port, mode, keepAliveInterval)
    val server = components.server

    if (mode == Mode.Dev) {
      Logger.info("Press ENTER to stop the application.")
      scala.io.StdIn.readLine()
      Logger.info("Stopping application...")
      server.stop()
      Logger.info("Application stopped.")
    }

    Runtime.getRuntime.addShutdownHook(new Thread() {
      override def run(): Unit = server.stop()
    })
  }

  def getMode(s: String): Mode.Mode = s match {
    case "dev" => Mode.Dev
    case "prod" => Mode.Prod
    case unknown => throw new IllegalArgumentException(
      s"""Unknown application.mode "$unknown" (Must be one of: dev, prod)"""
    )
  }
}

