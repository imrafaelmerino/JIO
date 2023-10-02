<img src="logo/package_twitter_itsywb76/black/full/coverphoto/black_logo_white_background.png" alt="logo"/>

- [Introduction](#introduction)
- [jio-exp](#core)
    - [Constructors](#constructors)
    - [Expressions](#exp)
    - [Lambdas λ](#λs)
    - [Managing resources](#resources)
    - [Being reactive](#reactive)
    - [Clocks](#clocks)
    - [JFR integration](#jfrcore)
    - [Installation](#installation)
- [jio-http](#http)
    - [Http Client](#httpclient)
    - [Oauth Http Client](#httpclient)
    - [Http Server](#httpserver)
    - [JFR integration](#jfrhttp)
    - [Installation](#installation)
- [jio-test](#test)
    - [stubs](#stubs)
        - [IO stubs](#iostubs)
        - [HttpServer stubs](#httpserverstubs)
    - [Property-Based-Testing](#pbt)
    - [Junit integration](#junit)
      -[JFR](#jfrjunit)
    - [Installation](#installation)
- [jio-mongodb](#mongodb)
    - [MongoDB Client](#mongodbclient)
    - [JFR integration](#jfr integration)
    - [Installation](#installation)
- [jio-console](#console)
    - [Installation](#installation)
- [jio-chatgpt](#chatgpt)
    - [Installation](#installation)

# <a name="introduction"><a/> Introduction

Functional Programming is all about working with pure functions and
values. That's all. **However, where FP especially shines is dealing
with effects**.

But, what is an effect?

An effect is something you can't call twice unless you intended to:

```code 

Instant a = Instant.now();

Instant b = Instant.now();

```

Because _now()_ returns a different value each time it's called, 'a' and 'b' are different instants. Doing
the following refactoring would change the meaning of the program completely (and still your favourite IDE suggests you
to do it at times!):

```code      
import java.time.Instant;

Instant now = Instant.now();

Instant a = now;

Instant b = now;

```

Here's when laziness comes into play. Since Java 8, we have suppliers. They
are indispensable to do FP in Java. The following piece of code is equivalent to
the previous where 'a' and 'b' where two different instants:

```code  

Supplier<Instant> now = () -> Instant.now();

Instant a = now.get();

Instant b = now.get();

```

This property called **referential transparency** is fundamental to
create and compose expressions.

We can learn a lot of things from how pure functional languages like
Haskell use laziness and the **IO monad** to turn side effects into
functional effects. Functional effects, not like side effects, are
pure composable values.

What can you expect from JIO:

 - Simple and powerful API
 - Errors are first class citizens
 - Simple and powerful testing tools (jio-test)
 - Easy to extend and get benefit from all the above. Examples are jio-http, jio-mongodb or jio-chatgpt. And you can create your owns integrations!
 - I don't fall into the logging-library war. This is something that sucks in Java. I just use JFR!
 - Almost zero dependencies (just plain Java!)
 - JIO doesn't transliterate any functional API from other languages.
   Any standard Java programmer will find JIO quite easy and familiar.




# <a name="core"><a/> jio-exp

Let's model a funcional effect in Java! 

```java  

import java.util.function.Supplier;
import java.util.concurrent.CompletableFuture;

public abstract class IO<O> implements Supplier<CompletableFuture<O>> {
}

```
It's just a lazy computation (a supplier in Java) that returns a future!
As we mentioned before, we need to embrace lazyness to get befefit from composition
and create expressions. This how we deal with complexity in the FP world. On the
other hand, **a _CompletableFuture_ represents an asynchronous effect**. We don't want to
block any thread because of the latency of a computation. On the other hand,
**it can represent both a successful or a failed computation, which is key to not
throw exceptions whenever an error happens**. Errors are first class citizens in
JIO. They are just values.

From now on we'll talk indistinctly about effects, functional effects, computations and values.
We'll be referring to an IO type.

## <a name="constructors"><a/> Constructors

Now that we got the ball rolling, let's learn how to create IO effects

```code  
// from a constant

IO<String> effect = IO.succeed("hi");

// from an exception 

IO<String> effect1 = IO.fail(new RuntimeException("something went wrong :("));

// from a lazy computation or a supplier

Suplier<Long> computation;
IO<Long> effect2 = IO.lazy(computation);

// from a lazy computation than can fail or a callable

Callable<Long> callable;
IO<Long> effect3 = IO.task(callable);

// from a CompletableFuture
CompletableFuture<JsObj> get(String id){...}

IO<JsObj> effect4 = IO.effect( () -> get(id) );

``` 

In the above examples, the values **will be computed on the caller thread**.
Sometimes we need to control on what thread to perform a computation,
especially when it's blocking.
Whe can specify an executor, or to make use of the **ForkJoin** pool, which is
not a problem since **JIO uses internally
the [ManagedBlocker](https://docs.oracle.com/javase/7/docs/api/java/util/concurrent/ForkJoinPool.ManagedBlocker.html)**
interface, or you can even get benefit from the Loom project and use fibers!

```java 

// from a lazy computation that has to be executed on a specific pool

Suplier<Long> computation;
        IO<Long> effect=IO.laxy(computation,
        Executors.newCachedThreadPool()
        );

//from a blocking operation that has to be executed on the ForkJoin Pool
//by the ManagedBlocker

        Supplier<JsObj> blockingTask;
        IO<JsObj> effect1=IO.managedLazy(blockingTask);

//using fibers!

        IO<JsObj> effect2=IO.lazy(blockingTask,
        Executors.newVirtualThreadPerTaskExecutor()
        );

``` 

You can race an arbitrary number of effects and return the fastest:

```java   

IO<A> fastest=IO.race(effect,
        effect1,
        effect2
        );

```

## <a name="exp"><a/> Expressions

**Using expressions and function composition is how we deal with complexity
in Functional Programming**. Let's go over the essential expressions in JIO:

- **IfElseExp**. If the predicate is evaluated to true, the supplier associated
  to the consequence is evaluated and the computed IO is returned.
  The same way if it's evaluated to false, the supplier associated to the alternative
  is evaluated and woudl be the result of the expression.

```java   

import jio.IfElseExp;

IO<O> exp=IfElseExp.<O>predicate(IO<Boolean> condition)
        .consequence(Supplier<IO<O>>consequence)
        .alternative(Supplier<IO<O>>alternative);


```

Bear in mind that the alternative and the consequence are lazy computations of IO effects.

- **SwitchExp**. The switch construct implements multiple pattern-value branches.
  It evaluates an effect or value of type I and allows multiple clauses based on
  evaluating that value.

```java   

// matches a value of type I

IO<O> exp=
        SwitchExp<O>.eval(I value)
        .match(I pattern1,Supplier<IO<O>>value1,
        I pattern2,Supplier<IO<O>>value2,
        I pattern3,Supplier<IO<O>>value3,
        Supplier<IO<O>>otherwise
        );

// matches an effect of type I

        IO<O> exp=
        SwitchExp<I, O>.eval(IO<I> value)
        .match(I pattern1,Supplier<IO<O>>value1,
        I pattern2,Supplier<IO<O>>value2,
        I pattern3,Supplier<IO<O>>value3,,
        Supplier<IO<O>>defaultValue
        );


// For example, the following expression reduces to "Wednesday"

        IO<O> exp=
        SwitchExp<String>.eval(3)
        .match(1,()->IO.succedd("Monday"),
        2,()->IO.succedd("Tuesday"),
        3,()->IO.succedd("Wednesday"),
        4,()->IO.succedd("Thursday"),
        5,()->IO.succedd("Friday"),
        ()->IO.succedd("weekend")
        );
```

The same as before but using lists instead of constants as patterns.

```java   

IO<O> exp=SwitchExp<I, O>.eval(I value)
        .match(List<I> pattern1,Supplier<IO<O>>value1,
        List<I> pattern2,Supplier<IO<O>>value2,
        List<I> pattern3,Supplier<IO<O>>value3,
        Supplier<IO<O>>defaultValue
        );

// For example, the following expression reduces to "third week"
        IO<O> exp=
        SwitchExp<Integer, String>.eval(20)
        .match(List.of(1,2,3,4,5,6,7),()->IO.succedd("first week"),
        List.of(8,9,10,11,12,13,14),()->IO.succedd("second week"),
        List.of(15,16,17,18,19,20,10),()->IO.succedd("third week"),
        List.of(21,12,23,24,25,26,27),()->IO.succedd("forth week"),
        ()->IO.fromValue("last days of the month")
        );
```

Last but not least, you can use predicates as patterns instead of values or list of values:

```java   

IO<O> exp=
        SwitchExp<I, O>.eval(IO<I> value)
        .match(Predicate<I> pattern1,Supplier<IO<O>>value1,
        Predicate<I> pattern2,Supplier<IO<O>>value2,
        Predicate<I> pattern3,Supplier<IO<O>>value3,
        Supplier<IO<O>>defaultValue
        );

// For example, the following expression reduces to the default value

        IO<O> exp=
        SwitchExp<Integer, String>.eval(IO.succeed(20))
        .match(i->i< 5,()->IO.succeed("lower than five"),
        i->i< 10,()->IO.succeed("lower than ten"),
        i->i< 20,()->IO.succeed("lower than twenty"),
        ()->IO.succeed("greater or equal to twenty")
        );
```

- **CondExp**. It's a set of branches, and a default value. Each branch consists of an
  effect that computes a boolean (the condition) and its associated effect. The effect is
  computed and the expression reduced to its value if its condition is the first one in the
  list to be true. This means the order you place branches matters.
  If no condition is true, it computes the default effect, which is the last clause.
  You can compute all the conditions values either in parallel or sequentially.

```java   

IO<O> exp=CondExp.<O>seq(IO<Boolean> cond1,Supplier<IO<O>>value1,
        IO<Boolean> cond2,Supplier<IO<O>>value2,
        IO<Boolean> cond3,Supplier<IO<O>>value3,
        Supplier<IO<O>>defaultValue
        );


        IO<O> exp=CondExp.<O>par(IO<Boolean> cond1,Supplier<IO<O>>value1,
        IO<Boolean> cond2,Supplier<IO<O>>value2,
        IO<Boolean> cond3,Supplier<IO<O>>value3,
        Supplier<IO<O>>defaultValue
        );

``` 

- **AllExp** and **AnyExp**. They are just idiomatic names for the boolean expressions And and Or.
  You can compute all the boolean effects either in parallel or sequentially.

```java  

IO<Boolean> all=AllExp.par(IO<Boolean> cond1,IO<Boolean> cond2,....);
        IO<Boolean> all=AllExp.seq(IO<Boolean> cond1,IO<Boolean> cond2,....);

        IO<Boolean> any=AnyExp.par(IO<Boolean> cond1,IO<Boolean> cond2,...);
        IO<Boolean> any=AnyExp.seq(IO<Boolean> cond1,IO<Boolean> cond2,...);

```  

- **PairExp**. A pair is a tuple of two elements. Each element can be computed
  either in parallel or sequentially.

```java   

IO<Pair<A, B> pair=PairExp.par(IO<A> val1,IO<B> val2);

        IO<Pair<A, B> pair=PairExp.seq(IO<A> val1,IO<B> val2);

```

- **TripleExp**. A triple is a tuple of three elements. Each element can be
  computed either in parallel or sequentially.

```java   

IO<Triple<A, B, C> triple=TripleExp.par(IO<A> val1,IO<B> val2,IO<C> val3);

        IO<Triple<A, B, C> triple=TripleExp.seq(IO<A> val1,IO<B> val2,IO<C> val3);

```

- **JsObjExp** and **JsArrayExp**.

_JsObjExp_ and _JsArrayExp_ are data structures that look like raw Json.
You can compute all the values either in parallel or sequentially. You can mix
all the expressions we've seen so far and nest them, going as deep as
necessary, like in the following example:

```java   

IfElseExp<JsStr> a=IfElseExp.<JsStr>predicate(IO<Boolean> condition)
        .consequence(IO<JsStr> consequence)
        .alternative(IO<JsStr> alternative);

        JsArrayExp b=
        JsArrayExp.seq(SwitchExp<Integer, JsValue>.match(n)
        .patthers(1,Supplier<IO<JsValue>>value1,
        2,Supplier<IO<JsValue>>value2,
        Supplier<IO<JsValue>>defaultValue
        ),
        CondExp.par(IO<Boolean> cond1,Supplier<IO<JsValue>>value1,
        IO<Boolean> cond2,Supplier<IO<JsValue>>value3,
        Supplier<IO<JsValue>>defaultValue
        )
        );

        JsObjExp c=
        JsObjExp.par("d",AnyExp.seq(IO<Boolean> cond1,IO<Boolean> cond2)
        .map(JsBool::of),
        "e",AllExp.par(IO<Boolean> cond1,IO<Boolean> cond2)
        .map(JsBool::of),
        "f",JsArrayExp.par(IO<JsValue> value1,IO<JsValue> value2)
        )

        JsObjExp exp=JsObjExp.par("a",a,
        "b",b,
        "c",c
        );

        CompletableFuture<JsObj> json=exp.get();                          
```

It's important to notice that any value of the above expressions can be
computed by a different thread,sequentially or in parallel, you name it.
The whole expression remains the same.

## <a name="λs"><a/> Lambdas λ

A Lambda is just a function that takes an input and produces an effect:

```java  

interface IO<O> extends Supplier<CompletableFuture<O>> {
}

interface Lambda<I, O> extends Function<I, IO<O>> {
}

interface BiLambda<A, B, O> extends BiFunction<A, B, IO<O>> {
}

```

**We have effects, a set of powerful expressions to combine them
and lambdas. It's all we need to handle complexity. We also need
a way of deal with errors as they were data.**
The typical reactive function retry, recover and fallback, to name
just a few, will be introduced in the following section.

## <a name="reactive"><a/> Being reactive

Find below some of the most critical operations defined in the _IO_ interface that will
help us make our code more resilient:

```java  

import jio.RetryPolicy;

public interface IO<O> extends Supplier<Future<O>> {

    IO<O> retry(Predicate<Throwable> predicate,
                RetryPolicy policy
               );

    IO<O> repeat(Predicate<O> predicate,
                 RetryPolicy policy
                );

    <Q> IO<Q> map(Function<O, Q> fn);

    <Q> IO<Q> then(Lambda<O, Q> fn);

    IO<O> recover(Function<Throwable, O> fn);

    IO<O> recoverWith(Lambda<Throwable, O> fn);

    IO<O> fallbackTo(Lambda<Throwable, O> fn);

    IO<O> peek(Consumer<O> successHandler,
               Consumer<Throwable> failureHandler
              );

    IO<O> timeout(int time,
                  TimeUnit unit
                 );

``` 

**recoverWith**:  it switches to an alternative lambda when a failure happens.

**fallbackTo**: It's like recoverWith, but if the second lambda fails too, it
returns the first one error.

**recover**: returns a constant if the computation fails.

**retry**: retries the computation if an error happens. You can define a predicate
to retry only the specified errors.
Retry policies are created in a very declarative and composable way, for example:

```java   

import static jio.RetryPolicies.*

Duration oneHundredMillis=Duration.ofMillis(100);
        Duration oneSec=Duration.ofSeconds(1);

// up to five retries waiting 100 ms 
        constantDelay(oneHundredMillis).append(limitRetries(5))

//during 3 seconds up to 10 times     
        limitRetries(10).limitRetriesByCumulativeDelay(Duration.ofSeconds(3))

//5 times without delay and then, if it keeps failing, an incremental delay from 100 ms up to 1 second
        limiteRetries(5).followedBy(incrementalDelay(oneHundredMillis).capDelay(oneSec))

```

There are very interesting policies implemented based
on [this article](https://aws.amazon.com/blogs/architecture/exponential-backoff-and-jitter/):
exponential backoff, full jitter, equal jitter, decorrelated jitter etc

**repeat**: When you get a not expected value (a failure) and want to repeat
the computation. A predicate is specified to catch the failures. You can define
any imaginable policy as well. Imagine you make a http request, and you get a 500.
That's not an error, it's a server failure. You can repeat the request according
to a policy.

Like in the CompletableFuture API, you can also provide an _Executor_ using the methods with the suffix _on_,
like thenOn, recoverWithOn etc

## <a name="clocks"><a/> Clocks

Every time you write _new Date()_ in the body of a method or function, you are creating a bug. Remember
that in FP, all the inputs must appear in the signature of a function. Dealing with time, it's even more
important.

A clock is just a supplier that returns a number.

```java  

public sealed interface Clock extends Supplier<Long> permits Monotonic, MyClock, RealTime {

```

There are three types of clocks:

- Realtime. Is affected by NTP, and can move forwards and backwards. Implemented with _System.currentTimeMillis()_
- Monotonic. Clock useful for time measurements and comparison. It's not affected by NTP. Implemented with
  _System.nanoTime()_
- Custom. Implement your own clock to test your programs in the future and the past.

## <a name="mocks"><a/> Mocks

Testing functional programs is extremely easy. There are two mocks in JIO you'll be
able to use to test your code. You don't need anything else. Forget about Mockito and
all that stuff.

```java   

public final class IOMock<O> implements Supplier<IO<O>> {

    public static <O> IOMock<O> fromValue(IntFunction<O> value)

    public static <O> IOMock<O> fromValue(IntFunction<O> value,
                                          IntFunction<Duration> delay
                                         );

    public static <O> IOMock<O> failThenSucceed(IntFunction<Throwable> failure,
                                                O value
                                               );

    public static <O> IOMock<O> failThenSucceed(IntFunction<Throwable> failure,
                                                IntFunction<Duration> delay,
                                                O value
                                               );

}


```

You can create IO mocks that succeed or fail. Both accept two functions that take
an integer as an input (the call counter) and return a value and a delay.
For example:

```java   

IOMock<String> valMock=IOMock.fromValue(ncall->ncall< 3?"a":"b")
        IO<String> ioMock=valMock.get();

        Assertions.assertEquals("a",ioMock.get().block());
        Assertions.assertEquals("a",ioMock.get().block());

// it's been called twice, "b" will be always returned from now on
        Assertions.assertEquals("b",ioMock.get().block());
        Assertions.assertEquals("b",ioMock.get().block());

//You can specify after how long a value is returned,
//for example the first time is returned after one second,
//following times after no time

        IOMock<String> valMock=
        IOMock.fromValue(ncall->ncall< 3?"a":"b",
        ncall->ncall==0?Duration.ofSeconds(1):
        Duration.ofSeconds(0)
        );

```

The failThenSucceed method adds a third function to mock failures. If this function returns null
instead of an exception, the specified value is returned and the computation succeed:

## <a name="installation"><a/> Installation

```code   

<dependency>
  <groupId>com.github.imrafaelmerino</groupId>
  <artifactId>jio-exp</artifactId>
  <version>1.0.0</version>
</dependency>

<dependency>
  <groupId>com.github.imrafaelmerino</groupId>
  <artifactId>jio-http</artifactId>
  <version>1.0.0</version>
</dependency>

<dependency>
  <groupId>com.github.imrafaelmerino</groupId>
  <artifactId>jio-mongodb</artifactId>
  <version>1.0.0</version>
</dependency>

<dependency>
  <groupId>com.github.imrafaelmerino</groupId>
  <artifactId>jio-test</artifactId>
  <version>1.0.0</version>
</dependency>
```

## <a name="requirements"><a/> Requirements

- Java 17 or greater
- [json-values](https://github.com/imrafaelmerino/json-values)


