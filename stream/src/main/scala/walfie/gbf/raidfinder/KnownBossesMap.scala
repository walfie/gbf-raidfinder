package walfie.gbf.raidfinder

import akka.agent.Agent
import java.util.Date
import monix.execution.{Ack, Cancelable, Scheduler}
import monix.reactive._
import monix.reactive.subjects.ConcurrentSubject
import scala.concurrent.{ExecutionContext, Future}
import walfie.gbf.raidfinder.domain._

trait KnownBossesMap {
  def get(): Map[BossName, RaidBoss]
  def newBossObservable(): Observable[RaidBoss]

  /**
    * @param minDate Remove bosses that haven't been seen since this date
    * @param levelThreshold Bosses higher than this level (inclusive) will not be removed
    */
  def purgeOldBosses(
    minDate:        Date,
    levelThreshold: Option[Int]
  ): Future[Map[BossName, RaidBoss]]
}

object KnownBossesObserver {
  def fromRaidInfoObservable(
    observable:    Observable[RaidInfo],
    initialBosses: Seq[RaidBoss]
  )(implicit scheduler: Scheduler): (KnownBossesObserver, Cancelable) = {
    val observer = new KnownBossesObserver(initialBosses)
    val cancelable = observable.subscribe(observer)
    (observer, cancelable)
  }
}

/**
  * Takes incoming `RaidInfo`s and keeps the latest of each raid boss.
  * This can be implemented trivially with `Observable#scan` but eh.
  */
class KnownBossesObserver(
  initialBosses: Seq[RaidBoss]
)(implicit scheduler: Scheduler) extends Observer[RaidInfo] with KnownBossesMap {
  private val agent = Agent[Map[BossName, RaidBoss]](
    initialBosses.map(boss => boss.name -> boss)(scala.collection.breakOut)
  )

  // TODO: Write test for this
  private val subject = ConcurrentSubject.publish[RaidBoss]
  val newBossObservable: Observable[RaidBoss] = subject

  def onComplete(): Unit = ()
  def onError(e: Throwable): Unit = ()
  def onNext(elem: RaidInfo): Future[Ack] = {
    val name = elem.tweet.bossName
    val raidBoss = elem.boss
    if (!agent.get.isDefinedAt(name)) {
      subject.onNext(raidBoss)
    }
    agent.alter(_.updated(name, raidBoss)).flatMap(_ => Ack.Continue)
  }

  def get(): Map[BossName, RaidBoss] = agent.get()
  def purgeOldBosses(
    minDate:        Date,
    levelThreshold: Option[Int]
  ): Future[Map[BossName, RaidBoss]] = {
    agent.alter(_.filter {
      case (name, boss) => boss.lastSeen.after(minDate) || levelThreshold.exists(boss.level >= _)
    })
  }
}

