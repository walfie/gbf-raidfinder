package com.github.walfie.granblue

import com.github.walfie.granblue.raidfinder.domain._
import com.github.walfie.granblue.raidfinder.util.{ObservablesPartitioner, CachedObservablesPartitioner}

package object raidfinder {
  type RaidTweetsPartitioner = ObservablesPartitioner[BossName, RaidTweet]
  type CachedRaidTweetsPartitioner = CachedObservablesPartitioner[BossName, RaidTweet]
}

