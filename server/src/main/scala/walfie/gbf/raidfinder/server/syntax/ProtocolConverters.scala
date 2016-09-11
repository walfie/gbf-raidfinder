package walfie.gbf.raidfinder.server.syntax

import walfie.gbf.raidfinder.domain
import walfie.gbf.raidfinder.protocol

/** Convenient converters between domain objects and protobuf objects */
object ProtocolConverters {
  implicit class RaidBossDomainOps(val rb: domain.RaidBoss) extends AnyVal {
    def toProtocol(): protocol.RaidBoss = protocol.RaidBoss(
      name = rb.name, level = rb.level, image = rb.image, lastSeen = rb.lastSeen
    )
  }

  implicit class RaidBossProtocolOps(val rb: protocol.RaidBoss) extends AnyVal {
    def toDomain(): domain.RaidBoss = domain.RaidBoss(
      name = rb.name, level = rb.level, image = rb.image, lastSeen = rb.lastSeen
    )
  }
}

