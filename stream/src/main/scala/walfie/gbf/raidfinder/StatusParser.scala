package walfie.gbf.raidfinder

import twitter4j._
import walfie.gbf.raidfinder.domain._
import scala.util.Try

object StatusParser {
  /** Regexes to match raid request tweets */
  val RaidRegexJapanese = "(.*)参加者募集！参戦ID：([0-9A-F]+)\n(.+)\n?.*".r
  val RaidRegexEnglish = "(.*)I need backup!Battle ID: ([0-9A-F]+)\n(.+)\n?.*".r

  /**
    * Regex to get boss level from full name
    * e.g., "Lv100 オオゾラッコ" or "Lvl 100 Ozorotter"
    */
  val BossRegex = "Lv(?:l )?([0-9]+) (.*)".r

  /** The source value for the official Granblue Twitter app */
  val GranblueSource =
    """<a href="http://granbluefantasy.jp/" rel="nofollow">グランブルー ファンタジー</a>"""

  def parse(status: Status): Option[RaidInfo] = status.getText match {
    case _ if status.getSource != GranblueSource => None

    case RaidRegexJapanese(extraText, raidId, boss) =>
      Some(TweetParts(status, extraText, raidId, boss).toRaidInfo)

    case RaidRegexEnglish(extraText, raidId, boss) =>
      Some(TweetParts(status, extraText, raidId, boss).toRaidInfo)

    case _ => None
  }

  private case class TweetParts(status: Status, extraText: String, raidId: String, boss: String) {
    def toRaidInfo(): RaidInfo = {
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

      RaidInfo(raidTweet, raidBoss)
    }
  }

  private def getImageFromStatus(status: Status): Option[RaidImage] = {
    status.getMediaEntities.headOption.map(_.getMediaURLHttps)
  }
}

