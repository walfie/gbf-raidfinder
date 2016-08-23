package walfie.gbf.raidfinder.client

import walfie.gbf.raidfinder.protocol._
import walfie.gbf.raidfinder.protocol.RaidBossesResponse.RaidBoss
import com.thoughtworks.binding.Binding._

trait ResponseHandler {
  def handleResponse(response: Response): Unit
}

case class RaidBossColumn(bossName: BossName, raidTweets: Vars[RaidTweetResponse])

class DefaultResponseHandler extends ResponseHandler {
  val raidBossColumns: Vars[RaidBossColumn] = Vars.empty
  var raidTweets: Map[BossName, Vars[RaidTweetResponse]] = Map.empty

  def handleResponse(response: Response): Unit = response match {
    case r: RaidBossesResponse => r.raidBosses.foreach { rb =>
      println(rb.bossName)
    }

    case r: SubscriptionStatusResponse =>

    case r: RaidTweetResponse =>
      val tweetsOpt = raidTweets.get(r.bossName)
      val tweets = tweetsOpt.getOrElse(Vars.empty)
      r +=: tweets.get
      if (tweetsOpt.isEmpty) {
        raidBossColumns.get += RaidBossColumn(r.bossName, tweets)
        raidTweets = raidTweets.updated(r.bossName, tweets)
      }
  }
}

