package walfie.gbf.raidfinder

import java.util.Date
import org.mockito.Mockito._
import org.scalatest.Matchers._
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{Status => ScalaTestStatus, _}
import scala.collection.JavaConverters._
import twitter4j._
import walfie.gbf.raidfinder.domain._

class StatusParserSpec extends StatusParserSpecHelpers {
  "parse a valid status in Japanese" - {
    val expectedRaidTweet = RaidTweet(
      tweetId = 12345L,
      screenName = "walfieee",
      bossName = "Lv60 オオゾラッコ",
      raidId = "ABCD1234",
      profileImage = "http://example.com/profile-image_normal.png",
      text = "INSERT CUSTOM MESSAGE HERE",
      createdAt = now,
      language = Language.Japanese
    )

    val expectedRaidBoss = RaidBoss(
      name = "Lv60 オオゾラッコ",
      level = 60,
      image = Some("http://example.com/raid-image.png"),
      lastSeen = now,
      language = Language.Japanese
    )

    "with extra text" in {
      StatusParser.parse(mockStatus()) shouldBe Some {
        RaidInfo(expectedRaidTweet, expectedRaidBoss)
      }
    }

    "with newlines in extra text" in {
      val text = """
        |Hey
        |Newlines
        |Are
        |Cool
        |参加者募集！参戦ID：ABCD1234
        |Lv60 オオゾラッコ
        |http://example.com/image-that-is-ignored.png""".stripMargin.trim

      StatusParser.parse(mockStatus(text = text)) shouldBe Some {
        RaidInfo(expectedRaidTweet.copy(text = "Hey\nNewlines\nAre\nCool"), expectedRaidBoss)
      }
    }

    "without extra text" in {
      val text = """
        |参加者募集！参戦ID：ABCD1234
        |Lv60 オオゾラッコ
        |http://example.com/image-that-is-ignored.png""".stripMargin.trim

      StatusParser.parse(mockStatus(text = text)) shouldBe Some {
        RaidInfo(expectedRaidTweet.copy(text = ""), expectedRaidBoss)
      }
    }

  }

  "parse a valid status in English" - {
    val expectedRaidTweet = RaidTweet(
      tweetId = 12345L,
      screenName = "walfieee",
      bossName = "Lvl 60 Ozorotter",
      raidId = "ABCD1234",
      profileImage = "http://example.com/profile-image_normal.png",
      text = "INSERT CUSTOM MESSAGE HERE",
      createdAt = now,
      language = Language.English
    )

    val expectedRaidBoss = RaidBoss(
      name = "Lvl 60 Ozorotter",
      level = 60,
      image = Some("http://example.com/raid-image.png"),
      lastSeen = now,
      language = Language.English
    )

    "with extra text" in {
      val text = """
        |INSERT CUSTOM MESSAGE HERE I need backup!Battle ID: ABCD1234
        |Lvl 60 Ozorotter""".stripMargin.trim
      val status = mockStatus(text = text)

      StatusParser.parse(status) shouldBe Some {
        RaidInfo(expectedRaidTweet, expectedRaidBoss)
      }
    }

    "with newlines in extra text" in {
      val text = """
        |Hey
        |Newlines
        |Are
        |Cool
        |I need backup!Battle ID: ABCD1234
        |Lvl 60 Ozorotter
        |http://example.com/image-that-is-ignored.png""".stripMargin.trim
      val status = mockStatus(text = text)

      StatusParser.parse(status) shouldBe Some {
        RaidInfo(expectedRaidTweet.copy(text = "Hey\nNewlines\nAre\nCool"), expectedRaidBoss)
      }
    }

    "without extra text" in {
      val text = """
        |I need backup!Battle ID: ABCD1234
        |Lvl 60 Ozorotter
        |http://example.com/image-that-is-ignored.png""".stripMargin.trim
      val status = mockStatus(text = text)

      StatusParser.parse(status) shouldBe Some {
        RaidInfo(expectedRaidTweet.copy(text = ""), expectedRaidBoss)
      }
    }
  }

