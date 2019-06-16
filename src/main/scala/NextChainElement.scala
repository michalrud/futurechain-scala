import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Future, Promise}
import scala.util.{Failure, Success}

class NextChainElement[T, U, PreviousElement <: ChainElement[T]](previousChainElement: PreviousElement,
                                                                 cbk: T => Future[U])
  extends ChainElement[U] {
  private val previousElement = previousChainElement

  override def _run(): Future[U] = {
    val completionPromise = Promise[U]
    previousElement.run().onComplete {
      case Success(value) => completionPromise.completeWith(cbk(value))
      case Failure(e) => completionPromise.failure(e)
    }
    completionPromise.future
  }
}
