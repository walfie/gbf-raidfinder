package walfie.gbf.raidfinder.server

import akka.agent._
import scala.concurrent.ExecutionContext

trait MetricsCollector {
  def webSocketConnected(): Unit
  def webSocketDisconnected(): Unit

  def getActiveWebSocketCount(): Int
}

class MetricsCollectorImpl(implicit ec: ExecutionContext) extends MetricsCollector {
  private val webSocketConnectionCountAgent = Agent[Int](0)

  def webSocketConnected(): Unit = webSocketConnectionCountAgent.send(_ + 1)
  def webSocketDisconnected(): Unit = webSocketConnectionCountAgent.send(_ - 1)

  def getActiveWebSocketCount(): Int = webSocketConnectionCountAgent.get
}