  "parse a status without an image URL at the end" in {
    // New bosses (e.g., watermelon boss) might not have images when they first come out
    val text = """
      |INSERT CUSTOM MESSAGE HERE 参加者募集！参戦ID：ABCD1234
      |Lv60 オオゾラッコ""".stripMargin.trim
    val status = mockStatus(text = text)

    StatusParser.parse(status) should not be empty
  }

  "parse a status with a newline at the end" in {
    val text = """
      |INSERT CUSTOM MESSAGE HERE 参加者募集！参戦ID：ABCD1234
      |Lv60 オオゾラッコ""".stripMargin.trim + "\n"
    val status = mockStatus(text = text)

    StatusParser.parse(status) should not be empty
  }

  "parse a status without media entities" in {
    val status = mockStatus(mediaEntities = Array.empty)

    val parsed = StatusParser.parse(status)
    parsed should not be empty
    parsed.foreach(_.boss.image shouldBe empty)
  }

  "return None if non-official client" in {
    StatusParser.parse(mockStatus(source = "TweetDeck")) shouldBe None
  }

  "return None invalid text" - {
    "haiku" in {
      val haiku = "#GranblueHaiku http://example.com/haiku.png"
      StatusParser.parse(mockStatus(text = haiku)) shouldBe None
    }

    "daily refresh" in {
      // Ignore tweets made via the daily Twitter refresh
      // https://github.com/walfie/gbf-raidfinder/issues/98
      val text = """
        |救援依頼 参加者募集！参戦ID：114514810
        |Lv100 ケルベロス スマホRPGは今これをやってるよ。今の推しキャラはこちら！　ゲーム内プロフィール→　https://t.co/5Xgohi9wlE https://t.co/Xlu7lqQ3km
        """.stripMargin.trim
      StatusParser.parse(mockStatus(text = text)) shouldBe None
    }

    "another daily refresh" in {
      // First two lines are user input
      val text = """
        |救援依頼 参加者募集！参戦ID：114514810
        |Lv100 ケルベロス
        |スマホRPGは今これをやってるよ。今の推しキャラはこちら！　ゲーム内プロフィール→　https://t.co/5Xgohi9wlE https://t.co/Xlu7lqQ3km
        """.stripMargin.trim
      StatusParser.parse(mockStatus(text = text)) shouldBe None
    }

    "image URL has extra text after" in {
      // First two lines are user input
      val text = """
        |救援依頼 参加者募集！参戦ID：114514810
        |Lv100 ケルベロス
        |https://t.co/5Xgohi9wlE https://t.co/Xlu7lqQ3km
        """.stripMargin.trim
      StatusParser.parse(mockStatus(text = text)) shouldBe None
    }
  }
}

trait StatusParserSpecHelpers extends FreeSpec with MockitoSugar {
  val now = new java.util.Date()

  val validTweetText = """
    |INSERT CUSTOM MESSAGE HERE 参加者募集！参戦ID：ABCD1234
    |Lv60 オオゾラッコ
    |http://example.com/image-that-is-ignored.png""".stripMargin.trim

  def mockStatus(
    source:        String             = StatusParser.GranblueSource,
    id:            Long               = 12345L,
    user:          User               = mockUser(),
    createdAt:     Date               = now,
    mediaEntities: Array[MediaEntity] = Array(mockMediaEntity()),
    text:          String             = validTweetText
  ): Status = {
    val status = mock[Status]
    doReturn(source).when(status).getSource
    doReturn(id).when(status).getId
    doReturn(user).when(status).getUser
    doReturn(createdAt).when(status).getCreatedAt
    doReturn(mediaEntities).when(status).getMediaEntities
    doReturn(text).when(status).getText
    status
  }

  def mockMediaEntity(
    mediaURLHttps: String = "http://example.com/raid-image.png"
  ): MediaEntity = {
    val entity = mock[MediaEntity]
    doReturn(mediaURLHttps).when(entity).getMediaURLHttps
    entity
  }

  def mockUser(
    screenName:           String = "walfieee",
    profileImageURLHttps: String = "http://example.com/profile-image_normal.png"
  ): User = {
    val user = mock[User]
    doReturn(screenName).when(user).getScreenName
    doReturn(profileImageURLHttps).when(user).getProfileImageURLHttps
    user
  }
}

