import org.scalatest._

import scala.concurrent.Future

class FutureChainTest extends AsyncFlatSpec {

  class MyTestException extends Exception {}

  behavior of "One function FutureChain"

  it should "Return the immediate result of a first function" in {
    FutureChain.futureChain(_ => {
      Future {
        1
      }
    }).run().map({ result => assert(result == 1) })
  }

  it should "Return the delayed result of a first function" in {
    FutureChain.futureChain(_ => {
      Thread.sleep(10)
      Future {
        Thread.sleep(10)
        1
      }
    }).run().map({ result => assert(result == 1) })
  }

  it should "Not allow to be run multiple times" in {
    assertThrows[AssertionError] {
      val a = FutureChain.futureChain(_ => Future {
        1
      })
      a.run()
      a.run()
    }
  }

  it should "Propagate an exception" in {
    recoverToSucceededIf[MyTestException] {
      FutureChain.futureChain(_ => Future.failed(new MyTestException)).run()
    }
  }

  behavior of "Two functions FutureChain"

  it should "Return the immediate result of a second function that converts int to string" in {
    FutureChain.futureChain(_ => Future {
      1
    })
      .andThen(i => Future {
        i.toString
      })
      .run().map(result => assert(result == "1"))
  }

  it should "Not allow type mismatches" in {
    assertTypeError("FutureChain.futureChain(_ => Future{ 1 }).andThen((i: String) => Future{i})")
  }

  it should "Not allow to be run multiple times" in {
    assertThrows[AssertionError] {
      val a = FutureChain.futureChain(_ => Future {
        1
      }).andThen(v => Future {
        v
      })
      a.run()
      a.run()
    }
  }

  it should "Propagate an exception from the first function" in {
    recoverToSucceededIf[MyTestException] {
      FutureChain.futureChain(_ => Future[Int] {
        throw new MyTestException
      })
        .andThen(a => Future {
          a
        }).run()
    }
  }

  it should "Propagate an exception from the second function" in {
    recoverToSucceededIf[MyTestException] {
      FutureChain.futureChain(_ => Future {
        1
      })
        .andThen(_ => Future {
          throw new MyTestException
        }).run()
    }
  }

  behavior of "Three functions FutureChain"

  it should "Return the immediate result of a third function that converts int to float and then to string" in {
    FutureChain.futureChain(_ => Future {
      1
    })
      .andThen((i: Int) => Future {
        i.toFloat
      })
      .andThen((i: Float) => Future {
        i.toString
      })
      .run().map({ result => assert(result === "1.0") })
  }

  it should "Return operations in order" in {
    FutureChain.futureChain(_ => Future {
      Thread.sleep(10); 4
    })
      .andThen(i => Future {
        Thread.sleep(10); i + 2
      })
      .andThen(i => Future {
        i * 2
      })
      .run().map({ result => assert(result == (4 + 2) * 2) })
  }

  it should "Not allow to be run multiple times" in {
    assertThrows[AssertionError] {
      val a = FutureChain.futureChain(_ => Future {
        1
      }).andThen(v => Future {
        v
      }).andThen(v => Future {
        v
      })
      a.run()
      a.run()
    }
  }

  it should "Propagate an exception from the first function" in {
    recoverToSucceededIf[MyTestException] {
      FutureChain.futureChain(_ => Future[Int] {
        throw new MyTestException
      })
        .andThen(a => Future {
          a
        })
        .andThen(a => Future {
          a
        }).run()
    }
  }

  it should "Propagate an exception from the second function" in {
    recoverToSucceededIf[MyTestException] {
      FutureChain.futureChain(_ => Future {
        1
      })
        .andThen(_ => Future[Int] {
          throw new MyTestException
        })
        .andThen(a => Future {
          a
        }).run()
    }
  }

  it should "Propagate an exception from the third function" in {
    recoverToSucceededIf[MyTestException] {
      FutureChain.futureChain(_ => Future {
        1
      })
        .andThen(a => Future {
          a
        })
        .andThen(_ => Future {
          throw new MyTestException
        }).run()
    }
  }

  it should "Not allow type mismatches" in {
    assertTypeError("FutureChain.futureChain(_ => Future{ 1 }).andThen(i => Future{i}).andThen((i: String) => Future{i})")
    assertTypeError("FutureChain.futureChain(_ => Future{ 1 }).andThen((i: String) => Future{i}).andThen(i => Future{i})")
  }
}