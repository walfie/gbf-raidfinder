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
}

