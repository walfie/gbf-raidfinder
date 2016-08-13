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

  private val subscribersWaiting: Agent[Map[BossName, Set[Subscriber[RaidInfo]]]] =
    Agent(Map.empty)

  def onComplete(): Unit = ???

  def onError(e: Throwable): Unit = ???

  def onNext(elem: GroupedObservable[BossName, RaidInfo]): Future[Ack] = {
    val bossName = elem.key
    val newStream = elem.cache(cacheSizePerBoss)

    raidInfoObservables
      .alter(_.updated(bossName, newStream))
      .flatMap(_ => Ack.Continue)
  }

  def subscribe(subscriber: Subscriber[RaidInfo], bossName: BossName): Cancelable = {
    raidInfoObservables.get.get(bossName) match {
      case None =>
        updateSubscribersWaiting(bossName)(_ + subscriber)
        Cancelable(() => updateSubscribersWaiting(bossName)(_ - subscriber))
      case Some(raidInfoObservable) =>
        raidInfoObservable.subscribe(subscriber)
    }
  }

  private def updateSubscribersWaiting(bossName: BossName)(
    f: Set[Subscriber[RaidInfo]] => Set[Subscriber[RaidInfo]]
  ): Future[Map[BossName, Set[Subscriber[RaidInfo]]]] = {
    subscribersWaiting.alter { waiting =>
      val currentlyWaiting = waiting.getOrElse(bossName, Set.empty)
      waiting.updated(bossName, f(currentlyWaiting))
    }
  }
}

