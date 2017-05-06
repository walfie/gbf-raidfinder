package walfie.gbf.raidfinder

import scala.math.Ordered
import scala.util.Try

package object protocol {
  type BossName = String

  private[protocol] val VersionRegex = """v?([0-9]+)\.([0-9]+)\.([0-9]+)-?(.*)""".r
}

package protocol {
  class BinaryProtobuf(val value: Array[Byte]) extends AnyVal
  object BinaryProtobuf {
    def apply(value: Array[Byte]): BinaryProtobuf =
      new BinaryProtobuf(value)
  }

  // Doesn't support versions with no bugfix segment
  // TODO: This throws a ton of errors on linking scala.js if this extends AnyVal.
  // Should figure out why. Maybe. It's not incredibly important.
  case class VersionString(value: String) {
    def parse(): Option[ParsedVersion] = value.trim match {
      case VersionRegex(major, minor, bugfix, prerelease) => Try {
        val optionalPrerelease = if (prerelease.isEmpty) None else Some(prerelease)
        ParsedVersion(major.toInt, minor.toInt, bugfix.toInt, optionalPrerelease)
      }.toOption
      case _ => None
    }
  }

  case class ParsedVersion(
    major: Int, minor: Int, bugfix: Int, prerelease: Option[String]
  ) extends Ordered[ParsedVersion] {
    import scala.math.Ordered.orderingToOrdered

    def compare(that: ParsedVersion): Int = {
      val thisTuple = (this.major, this.minor, this.bugfix, this.prerelease)
      val thatTuple = (that.major, that.minor, that.bugfix, that.prerelease)
      thisTuple.compare(thatTuple)
    }
  }
}

