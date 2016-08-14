package com.github.walfie.granblue.raidfinder

import com.github.walfie.granblue.raidfinder.util.{ObservablesPartitioner, CachedObservablesPartitioner}
import com.github.walfie.granblue.raidfinder.domain._
import monix.execution.Scheduler
import monix.reactive.Observable

object CachedRaidTweetsPartitioner {
  def fromObservable(
    observable:       Observable[RaidTweet],
    cacheSizePerBoss: Int
  )(implicit scheduler: Scheduler): CachedRaidTweetsPartitioner = {
    CachedObservablesPartitioner.fromObservable(observable, cacheSizePerBoss)(_.bossName)
  }
}

