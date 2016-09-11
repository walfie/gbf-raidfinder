package walfie.gbf.raidfinder.server.actor

import akka.actor._
import monix.execution.{Cancelable, Scheduler}
import scala.concurrent.duration.FiniteDuration
import walfie.gbf.raidfinder.domain._
import walfie.gbf.raidfinder.protocol
import walfie.gbf.raidfinder.protocol.syntax._
import walfie.gbf.raidfinder.protocol.{RaidBoss => _, _}
import walfie.gbf.raidfinder.RaidFinder
import walfie.gbf.raidfinder.server.syntax.ProtocolConverters.RaidBossDomainOps

class WebsocketRaidsHandler(
  out:               ActorRef,
  raidFinder:        RaidFinder,
  keepAliveInterval: Option[FiniteDuration]
) extends Actor {
  implicit val scheduler = Scheduler(context.system.dispatcher)

  var followed: Map[BossName, Cancelable] = Map.empty
  val newBossListener: Cancelable = raidFinder.newBossObservable.foreach { boss =>
    val bosses = Seq(boss.toProtocol)
    this push RaidBossesResponse(raidBosses = bosses)
  }

  def receive: Receive = {
    case r: RequestMessage => r.toRequest.foreach(handleRequest)
  }

  def push(response: Response): Unit = out ! response.toMessage

  val keepAliveCancelable = keepAliveInterval.map { interval =>
    context.system.scheduler.schedule(interval, interval) {
      this push KeepAliveResponse()
    }
  }

  val handleRequest: PartialFunction[Request, _] = {
    case r: AllRaidBossesRequest =>
      val bosses = raidFinder.getKnownBosses.values.map(_.toProtocol)
      this push RaidBossesResponse(raidBosses = bosses.toSeq)

    case req: RaidBossesRequest =>
      val bosses = req.bossNames
        .collect(raidFinder.getKnownBosses)
        .map(_.toProtocol)
      this push RaidBossesResponse(raidBosses = bosses.toSeq)

    case r: FollowRequest =>
      val cancelables = r.bossNames
        .filterNot(followed.keys.toSeq.contains)
        .map { bossName =>
          bossName -> raidFinder.getRaidTweets(bossName).foreach { r: RaidTweet =>
            // TODO: Add utils for converting domain objects to protobuf messages
            val resp = RaidTweetResponse(
              bossName = r.bossName,
              raidId = r.raidId,
              screenName = r.screenName,
              tweetId = r.tweetId,
              profileImage = r.profileImage,
              text = r.text,
              createdAt = r.createdAt
            )

            this push resp
          }
        }

      followed = followed ++ cancelables
      this push FollowStatusResponse(followed.keys.toSeq)

    case r: UnfollowRequest =>
      r.bossNames.map { bossName =>
        followed.get(bossName).foreach(_.cancel())
      }
      followed = followed -- r.bossNames
      this push FollowStatusResponse(followed.keys.toSeq)
  }

  override def postStop(): Unit = {
    newBossListener.cancel()
    keepAliveCancelable.foreach(_.cancel())
    followed.values.foreach(_.cancel())
  }
}

object WebsocketRaidsHandler {
  def props(
    out:               ActorRef,
    raidFinder:        RaidFinder,
    keepAliveInterval: Option[FiniteDuration]
  ): Props = Props {
    new WebsocketRaidsHandler(out, raidFinder, keepAliveInterval)
  }.withDeploy(Deploy.local)
}

