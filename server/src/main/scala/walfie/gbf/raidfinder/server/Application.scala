package walfie.gbf.raidfinder.server

import com.typesafe.config.{Config, ConfigFactory}
import java.net.URI
import java.util.Date
import net.ceedubs.ficus.Ficus._
import net.ceedubs.ficus.readers.ArbitraryTypeReader._
import play.api.{Logger, Mode}
import scala.concurrent.duration._
import scala.concurrent.Future
import walfie.gbf.raidfinder.domain
import walfie.gbf.raidfinder.protocol._
import walfie.gbf.raidfinder.protocol.syntax.ResponseOps
import walfie.gbf.raidfinder.server.persistence._
import walfie.gbf.raidfinder.server.syntax.ProtocolConverters._
import walfie.gbf.raidfinder.server.{ImageBasedBossNameTranslator => translator}
import walfie.gbf.raidfinder.util.BlockingIO
import walfie.gbf.raidfinder.{RaidFinder, StatusParser}

object Application {
  def main(args: Array[String]): Unit = {
    implicit val scheduler = monix.execution.Scheduler.Implicits.global

    val config = ConfigFactory.load()
    val appConfig = config.getConfig("application")
    val bossStorageConfig = appConfig.as[BossStorageConfig]("bosses")
    val translationsConfig = appConfig.as[TranslationsConfig]("translations")

    // Get initial data from cache
    val protobufStorage = {
      val url = appConfig.as[Option[String]]("cache.redisUrl").filter(_.nonEmpty)
      getProtobufStorage(url)
    }

    // Start RaidFinder
    val raidFinder = {
      implicit val fromRaidTweet: domain.FromRaidTweet[BinaryProtobuf] =
        domain.FromRaidTweet { raidTweet =>
          BinaryProtobuf(raidTweet.toProtocol.toMessage.toByteArray)
        }
      val initialBosses = getCachedBosses(protobufStorage, bossStorageConfig.cacheKey)
      RaidFinder.withBackfill(initialBosses = initialBosses)
    }

    val translator = new ImageBasedBossNameTranslator(
      initialTranslationData = getCachedTranslationData(protobufStorage, translationsConfig.cacheKey),
      manualOverrides = translationsConfig.overrides
    )

    // Periodically flush bosses to cache
    val bossFlushCancelable = scheduler.scheduleWithFixedDelay(
      bossStorageConfig.flushInterval, bossStorageConfig.flushInterval
    ) {
      // Purge old bosses and then update the cache
      val purgeMinDate = new Date(System.currentTimeMillis() - bossStorageConfig.ttl.toMillis)

      for {
        domainBosses <- raidFinder.purgeOldBosses(
          minDate = purgeMinDate,
          levelThreshold = Some(bossStorageConfig.levelThreshold)
        )

        // If maxTtl is specified, do another purge, ignoring boss level
        domainBosses0 <- bossStorageConfig.maxTtl.fold(
          Future.successful(domainBosses)
        ) { maxTtl =>
            raidFinder.purgeOldBosses(
              minDate = new Date(System.currentTimeMillis() - maxTtl.toMillis),
              levelThreshold = None
            )
          }

        protocolBosses = domainBosses0.values.map { boss =>
          boss.toProtocol(translator.translate(boss.name))
        }

        cacheObj = RaidBossesItem(raidBosses = protocolBosses.toSeq)
        _ <- BlockingIO.future(protobufStorage.set(bossStorageConfig.cacheKey, cacheObj))
      } yield ()
    }

    val translatorNewBossCancelable = raidFinder.newBossObservable.foreach { boss =>
      translator.update(Map(boss.name -> boss))
    }

    // Periodically update new translations and save translation data to cache
    val translationRefreshCancelable = scheduler.scheduleWithFixedDelay(
      Duration.Zero, translationsConfig.refreshInterval
    ) {
      for {
        _ <- translator.update(raidFinder.getKnownBosses())
        bossData = translator.getTranslationData.values.map(_.toProtocol).toSeq
        cacheObj = TranslationDataItem(data = bossData)
        _ <- BlockingIO.future(protobufStorage.set(translationsConfig.cacheKey, cacheObj))
      } yield ()
    }

    // Start server
    val port = config.as[Int]("http.port")
    val mode = getMode(appConfig.as[String]("mode"))
    val keepAliveInterval = appConfig.as[FiniteDuration]("websocket.keepAliveInterval")
    val metricsCollector = new MetricsCollectorImpl
    val components = new Components(raidFinder, translator, port, mode, keepAliveInterval, metricsCollector)
    val server = components.server

    // Shutdown handling
    val shutdown = () => {
      raidFinder.shutdown()
      server.stop()
      translatorNewBossCancelable.cancel()
      bossFlushCancelable.cancel()
      translationRefreshCancelable.cancel()
      protobufStorage.close()
    }

    handleShutdown(mode, shutdown)
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
      .get[RaidBossesItem](key)
      .fold(Seq.empty[domain.RaidBoss])(_.raidBosses.map(_.toDomain))
      .filter(boss => StatusParser.isValidName(boss.name))
  }

  def getCachedTranslationData(storage: ProtobufStorage, key: String): Seq[translator.TranslationData] = {
    storage
      .get[TranslationDataItem](key)
      .fold(Seq.empty[translator.TranslationData])(_.data.map(_.toDomain))
      .filter(data => StatusParser.isValidName(data.name))
  }

  def handleShutdown(mode: Mode.Mode, shutdown: () => Unit) = {
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
}

case class BossStorageConfig(
  cacheKey:       String,
  ttl:            FiniteDuration,
  maxTtl:         Option[FiniteDuration],
  flushInterval:  FiniteDuration,
  levelThreshold: Int
)

case class TranslationsConfig(
  cacheKey:        String,
  refreshInterval: FiniteDuration,
  overrides:       Map[BossName, BossName]
)

