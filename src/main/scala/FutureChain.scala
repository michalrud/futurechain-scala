import scala.concurrent.Future

object FutureChain {
  def futureChain[U](cbk: Unit => Future[U]): ChainElement[U] = {
    new FirstChainElement[U](cbk)
  }
}