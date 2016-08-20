package com.github.walfie.granblue.raidfinder.server.actor

import akka.actor._
import com.github.walfie.granblue.raidfinder.domain._
import com.github.walfie.granblue.raidfinder.RaidFinder
import com.github.walfie.granblue.raidfinder.protocol._
import com.github.walfie.granblue.raidfinder.protocol.implicits._
import monix.execution.{Cancelable, Scheduler}
import com.github.walfie.granblue.raidfinder.protocol.SubscriptionChangeRequest.SubscriptionAction.{SUBSCRIBE, UNSUBSCRIBE}

class WebsocketRaidsHandler(out: ActorRef, raidFinder: RaidFinder) extends Actor {
  implicit val scheduler = Scheduler(context.system.dispatcher)

  var subscribed: Map[BossName, Cancelable] = Map.empty

  def receive: Receive = {
    case r: RequestMessage => r.toRequest.foreach(handleRequest)
  }

  def push(response: Response): Unit = out ! response.toMessage

  val handleRequest: PartialFunction[Request, _] = {
    case r: RaidBossesRequest =>
      val bosses = raidFinder.getKnownBosses.values.map { rb: RaidBoss =>
        RaidBossesResponse.RaidBoss(
          bossName = rb.bossName,
          image = rb.image,
          lastSeen = rb.lastSeen
        )
      }

      this push RaidBossesResponse(raidBosses = bosses.toSeq)

    case r: SubscriptionChangeRequest if r.action == SUBSCRIBE =>
      val cancelables = r.bossNames
        .filterNot(subscribed.keys.toSeq.contains)
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

      subscribed = subscribed ++ cancelables

    case r: SubscriptionChangeRequest if r.action == UNSUBSCRIBE =>
      r.bossNames.map { bossName =>
        subscribed.get(bossName).foreach(_.cancel())
      }
      subscribed = subscribed -- r.bossNames
  }

  override def postStop(): Unit = {
    subscribed.values.foreach(_.cancel)
  }
}

object WebsocketRaidsHandler {
  def props(out: ActorRef, raidFinder: RaidFinder): Props = Props {
    new WebsocketRaidsHandler(out, raidFinder)
  }.withDeploy(Deploy.local)
}

