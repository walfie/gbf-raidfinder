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
  def url(): String = s"https://twitter.com/$screenName/status/$id"
}

case class TweetSearchResult(tweets: Seq[Tweet], maxId: Option[Long])

case class Raid(
  bossName: String,
  id: String,
  text: String
)

