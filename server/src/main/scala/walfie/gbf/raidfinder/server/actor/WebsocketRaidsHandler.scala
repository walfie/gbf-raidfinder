package walfie.gbf.raidfinder.server.actor

import akka.actor._
import monix.execution.{Cancelable, Scheduler}
import scala.concurrent.duration.FiniteDuration
import walfie.gbf.raidfinder.BuildInfo
import walfie.gbf.raidfinder.domain._
import walfie.gbf.raidfinder.protocol
import walfie.gbf.raidfinder.protocol.syntax._
import walfie.gbf.raidfinder.protocol.{RaidBoss => _, _}
import walfie.gbf.raidfinder.RaidFinder
import walfie.gbf.raidfinder.server.{BossNameTranslator, MetricsCollector}
import walfie.gbf.raidfinder.server.syntax.ProtocolConverters.{RaidBossDomainOps, RaidTweetDomainOps}

class WebsocketRaidsHandler(
  out:               ActorRef,
  raidFinder:        RaidFinder[BinaryProtobuf],
  translator:        BossNameTranslator,
  keepAliveInterval: Option[FiniteDuration],
  metricsCollector:  MetricsCollector
)(implicit scheduler: Scheduler) extends Actor {
  import WebsocketRaidsHandler.SerializedKeepAliveMessage

  private implicit val implicitTranslator: BossNameTranslator = translator

  // On connect, send current version
  override def preStart(): Unit = {
    this push WelcomeResponse(serverVersion = VersionString(BuildInfo.version))
    metricsCollector.webSocketConnected()
  }

  private var followed: Map[BossName, Cancelable] = Map.empty
  private val newBossCancelable = raidFinder.newBossObservable.foreach { boss =>
    val bosses = Seq(boss.toProtocol)
    this push RaidBossesResponse(raidBosses = bosses)
  }

  private val newTranslationCancelable = translator.observable.foreach { translation =>
    raidFinder.getKnownBosses.get(translation.from).foreach { boss =>
      // If a boss we're following gets a new translation, follow the translated boss too
      if (followed.isDefinedAt(translation.from)) {
        follow(Seq(translation.to))
      }

      val bosses = Seq(boss.toProtocol(Option(translation.to)))
      this push RaidBossesResponse(raidBosses = bosses)
    }
  }

  def receive: Receive = {
    case r: RequestMessage => r.toRequest.foreach(handleRequest)
  }

  private def push(response: Response): Unit =
    out ! BinaryProtobuf(response.toMessage.toByteArray)

  private val keepAliveCancelable = keepAliveInterval.map { interval =>
    context.system.scheduler.schedule(interval, interval) {
      out ! SerializedKeepAliveMessage
    }
  }

  private val handleRequest: PartialFunction[Request, _] = {
    case r: AllRaidBossesRequest =>
      val bosses = raidFinder.getKnownBosses.values.map(_.toProtocol)
      this push RaidBossesResponse(raidBosses = bosses.toSeq)

    case req: RaidBossesRequest =>
      val bosses = req.bossNames
        .collect(raidFinder.getKnownBosses)
        .map(_.toProtocol)
      this push RaidBossesResponse(raidBosses = bosses.toSeq)

    case r: FollowRequest =>
      // Follow bosses and their translated counterparts
      follow(r.bossNames ++ r.bossNames.flatMap(translator.translate))

      this push FollowStatusResponse(followed.keys.toSeq)

    case r: UnfollowRequest =>
      // Unfollow bosses and their translated counterparts
      unfollow(r.bossNames ++ r.bossNames.flatMap(translator.translate))

      this push FollowStatusResponse(followed.keys.toSeq)
  }

  private def follow(bossNames: Seq[BossName]): Unit = {
    // Filter out bosses we're already following
    val newBosses = bossNames.filterNot(followed.keys.toSet)

    val cancelables = newBosses.map { bossName =>
      val cancelable = raidFinder
        .getRaidTweets(bossName)
        .foreach(out ! _)

      // Intern the bossName string to reduce memory usage, since boss
      // names are repeated often between different users.
      bossName.intern -> cancelable
    }

    followed = followed ++ cancelables
  }

  private def unfollow(bossNames: Seq[BossName]): Unit = {
    bossNames.flatMap(followed.get).foreach(_.cancel())
    followed = followed -- bossNames
  }

  override def postStop(): Unit = {
    metricsCollector.webSocketDisconnected()
    newTranslationCancelable.cancel()
    newBossCancelable.cancel()
    keepAliveCancelable.foreach(_.cancel())
    followed.values.foreach(_.cancel())
  }
}

object WebsocketRaidsHandler {
  // Since this never changes, there's no need to create and serialize
  // a new instance of it every time.
  private val SerializedKeepAliveMessage: BinaryProtobuf =
    BinaryProtobuf(KeepAliveResponse().toMessage.toByteArray)

  def props(
    out:               ActorRef,
    raidFinder:        RaidFinder[BinaryProtobuf],
    translator:        BossNameTranslator,
    keepAliveInterval: Option[FiniteDuration],
    metricsCollector:  MetricsCollector
  )(implicit scheduler: Scheduler): Props = Props {
    new WebsocketRaidsHandler(out, raidFinder, translator, keepAliveInterval, metricsCollector)
  }.withDeploy(Deploy.local)
}

