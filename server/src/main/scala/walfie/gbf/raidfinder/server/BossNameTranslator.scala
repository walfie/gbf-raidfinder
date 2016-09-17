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
  def update(latestBosses: Map[BossName, RaidBoss]): Future[Unit]
  def getTranslations(): Map[BossName, BossName]
}

class ImageBasedBossNameTranslator(
  initialTranslationData: Seq[ImageBasedBossNameTranslator.TranslationData]
)(implicit ec: ExecutionContext) extends BossNameTranslator {
  import ImageBasedBossNameTranslator._

  private val pHash = new ImagePHash()

  private val translationDataAgent: Agent[Map[BossName, TranslationData]] = Agent {
    initialTranslationData.map(data => data.name -> data).toMap
  }

  def getTranslationData(): Map[BossName, TranslationData] = translationDataAgent.get

  private val translationsAgent: Agent[Map[BossName, BossName]] = Agent {
    // TODO: Can be optimized since translations are two-way
    // i.e., "A translates to B" implies "B translates to A"
    translationDataAgent.get.values.flatMap { data =>
      findTranslation(data).map(data.name -> _)
    }.toMap
  }

  def getTranslations(): Map[BossName, BossName] = translationsAgent.get

  def update(latestBosses: Map[BossName, RaidBoss]): Future[Unit] = {
    val futures: Iterable[Future[Unit]] = latestBosses.map {
      case (name, boss) if boss.image.nonEmpty && !translationDataAgent.get.isDefinedAt(name) =>
        getTranslationData(boss).flatMap(updateTranslationData)
      case _ => Future.successful(()) // Ignore
    }

    Future.sequence(futures).map(_ => ())
  }

  private def updateTranslationData(translationData: TranslationData): Future[Unit] = {
    for {
      _ <- translationDataAgent.alter(_.updated(translationData.name, translationData))
      _ <- findTranslation(translationData).fold {
        Future.successful[Any](())
      } { translatedName =>
        translationsAgent.alter { translations: Map[BossName, BossName] =>
          translations + (translationData.name -> translatedName) + (translatedName -> translationData.name)
        }
      }
    } yield ()
  }

  def translate(bossName: BossName): Option[BossName] =
    translationsAgent.get.get(bossName)

  // IMPORTANT: Only call this on a boss that has an image, otherwise it will fail
  private def getTranslationData(boss: RaidBoss): Future[TranslationData] = BlockingIO.future {
    val imageUrl = new URL(boss.image.get + ":small") // TODO: Make this testable
    val hash = ImageHash(pHash.getHashAsLong(croppedImageFromUrl(imageUrl)))

    TranslationData(name = boss.name, level = boss.level, language = boss.language, hash = hash)
  }

  /** Read the image and crop out the bottom 25% */
  private def croppedImageFromUrl(url: URL): BufferedImage = {
    // TODO: Use a real HTTP client to get the image
    val image = ImageIO.read(url.openStream())
    image.getSubimage(0, 0, image.getWidth(), image.getHeight() * 3 / 4);
  }

  // This assumes there are only two languages (which is true currently)
  private def findTranslation(newData: TranslationData): Option[BossName] = {
    translationDataAgent.get.values.find { existingData =>
      newData.hash == existingData.hash &&
        newData.language != existingData.language &&
        newData.level == existingData.level
    }.map(_.name)
  }
}

object ImageBasedBossNameTranslator {
  case class TranslationData(name: BossName, level: Int, language: Language, hash: ImageHash)
  case class ImageHash(value: Long) extends AnyVal
}

