package walfie.gbf

import walfie.gbf.raidfinder.domain._
import walfie.gbf.raidfinder.util.{ObservablesPartitioner, CachedObservablesPartitioner}

package object raidfinder {
  type RaidTweetsPartitioner = ObservablesPartitioner[BossName, RaidTweet]
  type CachedRaidTweetsPartitioner = CachedObservablesPartitioner[BossName, RaidTweet]
}

