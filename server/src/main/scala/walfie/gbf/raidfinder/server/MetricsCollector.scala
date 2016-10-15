package walfie.gbf.raidfinder.server

import akka.agent._
import monix.execution.Scheduler
import monix.reactive.subjects.ConcurrentSubject
import scala.concurrent.duration.FiniteDuration

trait MetricsCollector {
  def incrementKeepAliveCount(): Unit
  def getActiveUsers(): Int
}

class MetricsCollectorImpl(
  keepAliveInterval: FiniteDuration
)(implicit scheduler: Scheduler) extends MetricsCollector {
  private val keepAliveSubject = ConcurrentSubject.publish[Unit]
  private val keepAliveCountAgent = Agent[Int](0)

  keepAliveSubject
    .bufferTimed(keepAliveInterval)
    .map(_.length)
    .foreach(keepAliveCountAgent.send)

  def incrementKeepAliveCount(): Unit = keepAliveSubject.onNext(())

  def getActiveUsers(): Int = keepAliveCountAgent.get
}

