package walfie.gbf.raidfinder.server

import akka.agent.Agent
import com.pastebin.Pj9d8jt5.ImagePHash
import java.awt.image.BufferedImage
import java.net.URL
import javax.imageio.ImageIO
import monix.execution.Scheduler
import monix.reactive._
import monix.reactive.subjects.ConcurrentSubject
import scala.concurrent.{ExecutionContext, Future}
import walfie.gbf.raidfinder.domain._
import walfie.gbf.raidfinder.util.BlockingIO

trait BossNameTranslator {
  import BossNameTranslator.Translation

  def translate(bossName: BossName): Option[BossName]
  def update(latestBosses: Map[BossName, RaidBoss]): Future[Unit]
  def observable(): Observable[Translation]
}

object BossNameTranslator {
  case class Translation(from: BossName, to: BossName)
}

/**
  * @param initialTranslationData Initial image hashes to use
  * {@param manualOverrides
  *  Sometimes Granblue's boss images are just wrong between the English and Japanese
  *  versions (e.g., Lvl 100 Medusa). Use this for manual overrides.
  * }
  */
class ImageBasedBossNameTranslator(
  initialTranslationData: Seq[ImageBasedBossNameTranslator.TranslationData],
  manualOverrides:        Map[BossName, BossName]
)(implicit scheduler: Scheduler) extends BossNameTranslator {
  import ImageBasedBossNameTranslator._
  import BossNameTranslator.Translation

  private val overrides = manualOverrides ++ manualOverrides.map(_.swap)

  private val pHash = new ImagePHash()

  private val translationDataAgent: Agent[Map[BossName, TranslationData]] = Agent {
    initialTranslationData.map(data => data.name -> data)(scala.collection.breakOut)
  }

  private val subject = ConcurrentSubject.publish[Translation]
  val observable: Observable[Translation] = subject

  def getTranslationData(): Map[BossName, TranslationData] = translationDataAgent.get

  private val translationsAgent: Agent[Map[BossName, BossName]] = Agent {
    // TODO: Can be optimized since translations are two-way
    // i.e., "A translates to B" implies "B translates to A"
    translationDataAgent.get.values.flatMap { data =>
      findTranslation(data).map(data.name -> _)
    }(scala.collection.breakOut)
  }

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
          // Update the Observable with new translations
          subject.onNext(Translation(translationData.name, translatedName))
          subject.onNext(Translation(translatedName, translationData.name))

          // Update the Agent's stored Map
          translations + (translationData.name -> translatedName) + (translatedName -> translationData.name)
        }
      }
    } yield ()
  }

  def translate(bossName: BossName): Option[BossName] =
    overrides.get(bossName) orElse translationsAgent.get.get(bossName)

  // IMPORTANT: Only call this on a boss that has an image, otherwise it will fail
  private def getTranslationData(boss: RaidBoss): Future[TranslationData] = BlockingIO.future {
    // Must use large image because thumb/small sizes have too much variance when shifted slightly
    // TODO: Make this testable
    val imageUrl = new URL(boss.image.get + ":large")
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

