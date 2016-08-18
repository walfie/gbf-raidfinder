package com.github.walfie.granblue.raidfinder.server.controller

import akka.actor._
import akka.stream.Materializer
import com.github.walfie.granblue.raidfinder.domain._
import com.github.walfie.granblue.raidfinder.RaidFinder
import com.github.walfie.granblue.raidfinder.server.json._
import com.github.walfie.granblue.raidfinder.server.protocol._
import monix.execution.Cancelable
import monix.execution.Scheduler
import play.api.libs.streams._
import play.api.mvc._
import play.api.mvc.WebSocket.MessageFlowTransformer

class WebsocketController(
  raidFinder: RaidFinder
)(implicit system: ActorSystem, materializer: Materializer) extends Controller {
  implicit val messageFlowTransformer = MessageFlowTransformer
    .jsonMessageFlowTransformer[WebsocketRequest[_], WebsocketResponse]

  def raids = WebSocket.accept[WebsocketRequest[_], WebsocketResponse] { request =>
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

  override def postStop(): Unit = {
    subscribed.values.foreach(_.cancel)
  }
}

object WebsocketRaidsHandler {
  def props(out: ActorRef, raidFinder: RaidFinder): Props = Props {
    new WebsocketRaidsHandler(out, raidFinder)
  }.withDeploy(Deploy.local)
}

