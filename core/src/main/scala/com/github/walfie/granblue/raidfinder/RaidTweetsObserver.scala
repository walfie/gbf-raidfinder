package com.github.walfie.granblue.raidfinder

import akka.agent.Agent
import com.github.walfie.granblue.raidfinder.domain._
import monix.execution.{Ack, Cancelable}
import monix.reactive._
import monix.reactive.observables.GroupedObservable
import monix.reactive.observers.Subscriber
import scala.concurrent.{ExecutionContext, Future}

class RaidInfoObserver(
  cacheSizePerBoss: Int
)(implicit ec: ExecutionContext) extends Observer[GroupedObservable[BossName, RaidInfo]] {
  private val raidInfoObservables: Agent[Map[BossName, Observable[RaidInfo]]] =
    Agent(Map.empty)

  /** Subscribers waiting for a boss that we don't yet know about */
  private val subscribersOnHold: Agent[Map[BossName, Set[Subscriber[RaidInfo]]]] =
    Agent(Map.empty)

  def onComplete(): Unit = ???

  def onError(e: Throwable): Unit = ???

  /**
    * When a new raid boss comes in, add it to the Map of known raid bosses,
    * and start publishing to any subscriber who has previously expressed interest
    */
  def onNext(elem: GroupedObservable[BossName, RaidInfo]): Future[Ack] = {
    val bossName = elem.key
    val newStream = elem.cache(cacheSizePerBoss)

    for {
      _ <- raidInfoObservables.alter { observables =>
        observables.updated(bossName, newStream)
      }
      _ <- updateSubscribersOnHold(bossName) { subscribers =>
        subscribers.foreach(newStream.subscribe)
        Set.empty
      }
      ack <- Ack.Continue
    } yield ack
  }

  /**
    * Attempt to subscribe to a certain raid boss. If it's not a raid boss we know
    * about, put the subscriber on hold until we get info about that boss.
    */
  def subscribe(bossName: BossName, subscriber: Subscriber[RaidInfo]): Cancelable = {
    raidInfoObservables.get.get(bossName) match {
      case None =>
        updateSubscribersOnHold(bossName)(_ + subscriber)
        // TODO: This cancelable becomes invalid when the boss comes in later
        Cancelable(() => updateSubscribersOnHold(bossName)(_ - subscriber))
      case Some(raidInfoObservable) =>
        raidInfoObservable.subscribe(subscriber)
    }
  }

  private def updateSubscribersOnHold(bossName: BossName)(
    f: Set[Subscriber[RaidInfo]] => Set[Subscriber[RaidInfo]]
  ): Future[Map[BossName, Set[Subscriber[RaidInfo]]]] = {
    subscribersOnHold.alter { waiting =>
      val currentlyOnHold = waiting.getOrElse(bossName, Set.empty)
      val updatedOnHold = f(currentlyOnHold)

      if (updatedOnHold.isEmpty) {
        waiting - bossName
      } else waiting.updated(bossName, updatedOnHold)
    }
  }
}

