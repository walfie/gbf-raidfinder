package com.github.walfie.granblue.raidfinder

import java.util.Date

package object domain {
  type BossName = String
  type TweetId = Long
  type RaidImage = String
}

package domain {
  case class RaidInfo(
    tweet: RaidTweet,
    image: Option[RaidImage]
  )

  case class RaidTweet(
    bossName:     BossName,
    raidId:       String,
    screenName:   String,
    tweetId:      TweetId,
    profileImage: String,
    text:         String,
    createdAt:    Date
  )

  case class RaidBoss(
    bossName: BossName,
    image:    Option[RaidImage],
    lastSeen: Date
  )

  object RaidBoss {
    def fromRaidInfo(raidInfo: RaidInfo): RaidBoss = RaidBoss(
      bossName = raidInfo.tweet.bossName,
      image = raidInfo.image,
      lastSeen = raidInfo.tweet.createdAt
    )
  }
}

