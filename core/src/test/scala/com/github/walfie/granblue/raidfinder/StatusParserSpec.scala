package com.github.walfie.granblue.raidfinder

import com.github.walfie.granblue.raidfinder.domain._
import java.util.Date
import org.mockito.Mockito._
import org.scalatest.Matchers._
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{Status => ScalaTestStatus, _}
import scala.collection.JavaConverters._
import twitter4j._

class StatusParserSpec extends StatusParserSpecHelpers {
  "parse a valid status" in {
    val expectedRaidTweet = RaidTweet(
      tweetId = 12345L,
      screenName = "walfieee",
      bossName = "Lv60 Ozorotter",
      raidId = "ABCD1234",
      profileImage = "http://example.com/profile-image_normal.png",
      text = "INSERT CUSTOM MESSAGE HERE",
      createdAt = now
    )

    val expectedImageUrl = "http://example.com/raid-image.png"

    // Can't compare the whole case class because of an issue with Mockito and AnyVal
    StatusParser.parse(mockStatus()).map { parsed =>
      parsed.image.map(_.url) shouldBe Some(expectedImageUrl)
      parsed.tweet shouldBe expectedRaidTweet
    }
  }

  "non-official client" in {
    StatusParser.parse(mockStatus(source = "TweetDeck")) shouldBe None
  }

  "invalid text" in {
    val haiku = "#GranblueHaiku http://example.com/haiku.png"
    StatusParser.parse(mockStatus(text = haiku)) shouldBe None
  }
}

trait StatusParserSpecHelpers extends FreeSpec with MockitoSugar {
  val now = new java.util.Date()

  val validTweetText = """
    |INSERT CUSTOM MESSAGE HERE 参加者募集！参戦ID：ABCD1234
    |Lv60 Ozorotter
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

