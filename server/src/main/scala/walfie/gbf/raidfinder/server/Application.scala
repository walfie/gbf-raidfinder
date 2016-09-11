package walfie.gbf.raidfinder.server

import akka.actor._
import com.typesafe.config.{Config, ConfigFactory}
import java.net.URI
import net.ceedubs.ficus.Ficus._
import play.api.mvc._
import play.api.{Logger, Mode}
import play.core.server._
import scala.concurrent.duration._
import walfie.gbf.raidfinder.domain
import walfie.gbf.raidfinder.protocol._
import walfie.gbf.raidfinder.RaidFinder
import walfie.gbf.raidfinder.server.persistence._
import walfie.gbf.raidfinder.server.syntax.ProtocolConverters._
import walfie.gbf.raidfinder.util.BlockingIO

object Application {
  def main(args: Array[String]): Unit = {
    implicit val scheduler = monix.execution.Scheduler.Implicits.global

    val config = ConfigFactory.load()
    val appConfig = config.getConfig("application")

    // Get initial bosses from cache
    val protobufStorage = {
      val url = appConfig.as[Option[String]]("cache.redisUrl").filter(_.nonEmpty)
      getProtobufStorage(url)
    }
    val bossCacheKey = appConfig.as[String]("cache.bossesKey")
    val bossFlushInterval = appConfig.as[FiniteDuration]("cache.flushInterval")
    val cachedBosses = getCachedBosses(protobufStorage, bossCacheKey)

    // Start RaidFinder
    val raidFinder = RaidFinder.withBacklog(initialBosses = cachedBosses)

    // Periodically flush bosses to cache
    scheduler.scheduleWithFixedDelay(bossFlushInterval, bossFlushInterval) {
      val bosses = raidFinder.getKnownBosses().values.map(_.toProtocol)
      val bossesResponse = RaidBossesResponse(raidBosses = bosses.toSeq)
      BlockingIO.future(protobufStorage.set(bossCacheKey, bossesResponse))
    }

    // Start server
    val port = config.as[Int]("http.port")
    val mode = getMode(appConfig.as[String]("mode"))
    val keepAliveInterval = appConfig.as[FiniteDuration]("websocket.keepAliveInterval")
    val components = new Components(raidFinder, port, mode, keepAliveInterval)
    val server = components.server

    // Shutdown handling
    val shutdown = () => {
      server.stop()
      protobufStorage.close()
    }

    if (mode == Mode.Dev) {
      Logger.info("Press ENTER to stop the application.")
      scala.io.StdIn.readLine()
      Logger.info("Stopping application...")
      shutdown()
      Logger.info("Application stopped.")
    }

    Runtime.getRuntime.addShutdownHook(new Thread() {
      override def run(): Unit = shutdown()
    })
  }

  def getMode(s: String): Mode.Mode = s match {
    case "dev" => Mode.Dev
    case "prod" => Mode.Prod
    case unknown => throw new IllegalArgumentException(
      s"""Unknown application.mode "$unknown" (Must be one of: dev, prod)"""
    )
  }

  def getProtobufStorage(redisUrl: Option[String]): ProtobufStorage = {
    redisUrl.fold[ProtobufStorage](NoOpProtobufStorage) { url =>
      ProtobufStorage.redis(new URI(url))
    }
  }

  def getCachedBosses(storage: ProtobufStorage, key: String): Seq[domain.RaidBoss] = {
    storage
      .get[RaidBossesResponse](key)
      .fold(Seq.empty[domain.RaidBoss])(_.raidBosses.map(_.toDomain))
  }
}

