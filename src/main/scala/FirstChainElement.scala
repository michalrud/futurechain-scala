import scala.concurrent.Future

class FirstChainElement[U](val cbk: Unit => Future[U]) extends ChainElement[U] {
  override def _run(): Future[U] = {
    cbk()
  }
}
