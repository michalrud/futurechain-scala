import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Future, Promise}
import scala.util.{Failure, Success}

class RetryingChainElement[T, U, PreviousElement <: ChainElement[T]](previousChainElement: PreviousElement,
                                                                     cbk: T => Future[U],
                                                                     maxTries: Option[Int])
  extends ChainElement[U] {
  private val previousElement = previousChainElement

  override def _run(): Future[U] = {
    val completionPromise = Promise[U]
    previousElement.run().onComplete {
      case Success(value) => runCallback(value, completionPromise, maxTries)
      case Failure(e) => completionPromise.failure(e)
    }
    completionPromise.future
  }

  private def runCallback(value: T, completionPromise: Promise[U], leftTries: Option[Int]): Unit = {
    cbk(value).onComplete {
      case Success(result) => completionPromise.success(result)
      case Failure(e) => leftTries match {
        case Some(v) if v <= 1 => completionPromise.failure(e)
        case Some(howManyTriesLeft) => runCallback(value, completionPromise, Some(howManyTriesLeft - 1))
        case None => runCallback(value, completionPromise, None)
      }
    }
  }
}
