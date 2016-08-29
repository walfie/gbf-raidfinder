package walfie.gbf.raidfinder

import twitter4j._
import walfie.gbf.raidfinder.domain._
import scala.util.Try

object StatusParser {
  /** Regex to match Japanese raid request tweets */
  val RaidRegex = "(.*)参加者募集！参戦ID：([0-9A-F]+)\n(.+)\n?.*".r

  /** Regex to get boss level from full name */
  val BossRegex = "Lvl?([0-9]+) (.*)".r

  /** The source value for the official Granblue Twitter app */
  val GranblueSource =
    """<a href="http://granbluefantasy.jp/" rel="nofollow">グランブルー ファンタジー</a>"""

  def parse(status: Status): Option[RaidInfo] = status.getText match {
    case _ if status.getSource != GranblueSource => None

    case RaidRegex(extraText, raidId, boss) =>
      val bossName = boss.trim

      val raidTweet = RaidTweet(
        tweetId = status.getId,
        screenName = status.getUser.getScreenName,
        bossName = bossName,
        raidId = raidId.trim,
        profileImage = status.getUser.getProfileImageURLHttps,
        text = extraText.trim,
        createdAt = status.getCreatedAt
      )

      val defaultLevel = 0
      val bossLevel = bossName match {
        case BossRegex(level, name) =>
          Try(level.toInt).toOption.getOrElse(defaultLevel)
        case _ => defaultLevel
      }

      val raidBoss = RaidBoss(
        name = bossName,
        level = bossLevel,
        image = getImageFromStatus(status),
        lastSeen = status.getCreatedAt
      )

      Some(RaidInfo(raidTweet, raidBoss))

    case _ => None
  }

  private def getImageFromStatus(status: Status): Option[RaidImage] = {
    status.getMediaEntities.headOption.map(_.getMediaURLHttps)
  }
}

