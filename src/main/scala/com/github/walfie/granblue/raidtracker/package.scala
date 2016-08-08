package com.github.walfie.granblue.raidtracker

/** Twitter status with only the stuff we care about */
case class Tweet(
  id:         Long,
  screenName: String,
  text:       String,
  images:     Seq[TwitterImage],
  createdAt:  java.util.Date
) {
  def url(): String = s"https://twitter.com/$screenName/status/$id"
}

case class TwitterImage(url: String) extends AnyVal {
  def thumb(): String = url + ":thumb"
}

case class TweetSearchResult(tweets: Seq[Tweet], maxId: Option[Long])

case class Raid(
  bossName: String,
  id:       String,
  text:     String
)

case class RaidBoss(
  name:     String,
  image:    Option[String],
  lastSeen: java.util.Date
)

