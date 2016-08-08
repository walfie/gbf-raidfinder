package com.github.walfie.granblue.raidtracker.actor

import akka.actor._
import akka.cluster.pubsub.DistributedPubSubMediator.{Subscribe, Unsubscribe}
import com.github.walfie.granblue.raidtracker._

// For temporary debugging
class WebsocketHandler(
  backend: ActorRef, pubSubMediator: ActorRef
) extends Actor {
  import RaidPoller._
  import WebsocketHandler.WebsocketConnected
  import WebsocketHandler.Protocol._

  def receive: Receive = {
    case WebsocketConnected(out) =>
      context.become(connected(out))
  }

  def connected(out: ActorRef): Receive = {
    val fromClient: Receive = {
      case RaidBossesRequest =>
        backend ! GetRaidBosses
      case SubscribeRequest(bossName) =>
        pubSubMediator ! Subscribe(bossName, self)
        backend ! GetCachedRaids(bossName)
      case UnsubscribeRequest(bossName) =>
        pubSubMediator ! Unsubscribe(bossName, self)
    }

    val fromBackend: Receive = {
      case RaidBossesMessage(raidBosses) =>
        out ! RaidBossesResponse(raidBosses)
      case RaidsMessage(bossName, raids) =>
        out ! RaidsResponse(bossName, raids)
    }

    fromClient.orElse(fromBackend)
  }
}

object WebsocketHandler {
  case class WebsocketConnected(out: ActorRef)

  trait Response
  trait Request

  object Protocol {
    case object RaidBossesRequest extends Request
    case class SubscribeRequest(bossName: String) extends Request
    case class UnsubscribeRequest(bossName: String) extends Request
    case class RaidBossesResponse(raidBosses: Seq[RaidBoss]) extends Response
    case class RaidsResponse(bossName: String, raids: Seq[Raid]) extends Response
  }
}

