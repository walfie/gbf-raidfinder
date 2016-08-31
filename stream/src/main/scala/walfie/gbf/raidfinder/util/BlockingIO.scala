package walfie.gbf.raidfinder.util

import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.{Executors, ThreadFactory}
import scala.concurrent.{ExecutionContext, ExecutionContextExecutor, Future, Promise, blocking}
import scala.util.control.NonFatal
import monix.execution.Scheduler

// https://github.com/alexandru/scala-best-practices/blob/master/sections/4-concurrency-parallelism.md
object BlockingIO {
  private val ioThreadPool = Scheduler.io(name = "io-thread")

  def future[T](t: => T): Future[T] = {
    val p = Promise[T]()

    val runnable = new Runnable {
      def run() = try {
        p.success(blocking(t))
      } catch {
        case NonFatal(ex) => p.failure(ex)
      }
    }

    ioThreadPool.execute(runnable)

    p.future
  }
}

