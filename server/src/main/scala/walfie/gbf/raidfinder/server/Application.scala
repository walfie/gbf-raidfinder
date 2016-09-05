package walfie.gbf.raidfinder.server

import akka.actor._
import akka.stream.ActorMaterializer
import monix.execution.Scheduler.Implicits.global
import play.api.BuiltInComponents
import play.api.http.DefaultHttpErrorHandler
import play.api.mvc._
import play.api.routing.Router
import play.api.routing.sird._
import play.core.server._
import play.core.server.NettyServerComponents
import scala.concurrent.Future
import scala.util.control.NonFatal
import walfie.gbf.raidfinder
import walfie.gbf.raidfinder.RaidFinder

object Application {
  def main(args: Array[String]): Unit = {
    val raidFinder = RaidFinder.withBacklog()

    val components = new Components(raidFinder)
    val server = components.server

    Runtime.getRuntime.addShutdownHook(new Thread() {
      override def run = {
        println("Stopping application.")
        server.stop()
      }
    })
  }
}

