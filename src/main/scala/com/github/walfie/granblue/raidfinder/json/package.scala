package com.github.walfie.granblue.raidfinder

import com.github.walfie.granblue.raidfinder.actor.WebsocketHandler.Protocol._
import play.api.libs.functional.syntax._
import play.api.libs.json._
import scala.util.{Try, Success, Failure}

package object json {
  def parseStringAsJson(s: String): JsResult[JsValue] = {
    Try(Json.parse(s)) match {
      case Success(v) => JsSuccess(v)
      case Failure(e) => JsError("Invalid JSON: " + e.getMessage)
    }
  }

  implicit val ResponseWrites: Writes[Response] = Writes { response: Response =>
    response match {
      case r: RaidsResponse => RaidsResponse.writes(r)
      case r: RaidBossesResponse => RaidBossesResponseWrites.writes(r)
    }
  }

  implicit val RaidBossWrites: Writes[RaidBoss] =
    Json.writes[RaidBoss]

  implicit val RaidWrites: Writes[Raid] =
    Json.writes[Raid]

  implicit val RaidBossesResponseWrites: Writes[RaidBossesResponse] =
    Json.writes[RaidBossesResponse].messageType("RaidBossesResponse")

  implicit val RaidsResponse: Writes[RaidsResponse] =
    Json.writes[RaidsResponse].messageType("RaidsResponse")

  implicit val RequestReads: Reads[Request] = Reads { json: JsValue =>
    (json \ "type").validate[String].flatMap {
      case "SubscribeRequest" => SubscribeRequestReads.reads(json)
      case "UnsubscribeRequest" => UnsubscribeRequestReads.reads(json)
      case "RaidBossesRequest" => JsSuccess(RaidBossesRequest)
    }
  }

  implicit val SubscribeRequestReads: Reads[SubscribeRequest] =
    Json.reads[SubscribeRequest]

  implicit val UnsubscribeRequestReads: Reads[UnsubscribeRequest] =
    Json.reads[UnsubscribeRequest]

  implicit final class WritesOps[T](val self: Writes[T]) extends AnyVal {
    def messageType(messageType: String): Writes[T] =
      addField("type", messageType)

    def addField[U: Writes](fieldName: String, value: U): Writes[T] =
      addPath(__ \ fieldName, value)

    def addPath[U: Writes](path: JsPath, value: U): Writes[T] = {
      (toOWrites() ~ path.write[U])(_ -> value)
    }

    def toOWrites(): OWrites[T] = OWrites(self.writes(_).as[JsObject])
  }
}

