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

  it should "Be able to retry the function until success" in {
    var counter = 0
    val expectedCounter = 1000
    FutureChain.futureChain(_ => Future {
      1
    })
      .andThenRetryUntilSuccess(_ => Future {
        counter += 1
        if (counter < expectedCounter) throw new MyTestException
        counter
      }).run().map(result => assert(result == expectedCounter))
  }

  it should "Be able to retry the function until success if number of max retries is not reached" in {
    var counter = 0
    val expectedCounter = 1000
    FutureChain.futureChain(_ => Future {
      1
    })
      .andThenRetryUntilSuccess(_ => Future {
        counter += 1
        if (counter < expectedCounter) throw new MyTestException
        counter
      }, Some(1000)).run().map(result => assert(result == expectedCounter))
  }

  it should "Be able to give up retrying the function if it doesn't suceeds in specified number of tries" in {
    var counter = 0
    val expectedCounter = 1000
    recoverToSucceededIf[MyTestException] {
      FutureChain.futureChain(_ => Future {
        1
      })
        .andThenRetryUntilSuccess(_ => Future {
          counter += 1
          if (counter < expectedCounter) throw new MyTestException
          counter
        }, Some(999)).run()
    }
  }

  it should "Fail on first try if maximum number of tries is 0" in {
    var counter = 0
    val expectedCounter = 2
    recoverToSucceededIf[MyTestException] {
      FutureChain.futureChain(_ => Future {
        1
      })
        .andThenRetryUntilSuccess(_ => Future {
          counter += 1
          if (counter < expectedCounter) throw new MyTestException
          counter
        }, Some(0)).run()
    }
  }

  it should "Fail on first try if maximum number of tries is negative" in {
    var counter = 0
    val expectedCounter = 2
    recoverToSucceededIf[MyTestException] {
      FutureChain.futureChain(_ => Future {
        1
      })
        .andThenRetryUntilSuccess(_ => Future {
          counter += 1
          if (counter < expectedCounter) throw new MyTestException
          counter
        }, Some(-1)).run()
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

  it should "Allow to be mixed with synchronous functions" in {
    FutureChain.futureChain(_ => Future {
      1
    })
      .andThenSync((i: Int) => i.toFloat)
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