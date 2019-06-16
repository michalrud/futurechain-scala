# FutureChain

[![Build Status](https://travis-ci.org/michalrud/futurechain-scala.svg?branch=master)](https://travis-ci.org/michalrud/futurechain-scala)

Allows you to chain your futures like promises in JavaScript.
In short, each element in the chain gets the result from the previous element, and automatically
waits for that element to become available. If previous element is asynchronous, then the current one
will be called only when the Future received from the previous element is Succeeded, or immediately,
if previous element was synchronous.

Each element should honestly return its result as a `Future` object, but doesn't have to worry about
previous elements returning Futures. It should be able to safely assume that if it's called, then
all previous Futures have been succesfully completed, or in some other way take care of.

At the end of the chain, after calling `run()`, user receives a `Future` object that will contain the result of
the last chain element, after all the chain completes.
If any element in the chain fails, then the resulting `Future` will be failed too. That means:
 - Any asynchronous element fails its future,
 - Any synchronous element throws an exception
I've decided that asynchronous element throwing an exception means that it wanted to throw that exception, so let it be.

My first Scala project, so by default assume that it's shitty, unreliable and pretty much a carbon copy of how I would
do this in other languages I know.

## Assumptions

1. There should be no need to do callback pyramids,
2. It should be pleasant to use,
3. Any error should break the chain by default,
4. Types should be checked to help catch issues early

## Usage example

```scala
import scala.concurrent.{Future, Await}
import scala.concurrent.duration.Duration.Inf
import scala.concurrent.ExecutionContext.Implicits.global

val futureResult = FutureChain.futureChain(_ => Future{1})
    .andThen((v: Int) => Future(v.toString))
    .andThenSync((v: String) => s"Value is $v")
    .run()

println(Await.result(futureResult, atMost = Inf))
```