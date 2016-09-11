package walfie.gbf.raidfinder

import java.util.Date
import monix.execution.schedulers.TestScheduler
import monix.reactive.Observer
import monix.reactive.subjects._
import org.mockito.Mockito._
import org.scalatest._
import org.scalatest.concurrent.Eventually
import org.scalatest.Matchers._
import org.scalatest.mockito.MockitoSugar
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Random
import walfie.gbf.raidfinder.domain._

class KnownBossesObserverSpec extends KnownBossesObserverSpecHelpers {
  "Start with initial value" in new ObserverFixture {
    val boss1 = mockRaidInfo("A").boss
    val boss2 = mockRaidInfo("B").boss
    override val initialBosses = Seq(boss1, boss2)

    observer.get shouldBe Map("A" -> boss1, "B" -> boss2)
    cancelable.cancel()
  }

  "Keep last known of each boss" in new ObserverFixture {
    val bosses1 = (1 to 5).map(_ => mockRaidInfo("A"))
    val bosses2 = (1 to 10).map(_ => mockRaidInfo("B"))

    bosses1.foreach(raidInfos.onNext)
    bosses2.foreach(raidInfos.onNext)

    eventually {
      scheduler.tick()
      observer.get shouldBe Map(
        "A" -> bosses1.last.boss,
        "B" -> bosses2.last.boss
      )
    }
    cancelable.cancel()
  }
}

trait KnownBossesObserverSpecHelpers extends FreeSpec with MockitoSugar with Eventually {
  trait ObserverFixture {
    implicit val scheduler = TestScheduler()
    val initialBosses: Seq[RaidBoss] = Seq.empty
    val raidInfos = ConcurrentSubject.replay[RaidInfo]
    lazy val (observer, cancelable) = KnownBossesObserver
      .fromRaidInfoObservable(raidInfos, initialBosses)
  }

  def mockRaidInfo(bossName: String): RaidInfo = {
    val tweet = mock[RaidTweet]
    when(tweet.bossName) thenReturn bossName
    when(tweet.createdAt) thenReturn (new Date(Random.nextLong.abs * 1000))
    val boss = mock[RaidBoss]
    when(boss.name) thenReturn bossName
    RaidInfo(tweet, boss)
  }
}

