package walfie.gbf.raidfinder

import monix.execution.{Cancelable, Scheduler}
import monix.reactive.Observable
import walfie.gbf.raidfinder.domain._
import walfie.gbf.raidfinder.util.{ObservablesPartitioner, CachedObservablesPartitioner}

object CachedRaidTweetsPartitioner {
  def fromUngroupedObservable(
    observable:       Observable[RaidTweet],
    cacheSizePerBoss: Int
  )(implicit scheduler: Scheduler): (CachedRaidTweetsPartitioner, Cancelable) = {
    CachedObservablesPartitioner.fromUngroupedObservable(
      observable,
      cacheSizePerBoss
    )(_.bossName)
  }
}

