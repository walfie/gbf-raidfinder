package com.github.walfie.granblue.raidfinder.server.controller

import akka.actor._
import akka.stream.Materializer
import com.github.walfie.granblue.raidfinder.domain._
import com.github.walfie.granblue.raidfinder.RaidFinder
import com.github.walfie.granblue.raidfinder.server.protocol._
import monix.execution.Cancelable
import play.api.libs.streams._
import monix.execution.Scheduler
import play.api.mvc._

class WebsocketController(
  raidFinder: RaidFinder
)(implicit system: ActorSystem, mat: Materializer) extends Controller {
  // TODO: Change to Request/Response types
  def raids = WebSocket.accept[String, String] { request =>
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
    case Subscribe(bossName) =>
      // Only subscribe if not already subscribed
      if (!subscribed.isDefinedAt(bossName)) {
        val cancelable = raidFinder.getRaidTweets(bossName).foreach(out ! _)
        subscribed = subscribed.updated(bossName, cancelable)
      }
      out ! Subscribed(subscribed.keys.toSet)

    case Unsubscribe(bossName) =>
      subscribed.get(bossName).foreach { cancelable =>
        cancelable.cancel()
        subscribed = subscribed - bossName
      }
      out ! Subscribed(subscribed.keys.toSet)

    case GetBosses =>
      out ! Bosses(raidFinder.getKnownBosses.values.toSeq)
  }
}

object WebsocketRaidsHandler {
  def props(out: ActorRef, raidFinder: RaidFinder): Props = Props {
    new WebsocketRaidsHandler(out, raidFinder)
  }.withDeploy(Deploy.local)
}

