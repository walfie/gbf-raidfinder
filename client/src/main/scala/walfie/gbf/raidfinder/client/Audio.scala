package walfie.gbf.raidfinder.client

import org.scalajs.dom.raw.HTMLAudioElement
import scala.scalajs.js

package audio {
  case class NotificationSound(id: NotificationSoundId, fileName: String) {
    def play(): Unit = {
      js.Dynamic.newInstance(js.Dynamic.global.Audio)(pathPrefix + fileName)
        .asInstanceOf[HTMLAudioElement]
        .play()
    }
  }
}

package object audio {
  type NotificationSoundId = Int

  private[audio] val pathPrefix = "audio/"

  val NotificationSounds: Map[NotificationSoundId, NotificationSound] = Seq(
    0 -> "oh-finally.ogg",
    1 -> "oringz-w425.ogg",
    2 -> "pedantic.ogg",
    3 -> "suppressed.ogg",
    4 -> "system.ogg",
    5 -> "tweet.ogg",
    6 -> "youve-been-informed.ogg",
    7 -> "you-wouldnt-believe.ogg"
  ).map {
      case (id, fileName) => id -> NotificationSound(id, fileName)
    }.toMap
}

