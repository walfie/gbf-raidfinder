package com.pastebin.Pj9d8jt5

import java.awt.image.BufferedImage
import java.net.URL
import javax.imageio.ImageIO
import org.scalatest._
import org.scalatest.Matchers._
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import walfie.gbf.raidfinder.util.BlockingIO

class ImagePHashSpec extends FreeSpec {
  val pHash = new ImagePHash

  case class BossSimilarity(similarity: Double, boss1: String, boss2: String)

  sealed trait Language
  case object JP extends Language
  case object EN extends Language
  case class RaidBoss(name: String, level: Int, language: Language, url: String)

  case class RaidBossWithHash(boss: RaidBoss, hash: Long)

  "Find similar boss images" in {
    import scala.concurrent.ExecutionContext.Implicits.global

    val hashesF = Future.sequence {
      (japaneseBosses ++ englishBosses).map { boss =>
        val url = boss.url + ":large"

        BlockingIO.future {
          // Get image and crop out the bottom 25%
          val stream = new URL(url).openStream
          val image: BufferedImage = ImageIO.read(stream)
          val croppedImage = image.getSubimage(
            0, 0, image.getWidth(), image.getHeight() * 3/4
          )

          RaidBossWithHash(boss, pHash.getHashAsLong(croppedImage))
        }
      }
    }

    val hashes = Await.result(hashesF, 30.seconds)

    def shouldCompare(boss1: RaidBoss, boss2: RaidBoss): Boolean = {
      (boss1.language != boss2.language) && (boss1.level == boss2.level)
    }

    val results = hashes.toSeq.combinations(2).collect {
      case Seq(hashed1, hashed2) if shouldCompare(hashed1.boss, hashed2.boss) =>
        val similarity = pHash.similarity(hashed1.hash, hashed2.hash)
        BossSimilarity(similarity, hashed1.boss.name, hashed2.boss.name)
    }.filter(_.similarity == 1.0)

    results.map(b => b.boss1 -> b.boss2).toSet shouldBe Set(
      "Lv30 アーフラー" -> "Lvl 30 Ahura",
      "Lv40 アーフラー" -> "Lvl 40 Ahura",
      "Lv50 ベオウルフ" -> "Lvl 50 Grendel",
      "Lv60 ベオウルフ" -> "Lvl 60 Grendel",

      "Lv40 ゲイザー" -> "Lvl 40 Ogler",
      "Lv40 ヨグ＝ソトース" -> "Lvl 40 Yog-Sothoth",
      "Lv60 グガランナ" -> "Lvl 60 Gugalanna",
      "Lv60 マヒシャ" -> "Lvl 60 Mahisha",
      "Lv75 スーペルヒガンテ" -> "Lvl 75 Supergigante",
      "Lv75 エメラルドホーン" -> "Lvl 75 Viridian Horn",

      "Lv50 セレスト" -> "Lvl 50 Celeste",
      "Lv50 ティアマト" -> "Lvl 50 Tiamat",
      "Lv50 ティアマト・マグナ" -> "Lvl 50 Tiamat Omega",
      "Lv50 ユグドラシル" -> "Lvl 50 Yggdrasil",
      "Lv50 リヴァイアサン" -> "Lvl 50 Leviathan",
      "Lv50 ヴェセラゴ" -> "Lvl 50 Veselago",
      "Lv60 ユグドラシル・マグナ" -> "Lvl 60 Yggdrasil Omega",
      "Lv60 リヴァイアサン・マグナ" -> "Lvl 60 Leviathan Omega",
      "Lv70 コロッサス・マグナ" -> "Lvl 70 Colossus Omega",
      "Lv75 シュヴァリエ・マグナ" -> "Lvl 75 Luminiera Omega",
      "Lv75 セレスト・マグナ" -> "Lvl 75 Celeste Omega",
      "Lv100 Dエンジェル・オリヴィエ" -> "Lvl 100 Dark Angel Olivia",
      "Lv100 アテナ" -> "Lvl 100 Athena",
      "Lv100 アポロン" -> "Lvl 100 Apollo",
      "Lv100 オーディン" -> "Lvl 100 Odin",
      "Lv100 ガルーダ" -> "Lvl 100 Garuda",
      "Lv100 グラニ" -> "Lvl 100 Grani",
      "Lv100 コロッサス・マグナ" -> "Lvl 100 Colossus Omega",
      "Lv100 シュヴァリエ・マグナ" -> "Lvl 100 Luminiera Omega",
      "Lv100 ジ・オーダー・グランデ" -> "Lvl 100 Grand Order",
      "Lv100 セレスト・マグナ" -> "Lvl 100 Celeste Omega",
      "Lv100 ティアマト・マグナ＝エア" -> "Lvl 100 Tiamat Omega Ayr",
      "Lv100 ナタク" -> "Lvl 100 Nezha",
      "Lv100 バアル" -> "Lvl 100 Baal",
      "Lv100 フラム＝グラス" -> "Lvl 100 Twin Elements",
      "Lv100 プロトバハムート" -> "Lvl 100 Proto Bahamut",
      "Lv100 マキュラ・マリウス" -> "Lvl 100 Macula Marius",
      "Lv100 メドゥーサ" -> "Lvl 100 Medusa",
      "Lv100 ユグドラシル・マグナ" -> "Lvl 100 Yggdrasil Omega",
      "Lv100 リッチ" -> "Lvl 100 Lich",
      "Lv100 リヴァイアサン・マグナ" -> "Lvl 100 Leviathan Omega",
      "Lv120 Dエンジェル・オリヴィエ" -> "Lvl 120 Dark Angel Olivia",
      "Lv120 アポロン" -> "Lvl 120 Apollo",
      "Lv120 ナタク" -> "Lvl 120 Nezha",
      "Lv120 フラム＝グラス" -> "Lvl 120 Twin Elements",
      "Lv120 マキュラ・マリウス" -> "Lvl 120 Macula Marius",
      "Lv150 プロトバハムート" -> "Lvl 150 Proto Bahamut"
    )
  }

