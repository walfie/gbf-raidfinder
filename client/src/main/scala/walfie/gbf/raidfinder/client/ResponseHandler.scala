package walfie.gbf.raidfinder.client

import walfie.gbf.raidfinder.protocol._
import com.thoughtworks.binding.Binding._

trait ResponseHandler {
  def handleResponse(response: Response): Unit

  // TODO: Move these methods elsewhere
  def addColumn(bossName: String): Unit
  def removeColumn(bossName: String): Unit

  // TODO: These too
  def raidBossColumns: Vars[RaidBossColumn]
  def allBosses: Vars[RaidBoss]
}

case class RaidBossColumn(
  raidBoss:   Var[RaidBoss],
  raidTweets: Vars[RaidTweetResponse]
)

object RaidBossColumn {
  def empty(bossName: BossName) = {
    val raidBoss = RaidBoss(bossName = bossName)
    RaidBossColumn(raidBoss = Var(raidBoss), raidTweets = Vars.empty)
  }
}

class DefaultResponseHandler extends ResponseHandler {
  val raidBossColumns: Vars[RaidBossColumn] = Vars.empty
  val allBosses: Vars[RaidBoss] = Vars.empty
  private var raidBossColumnsMap: Map[BossName, RaidBossColumn] = Map.empty

  def addColumn(bossName: String): Unit = {
    val columnData = raidBossColumnsMap.getOrElse(bossName, RaidBossColumn.empty(bossName))
    raidBossColumnsMap = raidBossColumnsMap.updated(bossName, columnData)
    raidBossColumns.get += columnData
  }

  def removeColumn(bossName: String): Unit = {
    raidBossColumnsMap.get(bossName).foreach(_.raidTweets.get.clear)
    val index = raidBossColumns.get.indexWhere(_.raidBoss.get.bossName == bossName)
    if (index >= 0) raidBossColumns.get.remove(index)
  }

  // TODO: Don't change Vars in this class, handle it in some centralized client
  def handleResponse(response: Response): Unit = response match {
    case r: RaidBossesResponse =>
      allBosses.get.clear()
      allBosses.get ++= r.raidBosses.sortBy(_.bossName)
      r.raidBosses.foreach { raidBoss =>
        val bossName = raidBoss.bossName
        raidBossColumnsMap.get(bossName) match {
          // New raid boss that we don't yet know about
          case None =>
            val columnData = RaidBossColumn(raidBoss = Var(raidBoss), raidTweets = Vars.empty)
            raidBossColumnsMap = raidBossColumnsMap.updated(bossName, columnData)

          // Update existing raid boss data
          case Some(columnData) => columnData.raidBoss := raidBoss
        }
      }

    case r: SubscriptionStatusResponse =>
    // Ignore. Also TODO: Figure out why this doesn't come back consistently

    case r: RaidTweetResponse =>
      // TODO: Check if following
      raidBossColumnsMap.get(r.bossName).foreach(column => r +=: column.raidTweets.get)
  }
}

