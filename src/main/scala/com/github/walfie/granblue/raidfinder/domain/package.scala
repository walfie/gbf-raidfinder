package com.github.walfie.granblue.raidfinder.domain

import java.util.Date

case class RaidInfo(
  tweet: RaidTweet,
  image: Option[RaidImage]
)

case class RaidTweet(
  tweetId:    Long,
  screenName: String,
  bossName:   String,
  raidId:     String,
  text:       String,
  createdAt:  Date
)

case class RaidImage(url: String) extends AnyVal {
  def thumb(): String = url + ":thumb"
}

