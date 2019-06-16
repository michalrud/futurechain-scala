import scala.concurrent.Future

trait ChainElement[T] {
  private var alreadyStarted = false

  /**
    * Runs the future chain.
    *
    * @return A Future returned by the last chain element.
    */
  final def run(): Future[T] = {
    assert(!alreadyStarted, "Attempted to run an already started FutureChain")
    alreadyStarted = true
    _run()
  }

  /**
    * Adds a new element to the future chain.
    *
    * @param cbk Element to be added to the chain. Should return a Future object.
    * @tparam U Type returned by the object to be added, as enclosed in Future.
    * @return Last element in the future chain.
    */
  def andThen[U](cbk: T => Future[U]): ChainElement[U] = {
    new AsynchronousChainElement[T, U, this.type](this, cbk)
  }

  /**
    * Adds a new synchronous element to the future chain.
    *
    * @param cbk Element to be added to the chain. Can return anything, but next element in the chain won't wait for it.
    * @tparam U Type returned by the element to be added.
    * @return Last element in the future chain.
    */
  def andThenSync[U](cbk: T => U): ChainElement[U] = {
    new SynchronousChainElement[T, U, this.type](this, cbk)
  }

  /**
    * To be reimplemented by the chain element classes.
    *
    * @return Result of the chain element execution in the Future.
    */
  protected def _run(): Future[T]
}
