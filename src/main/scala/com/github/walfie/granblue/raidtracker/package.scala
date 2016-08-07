package com.github.walfie.granblue.raidtracker

/** Twitter status with only the stuff we care about */
case class Tweet(
  id: Long,
  screenName: String,
  text: String,
  imageUrls: Seq[String],
  createdAt: java.util.Date
) {
  def smallImageUrls(): Seq[String] = imageUrls.map(_ + ":small")
}

case class TweetSearchResult(tweets: Seq[Tweet], maxId: Option[Long])

case class Raid(
  bossName: String,
  id: String,
  text: String
)

