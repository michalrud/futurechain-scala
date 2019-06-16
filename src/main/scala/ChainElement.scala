import scala.concurrent.Future

trait ChainElement[T] {
  private var alreadyStarted = false

  final def run(): Future[T] = {
    assert(!alreadyStarted, "Attempted to run an already started FutureChain")
    alreadyStarted = true
    _run()
  }

  def andThen[U](cbk: T => Future[U]): ChainElement[U] = {
    new NextChainElement[T, U, this.type](this, cbk)
  }

  protected def _run(): Future[T]
}
