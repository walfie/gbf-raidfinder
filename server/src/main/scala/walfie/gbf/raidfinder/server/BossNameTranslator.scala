package walfie.gbf.raidfinder.server

import akka.agent.Agent
import com.pastebin.Pj9d8jt5.ImagePHash
import java.net.URL
import scala.concurrent.{ExecutionContext, Future}
import walfie.gbf.raidfinder.domain._
import walfie.gbf.raidfinder.util.BlockingIO

trait BossNameTranslator {
  def translate(bossName: BossName): Option[BossName]
  def update(latestBosses: Map[BossName, RaidBoss]): Unit
}

class ImageBasedBossNameTranslator(
  initialBossData:          Seq[ImageBasedBossNameTranslator.BossData],
  imageSimilarityThreshold: Double
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
    val imageStream = new URL(boss.image.get + ":small").openStream // TODO: Make this testable
    val hash = ImageHash(pHash.getHashAsLong(imageStream))
    BossData(name = boss.name, level = boss.level, language = boss.language, hash: ImageHash)
  }

  private case class BossSimilarity(name: BossName, similarity: Double)

  // This assumes there are only two languages (which is true currently)
  private def findTranslation(newData: BossData): Option[BossName] = {
    val similarities = bossDataAgent.get.values.collect {
      case existingData if shouldCompare(newData, existingData) =>
        val similarity = pHash.similarity(newData.hash.value, existingData.hash.value)

        BossSimilarity(
          name = existingData.name,
          similarity = similarity
        )
    }.filter(_.similarity >= imageSimilarityThreshold)

    if (similarities.isEmpty) None
    else Some(similarities.maxBy(_.similarity).name)
  }

  private def shouldCompare(data1: BossData, data2: BossData): Boolean = {
    data1.language != data2.language && data1.level == data2.level
  }
}

object ImageBasedBossNameTranslator {
  case class BossData(name: BossName, level: Int, language: Language, hash: ImageHash)
  case class ImageHash(value: Long) extends AnyVal {
    def similarity(otherHash: ImageHash)(implicit pHash: ImagePHash) = {
      pHash.similarity(value, otherHash.value)
    }
  }
}

