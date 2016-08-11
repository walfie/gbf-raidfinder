package com.github.walfie.granblue.raidfinder.flow

import akka.actor._
import akka.cluster.pubsub.DistributedPubSub
import akka.cluster.pubsub.DistributedPubSubMediator.Publish
import akka.Done
import akka.stream.scaladsl._
import com.github.walfie.granblue.raidfinder.domain._
import scala.concurrent.Future

object RaidTweetsPublisher {
  def publisher(pubSubMediator: ActorRef): Sink[RaidTweet, Future[Done]] =
    Sink.foreach { raidTweet: RaidTweet =>
      pubSubMediator ! Publish(raidTweet.bossName, raidTweet)
    }

  def fromSystem(system: ActorSystem): Sink[RaidTweet, Future[Done]] = {
    publisher(DistributedPubSub(system).mediator)
  }
}

