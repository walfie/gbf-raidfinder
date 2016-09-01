package walfie.gbf.raidfinder.util

import monix.eval.Task
import monix.execution.Ack
import monix.execution.schedulers.TestScheduler
import monix.reactive.{Observable, Observer}
import org.scalatest._
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.Matchers._
import scala.concurrent.Future

class ObservableSpec extends FreeSpec with ScalaFutures {
  case class Item(id: Int)
  object ItemRepository {
    def getItems(count: Int, pageNum: Int): Future[Seq[Item]] =
      Future.successful {
        (0 until count).map(i => Item(pageNum * count + i))
      }
  }

  // This was added as [[ObservableUtil.fromAsyncStateAction]] before
  // [[Observable.fromAsyncStateAction]] existed in Monix. Keeping these
  // tests around because why not.
  "fromAsyncStateAction" - {
    implicit val scheduler = TestScheduler()

    "yield an observable" in {

      val itemsPerPage = 5

      val observable = Observable.fromAsyncStateAction { pageNum: Int =>
        val nextPage = pageNum + 1
        val itemsF = ItemRepository.getItems(itemsPerPage, pageNum)
        Task.fromFuture(itemsF).map(_ -> nextPage)
      }(0)

      val resultF = observable.take(3).toListL.runAsync
      scheduler.tick()

      resultF.futureValue shouldBe Seq(
        (0 to 4).map(Item.apply),
        (5 to 9).map(Item.apply),
        (10 to 14).map(Item.apply)
      )
    }

    "stop on error" in {
      implicit val scheduler = TestScheduler()

      // Create an observable counter that errors when it gets to 5
      val error = new RuntimeException("Oh no!")
      val observable = Observable
        .fromAsyncStateAction[Int, Int] { counter: Int =>
          Task.fromFuture {
            if (counter == 5) Future.failed(error)
            else Future.successful(counter -> (counter + 1))
          }
        }(0)

      val observer = new TestObserver[Int]

      observable.take(10).subscribe(observer)
      scheduler.tick()

      observer.received shouldBe (0 to 4)
    }
  }
}

