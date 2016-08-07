package com.github.walfie.granblue.raidtracker

import twitter4j._
import twitter4j.conf.ConfigurationBuilder

trait RaidParser {
  def parseText(text: String): Option[Raid]
}

trait RaidParserImpl extends RaidParser {
  protected val RaidRegex = "(.*)参加者募集！参戦ID：([0-9A-F]{8})\n(.+)\n?.*".r

  def parseText(text: String): Option[Raid] = parseTextPF(text)

  private val parseTextPF: String => Option[Raid] = ({
    case RaidRegex(extraText, raidId, bossName) =>
      Raid(bossName, raidId, extraText)
  }: PartialFunction[String, Raid]).lift
}

