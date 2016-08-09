package com.github.walfie.granblue.raidfinder.flow

import com.github.walfie.granblue.raidfinder.domain._
import twitter4j._

object StatusParser {
  /** Regex to match Japanese raid request tweets */
  val RaidRegex = "(.*)参加者募集！参戦ID：([0-9A-F]+)\n(.+)\n?.*".r

  /** The source value for the official Granblue Twitter app */
  val GranblueSource =
    """<a href="http://granbluefantasy.jp/" rel="nofollow">グランブルー ファンタジー</a>"""

  def parseStatus(status: Status): Option[ParsedStatus] = status.getText match {
    case _ if status.getSource != GranblueSource => None

    case RaidRegex(extraText, raidId, boss) =>
      val bossName = boss.trim

      val raidTweet = RaidTweet(
        tweetId = status.getId,
        screenName = status.getUser.getScreenName,
        bossName = bossName,
        raidId = raidId.trim,
        text = extraText.trim,
        createdAt = status.getCreatedAt
      )

      val raidBoss = RaidBoss(
        bossName = bossName,
        image = getImageFromStatus(status),
        lastSeen = status.getCreatedAt
      )

      Some(ParsedStatus(raidTweet, raidBoss))

    case _ => None
  }

  def getImageFromStatus(status: Status): Option[String] = {
    status.getMediaEntities.headOption.map(_.getMediaURLHttps)
  }
}

