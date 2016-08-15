package com.github.walfie.granblue.raidfinder

import com.github.walfie.granblue.raidfinder.util.{ObservablesPartitioner, CachedObservablesPartitioner}
import com.github.walfie.granblue.raidfinder.domain._
import monix.execution.{Cancelable, Scheduler}
import monix.reactive.Observable

object CachedRaidTweetsPartitioner {
  def fromUngroupedObservable(
    observable:       Observable[RaidTweet],
    cacheSizePerBoss: Int
  )(implicit scheduler: Scheduler): (CachedRaidTweetsPartitioner, Cancelable) = {
    CachedObservablesPartitioner.fromUngroupedObservable(observable, cacheSizePerBoss)(_.bossName)
  }
}

