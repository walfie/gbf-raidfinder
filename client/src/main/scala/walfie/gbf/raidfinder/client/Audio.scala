package walfie.gbf.raidfinder.client

import org.scalajs.dom.raw.HTMLAudioElement
import scala.scalajs.js

package audio {
  case class NotificationSound(id: NotificationSoundId, fileName: String) {
    def play(): Unit = {
      NotificationSounds.audioCache.getOrElseUpdate(fileName, {
        js.Dynamic
          .newInstance(js.Dynamic.global.Audio)(pathPrefix + fileName)
          .asInstanceOf[HTMLAudioElement]
      }).play()
    }
  }
}

package object audio {
  type NotificationSoundId = Int

  private[audio] val pathPrefix = "audio/"

  object NotificationSounds {
    // When adding new items, don't change the numeric IDs
    val all: Seq[NotificationSound] = Seq(
      1 -> "oh-finally.ogg",
      2 -> "oringz-w425.ogg",
      3 -> "pedantic.ogg",
      4 -> "suppressed.ogg",
      5 -> "system.ogg",
      6 -> "tweet.ogg",
      7 -> "you-wouldnt-believe.ogg",
      8 -> "youve-been-informed.ogg"
    ).map { case (id, fileName) => NotificationSound(id, fileName) }.sortBy(_.fileName)

    val findById: NotificationSoundId => Option[NotificationSound] =
      all.map(n => n.id -> n).toMap.get _

    private[audio] var audioCache: js.Dictionary[HTMLAudioElement] = js.Dictionary()
  }

}

