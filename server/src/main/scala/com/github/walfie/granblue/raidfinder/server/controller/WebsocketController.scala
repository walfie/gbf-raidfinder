package com.github.walfie.granblue.raidfinder.server.controller

import akka.actor._
import akka.stream.Materializer
import com.github.walfie.granblue.raidfinder.domain._
import com.github.walfie.granblue.raidfinder.RaidFinder
import com.github.walfie.granblue.raidfinder.server.json._ // TODO: Remove
import com.github.walfie.granblue.raidfinder.server.protocol._ // TODO: remove
import com.github.walfie.granblue.raidfinder.protocol._
import monix.execution.Cancelable
import monix.execution.Scheduler
import play.api.libs.streams._
import play.api.mvc._
import play.api.mvc.WebSocket.MessageFlowTransformer
import com.github.walfie.granblue.raidfinder.server.util.MessageFlowTransformerUtil
import com.github.walfie.granblue.raidfinder.protocol.{Request, Response}

class WebsocketController(
  raidFinder: RaidFinder
)(implicit system: ActorSystem, materializer: Materializer) extends Controller {
  implicit val messageFlowTransformer =
    MessageFlowTransformerUtil.protobufJsonMessageFlowTransformer

  def raids = WebSocket.accept[RequestMessage, ResponseMessage] { request =>
    ActorFlow.actorRef(out => WebsocketRaidsHandler.props(out, raidFinder))
  }
}

class WebsocketRaidsHandler(
  out:        ActorRef,
  raidFinder: RaidFinder
) extends Actor {
  implicit val scheduler = Scheduler(context.system.dispatcher)

  var subscribed: Map[BossName, Cancelable] = Map.empty

  def receive: Receive = {
    case RequestMessage(data) => handleRequest(data)
  }

  import RequestMessage.Data._
  import SubscriptionChangeRequest.SubscriptionAction._
  val handleRequest: PartialFunction[RequestMessage.Data, _] = {
    case Empty =>
      out ! ErrorResponse() // TODO: Specific error

    case msg: RaidBossesMessage =>
      val bosses = raidFinder.getKnownBosses.values.map { rb: RaidBoss =>
        RaidBossesResponse.RaidBoss(
          bossName = rb.bossName,
          image = rb.image,
          lastSeen = rb.lastSeen
        )
      }
      out ! RaidBossesResponse(raidBosses = bosses.toSeq)

    case SubscriptionChangeMessage(r) if r.action == SUBSCRIBE =>
      val cancelables = r.bossNames
        .filterNot(subscribed.keys.toSeq.contains)
        .map { bossName =>
          bossName -> raidFinder.getRaidTweets(bossName).foreach { r: RaidTweet =>
            // TODO: Add utils for converting domain objects to protobuf messages
            out ! RaidTweetResponse(
              bossName = r.bossName,
              raidId = r.raidId,
              screenName = r.screenName,
              tweetId = r.tweetId,
              profileImage = r.profileImage,
              text = r.text,
              createdAt = r.createdAt
            )
          }
        }
      subscribed = subscribed ++ cancelables

    case SubscriptionChangeMessage(r) if r.action == UNSUBSCRIBE =>
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

