# FutureChain

[![Build Status](https://travis-ci.org/michalrud/futurechain-scala.svg?branch=master)](https://travis-ci.org/michalrud/futurechain-scala)

Allows you to chain your futures like promises in JavaScript.

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