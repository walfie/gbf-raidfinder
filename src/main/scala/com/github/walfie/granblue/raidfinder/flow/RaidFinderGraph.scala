package com.github.walfie.granblue.raidfinder.flow

import akka.actor._
import akka.NotUsed
import akka.stream.scaladsl._
import akka.stream.{ActorMaterializer, ClosedShape}
import com.github.walfie.granblue.raidfinder.domain._
import com.github.walfie.granblue.raidfinder.flow._
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext

object RaidFinderGraph {
  def create(
    raidInfoSource:      Source[Seq[RaidInfo], _],
    raidInfoCache:       Option[RaidInfoCache],
    raidTweetsPublisher: Sink[RaidTweet, _]
  ): RunnableGraph[NotUsed] =
    RunnableGraph.fromGraph(GraphDSL.create() { implicit builder =>
      import GraphDSL.Implicits._

      val broadcast = builder.add(Broadcast[Seq[RaidInfo]](2))

      val cacheSink = raidInfoCache.fold[Sink[Seq[RaidInfo], _]](
        Sink.ignore
      )(cache => Sink.foreach(cache.put))

      raidInfoSource ~> broadcast
      broadcast ~> cacheSink
      broadcast ~> Flow[Seq[RaidInfo]].mapConcat(_.toVector).map(_.tweet) ~>
        raidTweetsPublisher

      ClosedShape
    })

  def default(
    raidInfoCache: Option[RaidInfoCache]
  )(implicit system: ActorSystem, ec: ExecutionContext): RunnableGraph[NotUsed] = {
    val raidInfoSource: Source[Seq[RaidInfo], NotUsed] =
      TwitterSearch.defaultPaginatedSource
        .map(_.flatMap(StatusParser.parseStatus))

    val raidTweetsPublisher = RaidTweetsPublisher.fromSystem(system)

    create(raidInfoSource, raidInfoCache, raidTweetsPublisher)
  }
}

