package com.github.walfie.granblue.raidfinder.domain

import java.util.Date

case class ParsedStatus(
  raidTweet: RaidTweet,
  raidBoss:  RaidBoss
)

case class RaidTweet(
  tweetId:    Long,
  screenName: String,
  bossName:   String,
  raidId:     String,
  text:       String,
  createdAt:  Date
)

case class RaidBoss(
  bossName: String,
  image:    Option[String],
  lastSeen: Date
)

