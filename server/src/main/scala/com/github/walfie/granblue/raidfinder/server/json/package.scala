package com.github.walfie.granblue.raidfinder.server

import com.github.walfie.granblue.raidfinder.domain._
import com.github.walfie.granblue.raidfinder.server.protocol._
import play.api.libs.functional.syntax._
import play.api.libs.json._
import scala.util.{Try, Success, Failure}

package object json {
  implicit val WebsocketResponseWrites: Writes[WebsocketResponse] =
    Writes { response: WebsocketResponse =>
      response match {
        case r: RaidTweetWrapper => RaidTweetWrapperWrites.writes(r)
        case r: Subscribed => SubscribedWrites.writes(r)
        case r: RaidBosses => RaidBossesWrites.writes(r)
      }
    }

  implicit val RaidTweetWrapperWrites: Writes[RaidTweetWrapper] = Writes { rtw =>
    Json.writes[RaidTweet].messageType("RaidTweet").writes(rtw.underlying)
  }

  implicit val SubscribedWrites: Writes[Subscribed] =
    Json.writes[Subscribed].messageType("Subscribed")

  implicit val RaidBossWrites: Writes[RaidBoss] =
    Json.writes[RaidBoss] // messageType not needed because this is inside `RaidBosses` response

  implicit val RaidBossesWrites: Writes[RaidBosses] =
    Json.writes[RaidBosses].messageType("RaidBosses")

  implicit val WebsocketRequestReads: Reads[WebsocketRequest[_]] =
    Reads { json: JsValue =>
      (json \ "type").validate[String].flatMap {
        case "Subscribe" => SubscribeReads.reads(json)
        case "Unsubscribe" => UnsubscribeReads.reads(json)
        case "GetRaidBosses" => JsSuccess(GetRaidBosses)
      }
    }

  implicit val SubscribeReads: Reads[Subscribe] =
    Json.reads[Subscribe]

  implicit val UnsubscribeReads: Reads[Unsubscribe] =
    Json.reads[Unsubscribe]

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

