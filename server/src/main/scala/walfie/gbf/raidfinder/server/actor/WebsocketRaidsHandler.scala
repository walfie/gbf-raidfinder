package walfie.gbf.raidfinder.server.actor

import akka.actor._
import monix.execution.{Cancelable, Scheduler}
import walfie.gbf.raidfinder.domain._
import walfie.gbf.raidfinder.protocol
import walfie.gbf.raidfinder.protocol.{RaidBoss => _, _}
import walfie.gbf.raidfinder.protocol.syntax._
import walfie.gbf.raidfinder.RaidFinder

class WebsocketRaidsHandler(out: ActorRef, raidFinder: RaidFinder) extends Actor {
  implicit val scheduler = Scheduler(context.system.dispatcher)

  var followed: Map[BossName, Cancelable] = Map.empty

  def receive: Receive = {
    case r: RequestMessage => r.toRequest.foreach(handleRequest)
  }

  def push(response: Response): Unit = out ! response.toMessage

  private def raidBossToProtocol(rb: RaidBoss): protocol.RaidBoss = protocol.RaidBoss(
    name = rb.name, level = rb.level, image = rb.image, lastSeen = rb.lastSeen
  )

  val handleRequest: PartialFunction[Request, _] = {
    case r: AllRaidBossesRequest =>
      val bosses = raidFinder.getKnownBosses.values.map(raidBossToProtocol)
      this push RaidBossesResponse(raidBosses = bosses.toSeq)

    case req: RaidBossesRequest =>
      val bosses = req.bossNames
        .collect(raidFinder.getKnownBosses)
        .map(raidBossToProtocol)
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
    followed.values.foreach(_.cancel)
  }
}

object WebsocketRaidsHandler {
  def props(out: ActorRef, raidFinder: RaidFinder): Props = Props {
    new WebsocketRaidsHandler(out, raidFinder)
  }.withDeploy(Deploy.local)
}