  lazy val japaneseBosses = List(
    RaidBoss("Lv30 アーフラー", 30, JP, "https://pbs.twimg.com/media/CeSO4quUYAAy8U1.jpg"),
    RaidBoss("Lv40 アーフラー", 40, JP, "https://pbs.twimg.com/media/CeSO6aAWAAAAOGe.jpg"),
    RaidBoss("Lv50 ベオウルフ", 50, JP, "https://pbs.twimg.com/media/CeSO8gHWAAEnMGl.jpg"),
    RaidBoss("Lv60 ベオウルフ", 60, JP, "https://pbs.twimg.com/media/CeSO-QrWwAECCG1.jpg"),

    RaidBoss("Lv40 ゲイザー", 40, JP, "https://pbs.twimg.com/media/Cn7opV5VUAAfgDz.jpg"),
    RaidBoss("Lv40 ヨグ＝ソトース", 40, JP, "https://pbs.twimg.com/media/Cqlrx16VUAAU1-Z.jpg"),
    RaidBoss("Lv60 グガランナ", 60, JP, "https://pbs.twimg.com/media/Cn7o2sYVYAEP45o.jpg"),
    RaidBoss("Lv60 マヒシャ", 60, JP, "https://pbs.twimg.com/media/Cqlr4E5VMAAOGqb.jpg"),
    RaidBoss("Lv75 スーペルヒガンテ", 75, JP, "https://pbs.twimg.com/media/Cqlr_RRVYAEabiO.jpg"),
    RaidBoss("Lv75 エメラルドホーン", 75, JP, "https://pbs.twimg.com/media/Cn7o767UkAEX8-H.jpg"),

    RaidBoss("Lv30 クレイゴーレム", 30, JP, "https://pbs.twimg.com/media/Crtprh7UIAARax2.jpg"),
    RaidBoss("Lv30 コロッサス", 30, JP, "https://pbs.twimg.com/media/CT6cUf8VEAEBaEb.jpg"),
    RaidBoss("Lv30 セレスト", 30, JP, "https://pbs.twimg.com/media/CT6cmzjUcAIvSo_.jpg"),
    RaidBoss("Lv30 ティアマト", 30, JP, "https://pbs.twimg.com/media/CT6cLx4VAAAzePV.jpg"),
    RaidBoss("Lv50 アドウェルサ", 50, JP, "https://pbs.twimg.com/media/CT6cgc5UYAIhJoB.jpg"),
    RaidBoss("Lv50 コロッサス", 50, JP, "https://pbs.twimg.com/media/CT6cUf8VEAEBaEb.jpg"),
    RaidBoss("Lv50 セレスト", 50, JP, "https://pbs.twimg.com/media/CT6cmzjUcAIvSo_.jpg"),
    RaidBoss("Lv50 ティアマト", 50, JP, "https://pbs.twimg.com/media/CT6cLx4VAAAzePV.jpg"),
    RaidBoss("Lv50 ティアマト・マグナ", 50, JP, "https://pbs.twimg.com/media/CT6buTPUwAIM9VJ.jpg"),
    RaidBoss("Lv50 ユグドラシル", 50, JP, "https://pbs.twimg.com/media/CT6cbScU8AAgXRw.jpg"),
    RaidBoss("Lv50 リヴァイアサン", 50, JP, "https://pbs.twimg.com/media/CT6cXcgUEAEc0Zl.jpg"),
    RaidBoss("Lv50 ヴェセラゴ", 50, JP, "https://pbs.twimg.com/media/Crtpt5RUAAAV6OG.jpg"),
    RaidBoss("Lv60 ユグドラシル・マグナ", 60, JP, "https://pbs.twimg.com/media/CT6cDD3UkAEnP8Y.jpg"),
    RaidBoss("Lv60 リヴァイアサン・マグナ", 60, JP, "https://pbs.twimg.com/media/CT6cBK3U8AA4xdW.jpg"),
    RaidBoss("Lv70 コロッサス・マグナ", 70, JP, "https://pbs.twimg.com/media/CT6bwJTUYAA6mcV.jpg"),
    RaidBoss("Lv75 シュヴァリエ・マグナ", 75, JP, "https://pbs.twimg.com/media/CT6cEwEUcAAlwFM.jpg"),
    RaidBoss("Lv75 セレスト・マグナ", 75, JP, "https://pbs.twimg.com/media/CT6cGF4UYAAHBg5.jpg"),
    RaidBoss("Lv100 Dエンジェル・オリヴィエ", 100, JP, "https://pbs.twimg.com/media/CT6csqNVAAA_GFU.jpg"),
    RaidBoss("Lv100 アテナ", 100, JP, "https://pbs.twimg.com/media/Cg7oAJsUkAApRif.jpg"),
    RaidBoss("Lv100 アポロン", 100, JP, "https://pbs.twimg.com/media/CT6chwtUsAA0WFw.jpg"),
    RaidBoss("Lv100 オーディン", 100, JP, "https://pbs.twimg.com/media/CqwDU_jUkAQjgKq.jpg"),
    RaidBoss("Lv100 ガルーダ", 100, JP, "https://pbs.twimg.com/media/CkVbhuqVEAA7e6K.jpg"),
    RaidBoss("Lv100 グラニ", 100, JP, "https://pbs.twimg.com/media/CqwDXDkUMAAjCE5.jpg"),
    RaidBoss("Lv100 コロッサス・マグナ", 100, JP, "https://pbs.twimg.com/media/CVL2CmeUwAAElDW.jpg"),
    RaidBoss("Lv100 シュヴァリエ・マグナ", 100, JP, "https://pbs.twimg.com/media/CbTeQ1fVIAAEoqi.jpg"),
    RaidBoss("Lv100 ジ・オーダー・グランデ", 100, JP, "https://pbs.twimg.com/media/CdL4YeiUEAI0JKW.jpg"),
    RaidBoss("Lv100 セレスト・マグナ", 100, JP, "https://pbs.twimg.com/media/CbTeWuMUUAIzFZl.jpg"),
    RaidBoss("Lv100 ティアマト・マグナ＝エア", 100, JP, "https://pbs.twimg.com/media/CT6cNUBUAAETdz6.jpg"),
    RaidBoss("Lv100 ナタク", 100, JP, "https://pbs.twimg.com/media/CT6cOzzUwAESsq_.jpg"),
    RaidBoss("Lv100 バアル", 100, JP, "https://pbs.twimg.com/media/CjLxgrbUgAAFbwi.jpg"),
    RaidBoss("Lv100 フラム＝グラス", 100, JP, "https://pbs.twimg.com/media/CT_qpfCUsAA9vfF.jpg"),
    RaidBoss("Lv100 プロトバハムート", 100, JP, "https://pbs.twimg.com/media/CT6cIKmUYAAPVmD.jpg"),
    RaidBoss("Lv100 マキュラ・マリウス", 100, JP, "https://pbs.twimg.com/media/CT6cYp-UsAAy_f0.jpg"),
    RaidBoss("Lv100 メドゥーサ", 100, JP, "https://pbs.twimg.com/media/CT6ccesVEAAy_Kx.jpg"),
    RaidBoss("Lv100 ユグドラシル・マグナ", 100, JP, "https://pbs.twimg.com/media/CYBkhTmUsAAPjgu.jpg"),
    RaidBoss("Lv100 リッチ", 100, JP, "https://pbs.twimg.com/media/CqwDZKwVMAAxt0Y.jpg"),
    RaidBoss("Lv100 リヴァイアサン・マグナ", 100, JP, "https://pbs.twimg.com/media/CYBkbZSUAAA2BW-.jpg"),
    RaidBoss("Lv110 ローズクイーン", 110, JP, "https://pbs.twimg.com/media/CUnrstgUYAA4hOz.jpg"),
    RaidBoss("Lv120 Dエンジェル・オリヴィエ", 120, JP, "https://pbs.twimg.com/media/CbTeSqbUcAARoNV.jpg"),
    RaidBoss("Lv120 アポロン", 120, JP, "https://pbs.twimg.com/media/CbTeO4fUkAEmmIN.jpg"),
    RaidBoss("Lv120 ギルガメッシュ", 120, JP, "https://pbs.twimg.com/media/CqG0X1tUkAA5B8_.jpg"),
    RaidBoss("Lv120 ナタク", 120, JP, "https://pbs.twimg.com/media/CT6cQD-UcAE3nt2.jpg"),
    RaidBoss("Lv120 フラム＝グラス", 120, JP, "https://pbs.twimg.com/media/CVL2EBHUwAA8nUj.jpg"),
    RaidBoss("Lv120 マキュラ・マリウス", 120, JP, "https://pbs.twimg.com/media/CYBkd_1UEAATH9J.jpg"),
    RaidBoss("Lv120 メドゥーサ", 120, JP, "https://pbs.twimg.com/media/CYBki-CUkAQVWW_.jpg"),
    RaidBoss("Lv150 プロトバハムート", 150, JP, "https://pbs.twimg.com/media/CdL4WyxUYAIXPb8.jpg")
  )

