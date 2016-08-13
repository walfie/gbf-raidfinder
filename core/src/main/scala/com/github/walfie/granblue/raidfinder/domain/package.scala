package com.github.walfie.granblue.raidfinder

import java.util.Date

package object domain {
  type BossName = String
  type TweetId = Long
}

package domain {
  case class RaidInfo(
    tweet: RaidTweet,
    image: Option[RaidImage]
  )

  case class RaidTweet(
    bossName:     BossName,
    tweetId:      TweetId,
    screenName:   String,
    profileImage: String,
    raidId:       String,
    text:         String,
    createdAt:    Date
  )

  case class RaidBoss(
    bossName: BossName,
    image:    Option[RaidImage],
    lastSeen: Date
  )

  case class RaidImage(url: String) extends AnyVal {
    def thumb(): String = url + ":thumb"
  }
}

