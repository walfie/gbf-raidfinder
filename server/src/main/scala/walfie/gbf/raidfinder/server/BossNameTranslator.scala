package walfie.gbf.raidfinder.server

import akka.agent.Agent
import com.pastebin.Pj9d8jt5.ImagePHash
import java.awt.image.BufferedImage
import java.net.URL
import javax.imageio.ImageIO
import scala.concurrent.{ExecutionContext, Future}
import walfie.gbf.raidfinder.domain._
import walfie.gbf.raidfinder.util.BlockingIO

// TODO: Add an Observable for new translation notifications?
trait BossNameTranslator {
  def translate(bossName: BossName): Option[BossName]
  def update(latestBosses: Map[BossName, RaidBoss]): Unit
}

class ImageBasedBossNameTranslator(
  initialBossData: Seq[ImageBasedBossNameTranslator.BossData]
)(implicit ec: ExecutionContext) extends BossNameTranslator {
  import ImageBasedBossNameTranslator._

  private val pHash = new ImagePHash()

  private val bossDataAgent: Agent[Map[BossName, BossData]] = Agent {
    initialBossData.map(data => data.name -> data).toMap
  }

  private val translationsAgent: Agent[Map[BossName, BossName]] = Agent {
    // TODO: Can be optimized since translations are two-way
    // i.e., "A translates to B" implies "B translates to A"
    bossDataAgent.get.values.flatMap { data =>
      findTranslation(data).map(data.name -> _)
    }.toMap
  }

  def update(latestBosses: Map[BossName, RaidBoss]): Unit = {
    latestBosses.foreach {
      case (name, boss) if boss.image.nonEmpty && !bossDataAgent.get.isDefinedAt(name) =>
        getBossData(boss).foreach(updateBossData)
      case _ => () // Ignore
    }
  }

  private def updateBossData(bossData: BossData): Unit = {
    bossDataAgent.alter(_.updated(bossData.name, bossData)).foreach { _ =>
      findTranslation(bossData).foreach { translatedName =>
        val newValues = Map(
          bossData.name -> translatedName,
          translatedName -> bossData.name
        )
        translationsAgent.send(_ ++ newValues)
      }
    }
  }

  def translate(bossName: BossName): Option[BossName] =
    translationsAgent.get.get(bossName)

  // IMPORTANT: Only call this on a boss that has an image, otherwise it will fail
  private def getBossData(boss: RaidBoss): Future[BossData] = BlockingIO.future {
    val imageUrl = new URL(boss.image.get + ":small") // TODO: Make this testable
    val hash = ImageHash(pHash.getHashAsLong(croppedImageFromUrl(imageUrl)))

    BossData(name = boss.name, level = boss.level, language = boss.language, hash: ImageHash)
  }

  /** Read the image and crop out the bottom 25% */
  private def croppedImageFromUrl(url: URL): BufferedImage = {
    // TODO: Use a real HTTP client to get the image
    val image = ImageIO.read(url.openStream())
    image.getSubimage(0, 0, image.getWidth(), image.getHeight() * 3 / 4);
  }

  // This assumes there are only two languages (which is true currently)
  private def findTranslation(newData: BossData): Option[BossName] = {
    bossDataAgent.get.values.find { existingData =>
      newData.hash == existingData.hash &&
        newData.language != existingData.language &&
        newData.level == existingData.level
    }.map(_.name)
  }
}

object ImageBasedBossNameTranslator {
  case class Translation(from: BossName, to: BossName)
  case class BossData(name: BossName, level: Int, language: Language, hash: ImageHash)
  case class ImageHash(value: Long) extends AnyVal
}

