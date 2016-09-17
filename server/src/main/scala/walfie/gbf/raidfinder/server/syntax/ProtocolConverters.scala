package walfie.gbf.raidfinder.server.syntax

import walfie.gbf.raidfinder.domain
import walfie.gbf.raidfinder.protocol
import walfie.gbf.raidfinder.server.{ImageBasedBossNameTranslator => translator}

/** Convenient converters between domain objects and protobuf objects */
object ProtocolConverters {
  implicit class RaidBossDomainOps(val rb: domain.RaidBoss) extends AnyVal {
    def toProtocol(): protocol.RaidBoss = protocol.RaidBoss(
      name = rb.name, level = rb.level, image = rb.image,
      lastSeen = rb.lastSeen, language = rb.language.toProtocol
    )
  }

  implicit class RaidBossProtocolOps(val rb: protocol.RaidBoss) extends AnyVal {
    def toDomain(): domain.RaidBoss = domain.RaidBoss(
      name = rb.name, level = rb.level, image = rb.image,
      lastSeen = rb.lastSeen, language = rb.language.toDomain(rb.name)
    )
  }

  implicit class TranslationDataDataDomainOps(val td: translator.TranslationData) extends AnyVal {
    def toProtocol(): protocol.TranslationData = protocol.TranslationData(
      name = td.name, level = td.level, imageHash = td.hash.value, language = td.language.toProtocol
    )
  }

  implicit class TranslationDataProtocolOps(val td: protocol.TranslationData) extends AnyVal {
    def toDomain(): translator.TranslationData = translator.TranslationData(
      name = td.name, level = td.level, hash = translator.ImageHash(td.imageHash),
      language = td.language.toDomain(td.name)
    )
  }

  implicit class LanguageDomainOps(val lang: domain.Language) extends AnyVal {
    def toProtocol(): protocol.Language = lang match {
      case domain.Language.Japanese => protocol.Language.JAPANESE
      case domain.Language.English => protocol.Language.ENGLISH
    }
  }

  implicit class LanguageProtocolOps(val lang: protocol.Language) extends AnyVal {
    def toDomain(bossName: => domain.BossName): domain.Language = lang match {
      case protocol.Language.ENGLISH => domain.Language.English
      case protocol.Language.JAPANESE => domain.Language.Japanese
      case protocol.Language.UNSPECIFIED | protocol.Language.Unrecognized(_) =>
        // English raid boses start with "Lvl " (e.g., "Lvl 100 InsertNameHere")
        if (bossName.startsWith("Lvl ")) domain.Language.English
        else domain.Language.Japanese
    }
  }

}

