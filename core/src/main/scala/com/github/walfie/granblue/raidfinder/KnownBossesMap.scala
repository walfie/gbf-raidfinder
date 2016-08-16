package com.github.walfie.granblue.raidfinder

import akka.agent.Agent
import com.github.walfie.granblue.raidfinder.domain._
import monix.execution.{Ack, Cancelable, Scheduler}
import monix.reactive._
import scala.concurrent.{ExecutionContext, Future}

trait KnownBossesMap {
  def get(): Map[BossName, RaidBoss]
}

object KnownBossesObserver {
  def fromRaidInfoObservable(
    observable: Observable[RaidInfo]
  )(implicit scheduler: Scheduler): (KnownBossesObserver, Cancelable) = {
    val observer = new KnownBossesObserver
    val cancelable = observable.subscribe(observer)
    (observer, cancelable)
  }
}

/**
  * Takes incoming [[RaidInfo]]s and keeps the latest of each raid boss.
  * This can be implemented trivially with [[Observable#scan]] but eh.
  */
class KnownBossesObserver(implicit ec: ExecutionContext) extends Observer[RaidInfo] with KnownBossesMap {
  val agent = Agent[Map[BossName, RaidBoss]](Map.empty)

  def onComplete(): Unit = ()
  def onError(e: Throwable): Unit = ()
  def onNext(elem: RaidInfo): Future[Ack] = {
    val name = elem.tweet.bossName
    val raidBoss = RaidBoss.fromRaidInfo(elem)
    agent.alter(_.updated(name, raidBoss)).flatMap(_ => Ack.Continue)
  }

  def get(): Map[BossName, RaidBoss] = agent.get()
}

