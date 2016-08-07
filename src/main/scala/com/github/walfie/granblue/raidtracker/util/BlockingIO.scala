package com.github.walfie.granblue.raidtracker.util

import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.{Executors, ThreadFactory}
import scala.concurrent.{ExecutionContext, ExecutionContextExecutor, Future, Promise, blocking}
import scala.util.control.NonFatal

// https://github.com/alexandru/scala-best-practices/blob/master/sections/4-concurrency-parallelism.md
object BlockingIO {
  private implicit val ioThreadPool: ExecutionContextExecutor = {
    val threadFactory = new ThreadFactory {
      private val counter = new AtomicLong(0L)

      def newThread(r: Runnable): Thread = {
        val th = new Thread(r)
        th.setName("eon-io-thread-" + counter.getAndIncrement.toString)
        th.setDaemon(true)
        th
      }
    }

    val threadPool = Executors.newCachedThreadPool(threadFactory)
    ExecutionContext.fromExecutorService(threadPool)
  }

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

