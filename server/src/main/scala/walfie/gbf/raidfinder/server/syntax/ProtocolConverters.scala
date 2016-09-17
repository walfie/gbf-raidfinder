package walfie.gbf.raidfinder.server.syntax

import walfie.gbf.raidfinder.domain
import walfie.gbf.raidfinder.protocol

/** Convenient converters between domain objects and protobuf objects */
object ProtocolConverters {
  implicit class RaidBossDomainOps(val rb: domain.RaidBoss) extends AnyVal {
    def toProtocol(): protocol.RaidBoss = {
      val language = rb.language match {
        case domain.Language.Japanese => protocol.Language.JAPANESE
        case domain.Language.English => protocol.Language.ENGLISH
      }
      protocol.RaidBoss(
        name = rb.name, level = rb.level, image = rb.image,
        lastSeen = rb.lastSeen, language = language
      )
    }
  }

  implicit class RaidBossProtocolOps(val rb: protocol.RaidBoss) extends AnyVal {
    def toDomain(): domain.RaidBoss = {
      val language = rb.language match {
        case protocol.Language.ENGLISH => domain.Language.English
        case protocol.Language.JAPANESE => domain.Language.Japanese
        case protocol.Language.UNSPECIFIED | protocol.Language.Unrecognized(_) =>
          // English raid boses start with "Lvl " (e.g., "Lvl 100 InsertNameHere")
          if (rb.name.startsWith("Lvl ")) domain.Language.English
          else domain.Language.Japanese
      }

      domain.RaidBoss(
        name = rb.name, level = rb.level, image = rb.image,
        lastSeen = rb.lastSeen, language = language
      )
    }
  }
}

