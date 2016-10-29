package walfie.gbf.raidfinder

import java.util.Date

package object domain {
  type BossName = String
  type TweetId = Long
  type RaidImage = String
}

package domain {

  sealed trait Language
  object Language {
    case object English extends Language
    case object Japanese extends Language
  }

  case class RaidInfo(
    tweet: RaidTweet,
    boss:  RaidBoss
  )

  case class RaidTweet(
    bossName:     BossName,
    raidId:       String,
    screenName:   String,
    tweetId:      TweetId,
    profileImage: String,
    text:         String,
    createdAt:    Date,
    language:     Language
  )

  case class RaidBoss(
    name:     BossName,
    level:    Int,
    image:    Option[RaidImage],
    lastSeen: Date,
    language: Language
  )

  trait FromRaidTweet[T] {
    def from(raidTweet: RaidTweet): T
    def getBossName(t: T): BossName
  }

  object FromRaidTweet {
    def apply[T](
      fromF:        RaidTweet => T,
      getBossNameF: T => BossName
    ) = new FromRaidTweet[T] {
      def from(raidTweet: RaidTweet): T = fromF(raidTweet)
      def getBossName(t: T): BossName = getBossNameF(t)
    }

    val Identity: FromRaidTweet[RaidTweet] =
      FromRaidTweet[RaidTweet](identity, _.bossName)
  }
}