  lazy val englishBosses = List(
    RaidBoss("Lvl 30 Ahura", 30, EN, "https://pbs.twimg.com/media/Cs1w7-QUIAApnHh.jpg"),
    RaidBoss("Lvl 40 Ahura", 40, EN, "https://pbs.twimg.com/media/Cs1w9nhVIAABgqg.jpg"),
    RaidBoss("Lvl 50 Grendel", 50, EN, "https://pbs.twimg.com/media/Cs1w-68UEAEjhEV.jpg"),
    RaidBoss("Lvl 60 Grendel", 60, EN, "https://pbs.twimg.com/media/Cs1xAKcVIAEXC9a.jpg"),

    RaidBoss("Lvl 40 Ogler", 40, EN, "https://pbs.twimg.com/media/Cn7oqbPUkAEimW2.jpg"),
    RaidBoss("Lvl 40 Yog-Sothoth", 40, EN, "https://pbs.twimg.com/media/Cqlr0pRVUAQxZ5g.jpg"),
    RaidBoss("Lvl 60 Mahisha", 60, EN, "https://pbs.twimg.com/media/Cqlr51CUAAA9lp3.jpg"),
    RaidBoss("Lvl 60 Gugalanna", 60, EN, "https://pbs.twimg.com/media/Cn7o6ppVUAArahp.jpg"),
    RaidBoss("Lvl 75 Supergigante", 75, EN, "https://pbs.twimg.com/media/CqlsAzGUkAAAcRT.jpg"),
    RaidBoss("Lvl 75 Viridian Horn", 75, EN, "https://pbs.twimg.com/media/Cn7o85cUEAAnlh0.jpg"),

    RaidBoss("Lvl 50 Celeste", 50, EN, "https://pbs.twimg.com/media/CfqXLhHUEAAF92L.jpg"),
    RaidBoss("Lvl 50 Leviathan", 50, EN, "https://pbs.twimg.com/media/CfqW-MVUIAEJrDP.jpg"),
    RaidBoss("Lvl 50 Tiamat Omega", 50, EN, "https://pbs.twimg.com/media/CfqXQA3UMAEMV7O.jpg"),
    RaidBoss("Lvl 50 Tiamat", 50, EN, "https://pbs.twimg.com/media/CfqWy2tUUAAr9yu.jpg"),
    RaidBoss("Lvl 50 Veselago", 50, EN, "https://pbs.twimg.com/media/Crtpu8RVMAALBKk.jpg"),
    RaidBoss("Lvl 50 Yggdrasil", 50, EN, "https://pbs.twimg.com/media/CfqXDAQVIAAixiA.jpg"),
    RaidBoss("Lvl 60 Leviathan Omega", 60, EN, "https://pbs.twimg.com/media/CfqXTAQUAAAu3ox.jpg"),
    RaidBoss("Lvl 60 Yggdrasil Omega", 60, EN, "https://pbs.twimg.com/media/CfuZgxLUkAArdGe.jpg"),
    RaidBoss("Lvl 70 Colossus Omega", 70, EN, "https://pbs.twimg.com/media/CfqXRjsUAAAwXTP.jpg"),
    RaidBoss("Lvl 75 Celeste Omega", 75, EN, "https://pbs.twimg.com/media/CfqXXVDUUAA0hAS.jpg"),
    RaidBoss("Lvl 75 Luminiera Omega", 75, EN, "https://pbs.twimg.com/media/CfqXWAhUsAAprzd.jpg"),
    RaidBoss("Lvl 100 Apollo", 100, EN, "https://pbs.twimg.com/media/CfqXI0lVAAAQgcj.jpg"),
    RaidBoss("Lvl 100 Athena", 100, EN, "https://pbs.twimg.com/media/Cg7oBQ_UYAEAIK7.jpg"),
    RaidBoss("Lvl 100 Baal", 100, EN, "https://pbs.twimg.com/media/CjLxhwmUoAEglVC.jpg"),
    RaidBoss("Lvl 100 Celeste Omega", 100, EN, "https://pbs.twimg.com/media/CfqZzDsUUAA5DEX.jpg"),
    RaidBoss("Lvl 100 Colossus Omega", 100, EN, "https://pbs.twimg.com/media/CfqZOt6VIAAniVV.jpg"),
    RaidBoss("Lvl 100 Dark Angel Olivia", 100, EN, "https://pbs.twimg.com/media/CfqXOjEUMAAXuK2.jpg"),
    RaidBoss("Lvl 100 Garuda", 100, EN, "https://pbs.twimg.com/media/CkVbjdpUYAAmnvb.jpg"),
    RaidBoss("Lvl 100 Grand Order", 100, EN, "https://pbs.twimg.com/media/CfqaAYfUUAQqgpv.jpg"),
    RaidBoss("Lvl 100 Grani", 100, EN, "https://pbs.twimg.com/media/CqwDYIbVMAE0VUR.jpg"),
    RaidBoss("Lvl 100 Leviathan Omega", 100, EN, "https://pbs.twimg.com/media/CfqZfExVIAA4YgF.jpg"),
    RaidBoss("Lvl 100 Lich", 100, EN, "https://pbs.twimg.com/media/CqwDaPAVYAQAYzq.jpg"),
    RaidBoss("Lvl 100 Luminiera Omega", 100, EN, "https://pbs.twimg.com/media/CfqZvtlVIAAgBeF.jpg"),
    RaidBoss("Lvl 100 Macula Marius", 100, EN, "https://pbs.twimg.com/media/CfqXAC1UIAAeGl-.jpg"),
    RaidBoss("Lvl 100 Medusa", 100, EN, "https://pbs.twimg.com/media/CfqXEh_UsAEb9dw.jpg"),
    RaidBoss("Lvl 100 Nezha", 100, EN, "https://pbs.twimg.com/media/CfqW0bzUMAAOJsU.jpg"),
    RaidBoss("Lvl 100 Odin", 100, EN, "https://pbs.twimg.com/media/CqwDWGjUIAEeJ4s.jpg"),
    RaidBoss("Lvl 100 Proto Bahamut", 100, EN, "https://pbs.twimg.com/media/CfqXYlBUAAQ1mtV.jpg"),
    RaidBoss("Lvl 100 Tiamat Omega Ayr", 100, EN, "https://pbs.twimg.com/media/CfqW2SWUEAAMr7S.jpg"),
    RaidBoss("Lvl 100 Twin Elements", 100, EN, "https://pbs.twimg.com/media/CfqXaAvUIAEUC8B.jpg"),
    RaidBoss("Lvl 100 Yggdrasil Omega", 100, EN, "https://pbs.twimg.com/media/Cfv1i6wUsAAZajc.jpg"),
    RaidBoss("Lvl 120 Apollo", 120, EN, "https://pbs.twimg.com/media/CfqZxihUEAEzcG-.jpg"),
    RaidBoss("Lvl 120 Dark Angel Olivia", 120, EN, "https://pbs.twimg.com/media/CfqZ3BwVIAEtIgy.jpg"),
    RaidBoss("Lvl 120 Macula Marius", 120, EN, "https://pbs.twimg.com/media/CfqZhE0UsAA_JqS.jpg"),
    RaidBoss("Lvl 120 Medusa", 120, EN, "https://pbs.twimg.com/media/CfqZlIcVIAAp8e_.jpg"),
    RaidBoss("Lvl 120 Nezha", 120, EN, "https://pbs.twimg.com/media/CfqW4BYUEAAeYSR.jpg"),
    RaidBoss("Lvl 120 Twin Elements", 120, EN, "https://pbs.twimg.com/media/CfqZQ_pUEAAQFI4.jpg"),
    RaidBoss("Lvl 150 Proto Bahamut", 150, EN, "https://pbs.twimg.com/media/CfqZ-YtVAAAt5qd.jpg")
  )
}

