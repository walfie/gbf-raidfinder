package walfie.gbf.raidfinder

import monix.execution.Scheduler
import monix.reactive._
import monix.reactive.subjects.ConcurrentSubject
import twitter4j._

// TODO: Write tests
trait TwitterStreamer {
  def observable: Observable[Status]
}

object TwitterStreamer {
  val DefaultFilterTerms = Seq("参加者募集！", ":参戦ID", "I need backup!", ":Battle ID")

  def apply(
    twitterStream: twitter4j.TwitterStream = TwitterStreamFactory.getSingleton,
    filterTerms:   Seq[String]             = DefaultFilterTerms
  )(implicit scheduler: Scheduler): Twitter4jStreamer =
    new Twitter4jStreamer(twitterStream, filterTerms)
}

class Twitter4jStreamer(
  twitterStream: twitter4j.TwitterStream,
  filterTerms:   Seq[String]
)(implicit scheduler: Scheduler) extends TwitterStreamer {
  private val subject = ConcurrentSubject.publish[Status]
  val observable: Observable[Status] = subject

  private val listener = new StatusAdapter() {
    override def onStatus(status: Status): Unit = subject.onNext(status)
    override def onException(e: Exception): Unit = println(e) // TODO: Better error handling
  }

  twitterStream.addListener(listener)
  twitterStream.filter(new FilterQuery(filterTerms: _*))
}

