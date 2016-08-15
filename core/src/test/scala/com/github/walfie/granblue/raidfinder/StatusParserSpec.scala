package com.github.walfie.granblue.raidfinder

import com.github.walfie.granblue.raidfinder.domain._
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

    // Have to check each part manually since Mockito has issues with AnyVal
    val parsed = StatusParser.parse(validStatus)
    parsed.map(_.tweet) shouldBe Some(expectedRaidTweet)
    parsed.map(_.image.get.url) shouldBe Some("http://example.com/raid-image.png")
  }

  // Testing this is going to be very annoying
  "return None on invalid status" in pending
}

trait StatusParserSpecHelpers extends FreeSpec with MockitoSugar {
  val now = new java.util.Date()

  def validStatus(): Status = {
    val status = mock[Status]
    doReturn(StatusParser.GranblueSource).when(status).getSource
    doReturn(12345L).when(status).getId
    doReturn(mockUser).when(status).getUser
    doReturn(now).when(status).getCreatedAt
    doReturn(Array(mockMediaEntity)).when(status).getMediaEntities
    val tweetText = """
      |INSERT CUSTOM MESSAGE HERE 参加者募集！参戦ID：ABCD1234
      |Lv60 Ozorotter
      |http://example.com/image-that-is-ignored.png""".stripMargin.trim

    doReturn(tweetText).when(status).getText
    status
  }

  def mockMediaEntity(): MediaEntity = {
    val entity = mock[MediaEntity]
    when(entity.getMediaURLHttps) thenReturn "http://example.com/raid-image.png"
    entity
  }

  def mockUser(): User = {
    val user = mock[User]
    when(user.getScreenName) thenReturn "walfieee"
    when(user.getProfileImageURLHttps) thenReturn "http://example.com/profile-image_normal.png"
    user
  }
}

