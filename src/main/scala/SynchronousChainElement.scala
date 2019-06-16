import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Future, Promise}
import scala.util.{Failure, Success}

class SynchronousChainElement[T, U, PreviousElement <: ChainElement[T]](previousChainElement: PreviousElement,
                                                                        cbk: T => U)
  extends ChainElement[U] {
  private val previousElement = previousChainElement

  override def _run(): Future[U] = {
    val completionPromise = Promise[U]
    previousElement.run().onComplete {
      case Success(value) =>
        try completionPromise.success(cbk(value))
        catch {
          case e: Throwable => completionPromise.failure(e)
        }
      case Failure(e) => completionPromise.failure(e)
    }
    completionPromise.future
  }
}
