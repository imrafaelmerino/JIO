<img src="logo/package_twitter_itsywb76/black/full/coverphoto/black_logo_white_background.png" alt="logo"/>  

- [Code wins arguments](#cwa)
- [Introduction](#Introduction)
- [jio-exp](#jio-exp)
    - [Creating effects](#Creating-effects)
    - [Lambdas](#Lambdas)
    - [Operations with effects](#Operations-with-effects)
    - [Expressions](#Expressions)
    - [Clocks](#Clocks)
    - [Debugging and JFR integration](#Debugging-and-JFR-integration)
    - [Installation](#Installation)
    - [Requirements and dependencies](#Requirements-and-dependencies)

## <a name="cwa"><a/> Code wins arguments

Let's implement a service with the following requirements:

* The signup service processes a JSON input containing at least two fields: email and address, both of which are
  expected as strings. The service proceeds to validate and standardize it using the Google Geocode API. The results
  obtained from Google are then presented to the frontend for the user's selection or rejection. In the event of any
  errors occurring during this process, the service will return an empty array of addresses.

* Additionally, the service stores the client's information in a MongoDB database. The identifier returned by MongoDB
  serves as the client identifier, which must be sent back to the frontend. If the client is successfully saved in the
  database and the user does not exist in the LDAP system, the service initiates two additional actions:
    * The user is sent to the LDAP service.
    * If the operation succeeds, an activation email is dispatched to the user.

* The signup service also provides information about the total number of existing clients in the MongoDB database. This
  information can be used by the frontend to display a welcoming message to the user, such as "You're the user number
  3000!" However, if any errors occur, the service will return -1, and the frontend will not display the message.

* Crucially, the signup service is designed to perform all these operations in parallel. This includes the request to
  Google for address validation and the MongoDB operations, including both persistence and counting.

* The response from the signup service follows this structure:

```code
{
  "number_users": integer, // Total number of existing clients in the DB (from MongoDB)
  "id": string, // MongoDB ID
  "timestamp": instant, // Timestamp indicating when the server begins processing the frontend request
  "addresses": array // Client addresses returned by Google Geocode API
}
```

The signup service constructor accepts some lambdas as input parameters. Think of a Lambda as a function that takes an
input and produces an output. However, unlike traditional functions, Lambdas don't throw exceptions (thank goodness!).
Instead, they can return exceptions as normal values.

```code

import jio.*;
package jio.api.exp;
import jio.time.Clock;
import jsonvalues.*;
import java.time.Instant;
import static java.util.Objects.requireNonNull;

public class SignupService implements Lambda<JsObj, JsObj> {

    final Lambda<JsObj, Void> persistLDAP;
    final Lambda<String, JsArray> normalizeAddresses;
    final Lambda<Void, Integer> countUsers;
    final Lambda<JsObj, String> persistMongo;
    final Lambda<JsObj, Void> sendEmail;
    final Lambda<String, Boolean> existsInLDAP;
    final Clock clock;

    //constructor

    @Override
    public IO<JsObj> apply(JsObj user) {
        String email = user.getStr("email");
        String address = user.getStr("address");

    return 
    JsObjExp.par("number_users", countUsers.apply(null)
                                           .map(JsInt::of),
                 "id",
                 persistMongo.apply(user)
                             .then(id -> IfElseExp.<String>predicate(existsInLDAP.apply(email))
                                                  .consequence(() -> IO.succeed(id))
                                                  .alternative(() -> PairExp.seq(persistLDAP.apply(user),
                                                                                 sendEmail.apply(user)
                                                                                 )
                                                                             .map(_ -> id)
                                                              )
                                                  .debugEach(email)
                                  )
                             .map(JsStr::of),
                 "addresses", normalizeAddresses.apply(address),
                 "timestamp", IO.lazy(clock)
                                .map(ms -> JsInstant.of(Instant.ofEpochMilli(ms)))
                )
                .debugEach(email);
    }
}

```

A few important points to note:

- Using `Instant.now()` directly all around your code is not a good practice as it introduces a side effect. It's better
  to use clocks, as I'll explain later. Think of a clock as a functional alternative to the widespread use
  of `Instant.now()`.

- The `par` constructor from the `JsObjExp` expression ensures that all the operations are performed in
  parallel: `countUsers`, `normalizeAddresses`, and `persistMongo`. If you want to execute them sequentially, simply
  use `JsObjExp.seq` instead.

- The `recover` functions provide an alternative value to be returned in case of any errors (-1 for `countUsers` and an
  empty array for `normalizeAddresses`, according to the specifications).

- `IfElseExp` is just a conditional expression, where the consequence is evaluated if the predicate is true, and the
  alternative if the predicate is false.

- `PairExp` is another expression that computes a tuple of two elements. Since we want to send the email after
  persisting successfully the user in the LDAP, we use the `seq` constructor. The `par` constructor would run both
  operations in parallel.

- `debugEach` is like magic. Contextual logging is a piece of cake with JIO and you can get all the information
  as you will see just registering the extension Debugger in your test. It uses JFR and send events whenever a
  computation happens.

- `IO` class is the most important one in JIO. You'll learn how to use it in the next section.

Since Lambdas are just functions, it's really simple to test the previous code. For now, let's just return constants,
but with jio-test you can create more elaborated stubs (from generators, with delays, simulating errors, you name it!).

```code


public class SignupTests {

    @RegisterExtension
    static Debugger debugger = new Debugger(Duration.ofSeconds(2));

    @Test
    public void test() {

        final Lambda<JsObj, Void> persistLDAP = user -> IO.NULL();
        final Lambda<String, JsArray> normalizeAddresses = address -> IO.succeed(JsArray.of("address1", "address2"));
        final Lambda<Void, Integer> countUsers = nill -> IO.succeed(3);
        final Lambda<JsObj, String> persistMongo = user -> IO.succeed("id");
        final Lambda<JsObj, Void> sendEmail = user -> IO.NULL();
        final Lambda<String, Boolean> existsInLDAP = email -> IO.TRUE;

        JsObj user = JsObj.of("email", JsStr.of("imrafaelmerino@gmail.com"),
                              "address", JsStr.of("Elm's Street")
                             );

        var resp = new SignupService(persistLDAP,
                                     normalizeAddresses,
                                     countUsers,
                                     persistMongo,
                                     sendEmail,
                                     existsInLDAP,
                                     Clock.realTime)
                .apply(user)
                .result();

        Assertions.assertEquals(3,resp.getInt("number_users"));
        Assertions.assertEquals("id",resp.getStr("id"));
        Assertions.assertTrue(resp.getArray("addresses").size() == 2);

    }

}

```

And thanks to `debugEach` and the debugger extension the following information will be printed out in the console:

```code

Started JFR stream for 2000 ms in SignupTests

event: eval-expression, expression: JsObjExpPar[number_users], result: SUCCESS, duration: 0 ns, output: 3
context: imrafaelmerino@gmail.com, thread: main, event-start-time: 2023-10-09T18:33:38.567551959+02:00

event: eval-expression, expression: JsObjExpPar[addresses], result: SUCCESS, duration: 0 ns, output: ["address1","address2"]
context: imrafaelmerino@gmail.com, thread: main, event-start-time: 2023-10-09T18:33:38.573194042+02:00

event: eval-expression, expression: IfElseExp-predicate, result: SUCCESS, duration: 0 ns, output: false
context: imrafaelmerino@gmail.com, thread: main, event-start-time: 2023-10-09T18:33:38.574709459+02:00

event: eval-expression, expression: PairExpSeq[1], result: SUCCESS, duration: 0 ns, output: null
context: imrafaelmerino@gmail.com, thread: main, event-start-time: 2023-10-09T18:33:38.5763505+02:00

event: eval-expression, expression: PairExpSeq[2], result: SUCCESS, duration: 0 ns, output: null
context: imrafaelmerino@gmail.com, thread: main, event-start-time: 2023-10-09T18:33:38.576501292+02:00

event: eval-expression, expression: PairExpSeq, result: SUCCESS, duration: 383792 ns, output: (null, null)
context: imrafaelmerino@gmail.com, thread: main, event-start-time: 2023-10-09T18:33:38.57634025+02:00

event: eval-expression, expression: IfElseExp-alternative, result: SUCCESS, duration: 0 ns, output: id
context: imrafaelmerino@gmail.com, thread: main, event-start-time: 2023-10-09T18:33:38.57674+02:00

event: eval-expression, expression: IfElseExp, result: SUCCESS, duration: 2073625 ns, output: id
context: imrafaelmerino@gmail.com, thread: main, event-start-time: 2023-10-09T18:33:38.57467925+02:00

event: eval-expression, expression: JsObjExpPar[id], result: SUCCESS, duration: 0 ns, output: id
context: imrafaelmerino@gmail.com, thread: main, event-start-time: 2023-10-09T18:33:38.576769917+02:00

event: eval-expression, expression: JsObjExpPar[timestamp], result: SUCCESS, duration: 0 ns, output: 2023-10-09T16:33:38.576Z
context: imrafaelmerino@gmail.com, thread: main, event-start-time: 2023-10-09T18:33:38.577138459+02:00

event: eval-expression, expression: JsObjExpPar, result: SUCCESS, duration: 16 ms, output: {"addresses":["address1","address2"],"number_users":3,"timestamp":"2023-10-09T16:33:38.576Z","id":"id"}
context: imrafaelmerino@gmail.com, thread: main, event-start-time: 2023-10-09T18:33:38.564300084+02:00

```

As you can see all the evaluations are performed by the main thread even when the operator `JsObjExp.par`
was used. Well, it's because the IO effects returned by the lambdas are just constants.
Let's use a more elaborated stubs using the jio-test library:

```code 


 Lambda<Void, Integer> countUsers =
            n -> StubSupplier.ofGen(IntGen.arbitrary(0, 100000))
                             .withExecutor(Executors.newVirtualThreadPerTaskExecutor())
                             .get();
 
 Lambda<JsObj, String> persistMongo =
            obj -> StubSupplier.ofGen(StrGen.alphabetic(20, 20))
                               .withExecutor(Executors.newVirtualThreadPerTaskExecutor())
                               .get();
 
 Lambda<JsObj, Void> sendEmail =
            obj -> StubSupplier.<Void>ofGen(Gen.cons(null))
                               .withExecutor(Executors.newVirtualThreadPerTaskExecutor())
                               .get();

 Lambda<String, Boolean> existsInLDAP =
            email -> StubSupplier.ofGen(BoolGen.arbitrary())
                                 .withExecutor(Executors.newVirtualThreadPerTaskExecutor())
                                 .get();
 Lambda<JsObj, Void> persistLDAP =
            obj -> StubSupplier.<Void>ofGen(Gen.cons(null))
                               .withExecutor(Executors.newVirtualThreadPerTaskExecutor())
                               .get();
 
 Lambda<String, JsArray> normalizeAddresses =
            address -> StubSupplier.ofGen(JsArrayGen.ofN(JsStrGen.alphabetic(),10))
                                   .withExecutor(Executors.newVirtualThreadPerTaskExecutor())
                                   .get();

```

and the result is

```code
Started JFR stream for 2000 ms in SignupTests

{"address":"Elm's Street","email":"imrafaelmerino@gmail.com"}
event: eval-expression, expression: JsObjExpSeq[number_users], result: SUCCESS, duration: 0 ns, output: 12653
context: imrafaelmerino@gmail.com, thread: virtual-32, event-start-time: 2023-10-10T08:59:48.058587167+02:00

event: eval-expression, expression: JsObjExpSeq[timestamp], result: SUCCESS, duration: 0 ns, output: 2023-10-10T06:59:48.058Z
context: imrafaelmerino@gmail.com, thread: main, event-start-time: 2023-10-10T08:59:48.059148042+02:00

event: eval-expression, expression: IfElseExp-predicate, result: SUCCESS, duration: 0 ns, output: false
context: imrafaelmerino@gmail.com, thread: virtual-43, event-start-time: 2023-10-10T08:59:48.061517209+02:00

event: eval-expression, expression: PairExpSeq[1], result: SUCCESS, duration: 0 ns, output: null
context: imrafaelmerino@gmail.com, thread: virtual-44, event-start-time: 2023-10-10T08:59:48.064076917+02:00

event: eval-expression, expression: PairExpSeq[2], result: SUCCESS, duration: 0 ns, output: null
context: imrafaelmerino@gmail.com, thread: virtual-45, event-start-time: 2023-10-10T08:59:48.064270209+02:00

event: eval-expression, expression: PairExpSeq, result: SUCCESS, duration: 673250 ns, output: (null, null)
context: imrafaelmerino@gmail.com, thread: virtual-44, event-start-time: 2023-10-10T08:59:48.063858417+02:00

event: eval-expression, expression: IfElseExp-alternative, result: SUCCESS, duration: 0 ns, output: FPmqgYeFZckMFnqHtrXj
context: imrafaelmerino@gmail.com, thread: virtual-44, event-start-time: 2023-10-10T08:59:48.064554542+02:00

event: eval-expression, expression: IfElseExp, result: SUCCESS, duration: 3122583 ns, output: FPmqgYeFZckMFnqHtrXj
context: imrafaelmerino@gmail.com, thread: virtual-44, event-start-time: 2023-10-10T08:59:48.061444542+02:00

event: eval-expression, expression: JsObjExpSeq[id], result: SUCCESS, duration: 0 ns, output: FPmqgYeFZckMFnqHtrXj
context: imrafaelmerino@gmail.com, thread: virtual-44, event-start-time: 2023-10-10T08:59:48.064584042+02:00

event: eval-expression, expression: JsObjExpSeq[addresses], result: SUCCESS, duration: 0 ns, output: ["m","r","E","k","l","u","P","q","U","F"]
context: imrafaelmerino@gmail.com, thread: virtual-37, event-start-time: 2023-10-10T08:59:48.066244125+02:00

event: eval-expression, expression: JsObjExpSeq, result: SUCCESS, duration: 17 ms, output: {"addresses":["m","r","E","k","l","u","P","q","U","F"],"number_users":12653,"timestamp":"2023-10-10T06:59:48.058Z","id":"FPmqgYeFZckMFnqHtrXj"}
context: imrafaelmerino@gmail.com, thread: virtual-37, event-start-time: 2023-10-10T08:59:48.051649417+02:00
```

Now we can see how the computation is in parallel from the thread fields.

But, to make our code resilient, let's add some retry logic. For `countUsers`, we want to make retries with a 50 ms
delay after an error, but not more than 300 ms in total for retries. On the other hand, for `persistLDAP`
and `sendEmail`, which run asynchronously, let's make retries every 200 ms for 5 seconds (they are legacy systems and
can be slow). Oh, I almost forgot, the `sendEmail` service sometimes doesn't fail but gives a response saying, "system
too busy, please wait." In this case will make up to five retries, waiting 1 second for the first retry, 2 seconds for
the second and so on.
How do we implement this? It's a piece of cake with JIO.

```

          

countUsers.apply(null)
          .retry(exception -> true,
                 RetryPolicies.constantDelay(Duration.ofMillis(50))
                              .limitRetriesByCumulativeDelay(Duration.ofMillis(300))
                )
          .recover(e -> -1)
          .map(JsInt::of)   
          
Predicate<HttpResponse<String> isSystemBusy = ...        
PairExp.par(persistLDAP.apply(payload),
            sendEmail.apply(payload)
                      .repeat(isSystemBusy,
                              RetryPolicies.incrementalDelay(Duration.ofSeconds(1))
                                           .append(RetryPolicies.limitRetries(5))
                             )
             )
        .retryEach(e -> true,
                   RetryPolicies.constantDelay(Duration.ofMillis(20))
                                .limitRetriesByCumulativeDelay(Duration.ofSeconds(2)))

```

Key points:

- retry takes in a predicate to specify which errors to consider, in this we'll retry no matter the error is
- Sometimes you want to make retries when the response is not a failure. That's exactly the purpose of the
  repeat function
- Retry policies are composable and very idiomatic!
- JIO scales very well. the more complex the logic doesn translate in a very complex expression, like it happends
  with the callback hell.
- `retryEach` is a powerful feature in JIO that allows you to individually retry every element of an expression that
  produces multiple results. In this case, it's being used to retry both the first and second elements of the tuple (
  composed of `persistLDAP.apply(payload)` and `sendEmail.apply(payload)`), providing granular control over retry
  behavior for each component of the operation. This fine-grained retry capability adds flexibility to handle different
  retry strategies for distinct parts of a complex operation.

## <a name="Introduction"><a/> Introduction

Functional Programming is all about working with pure functions and values. That's all. **However, where FP especially
shines is dealing with effects**.

But, what is an effect?

First take a look at the following piece of code:

```code  
  
int a = sum(1,2);  
  
int b = sum(1,2);  
  
```  

As far as the function `sum` is **pure**, you can refactor the previous piece of code and call the function just once:

```code  
  
int c = sum(1,2);  
  
int a = c;  
  
int b = c;  
  
```  

Both programs are equivalents and wherever you see `sum(1,2)` you can replace it by `c` without changing the meaning of
the program at all.

An effect, on the other hand, is something you can't call more than once unless you intended to:

```code  
  
Instant a = Instant.now();  
  
Instant b = Instant.now();  
  
```  

Because _now()_ returns a different value each time it's called and therefore is not a pure function, `a` and `b` are
different instants. Doing the following refactoring would change completely the meaning of the program (and still your
favourite IDE suggests you to do it at times!):

```code  
  
Instant now = Instant.now();  
  
Instant a = now;  
  
Instant b = now;  
  
```  

Here's when laziness comes into play. Since Java 8, we have suppliers. They are indispensable to do FP in Java. The
following piece of code is equivalent to the previous where `a` and `b` where two different instants:

```code  
  
Supplier<Instant> now = () -> Instant.now();  
  
Instant a = now.get();  
  
Instant b = now.get();  
  
```  

This property that allows you to factor out expressions is called **referential transparency**, and it's fundamental to
create and compose expressions.

What can you expect from JIO:

- Simple and powerful API
- Errors are first class citizens
- Simple and powerful testing tools (jio-test)
- Easy to extend and get benefit from all the above. Examples are jio-http, jio-mongodb or jio-chatgpt. And you can
  create your owns integrations!
- I don't fall into the logging-library war. This is something that sucks in Java. I just use Java Flight Recording!
- Almost zero dependencies (just plain Java!)
- JIO doesn't transliterate any functional API from other languages.  
  Any standard Java programmer will find JIO quite easy and familiar.

---  

## <a name="jio-exp"><a/> jio-exp

Let's model a funcional effect in Java!

```code  
  
import java.util.function.Supplier;  
import java.util.concurrent.CompletableFuture;  
  
public abstract class IO<O> implements Supplier<CompletableFuture<O>> {  
  
    @Override  
    CompletableFuture<O> get();  
      
    //blocking!  
    O result();  

}  
  
```  

Key Concepts:

- **`IO`  Definition**: The `IO` class is a fundamental component of JIO. It's an abstract class designed to represent
  functional effects or computations.

- **Lazy Computation**: `IO` is a lazy computation, implemented as a`Supplier`. This means that it's just a description
  of a computation and won't be executed until one of the methods, such as `CompletableFuture get()`  or `O result()`,
  is explicitly invoked.

- **Asynchronous Effects**: `IO` leverages `CompletableFuture` to represent asynchronous effects. Asynchronous effects
  are essential for avoiding thread blocking, especially when dealing with operations that might introduce latency.

- **Handling Errors**: A critical aspect of JIO is that `CompletableFuture` can represent both successful and failed
  computations. This approach ensures that errors are treated as first-class citizens, avoiding the need to throw  
  exceptions whenever an error occurs.

---  

### <a name="Creating-effects"><a/> Creating effects

Now that we got the ball rolling, let's learn how to create IO effects.

**From a constant or a computed value**

```code  
  
IO<String> effect = IO.succeed("hi");  
  
JsObj get(int id){...}  
IO<String> effect = IO.succeed(get(1)); //get(1) is invoked before constructing the effect  
  
```  

In both of the above examples, the effect will always compute the same value: either "hi" or the result of calling get(
1). There is no lazynes here, a value is computed right away and used to create the IO effect

**From an exception**

```code  
  
IO<String> effect = IO.fail(new RuntimeException("something went wrong :("));  
  
```  

Like with succeed, the effect will always produce the same result, in this case it fails always with the same exception,
which is instantiated before creating the effect. Do notice that no exception is thrown!

**From a lazy computation or a supplier**

This is a very common case, and you will use it all the time to create effects.

```code  

Suplier<JsObj> computation;  
IO<Long> effect = IO.lazy(computation);  
  
```  

In this example and effect is created but not like in `succeed` and `fail`, **nothing is evaluated**  since a `Supplier`
is lazy. It's very important to notice the difference. On the other hand, each time the  `get` or `result` methods are
invoked a new pottencially new value can be returned.

**From a callable**

We can think of a `Callable` as lazy computations like `Suppliers`, but with the important difference that they can
fail.

```code  
  
Callable<Long> callable;  
  
IO<Long> effect = IO.task(callable);  
  
```  

**From a CompletableFuture**:

This is a common case when working with APIs that returns futures like the `sendAsync` methods of Java HttpClient
introduced in Java 11

```code  
  
CompletableFuture<JsObj> get(String id){...}  
  
IO<JsObj> effect = IO.effect( () -> get(1) );  
  
```  

Like with `lazy` and `task`, the previous example doesn't evaluate anything to create the effect  
since the effect method takes in a `Supplier`.

In all the above examples, when the `get` or `result`methods are invoked, the values **will be computed on the caller  
thread**. Sometimes we need to control on what thread to perform a computation, especially when it's blocking.  
Whe can specify an executor, or to make use of the **ForkJoin** pool, which is not a problem since **JIO uses
internally
the [ManagedBlocker](https://docs.oracle.com/javase/7/docs/api/java/util/concurrent/ForkJoinPool.ManagedBlocker.html)**
interface, or you can even get benefit from the Loom project and use fibers!

**From a lazy computation or supplier that has to be executed on a specific pool**

```code  
  
Suplier<Long> computation;  
  
IO<Long> effect = IO.lazy(computation,  
                          Executors.newCachedThreadPool()  
                         );  
```  

**From a blocking operation that has to be executed on the ForkJoin Pool by the ManagedBlocker**

```code  
  
Supplier<JsObj> blockingComputation;  
Supplier<JsObj> blockingTask;  

IO<JsObj> effect = IO.managedLazy(blockingComputation);  
IO<JsObj> effect = IO.managedTask(blockingTask);  

```  

**With fibers**

```code  
IO<JsObj> effect = IO.lazy(blockingTask,  
                           Executors.newVirtualThreadPerTaskExecutor()  
                           );  
  
```  

**From autoclosable resources**

The `resource` method is used to create an IO effect that manages a resource implementing the `AutoCloseable`
interface. It takes a `Callable` that supplies the closable resource and a mapping function to transform the resource
into a value. This method ensures proper resource management, including automatic closing of the resource, to prevent
memory
leaks. It returns an IO effect encapsulating both the resource handling and mapping.

```code   
static <O, I extends AutoCloseable> IO<O> resource(Callable<I> resource,  
                                                   Lambda<I, O> map  
                                                   );  
```  

and an example:

```code  
Callable<FileInputStream> callable = () -> new FileInputStream("example.txt");  
  
// Create an IO effect using the resource method  
IO<String> resultEffect =  
         IO.resource(callable,  
                     inputStream -> {  
                                     try {  
                                        // Read the content of the file and return it as a String  
                                          byte[] bytes = new byte[inputStream.available()];  
                                          inputStream.read(bytes);  
                                          return IO.succeed(new String(bytes, 
                                                            StandardCharsets.UTF_8));  
                                          } 
                                          catch (IOException e) {  
                                             return IO.fail(e);  
                                          }  
                                     }
                     );  
```  

**Other regular IO effects**

```code
  
IO<Boolean> t = IO.TRUE  
  
IO<Boolean> f = IO.FALSE;  
  
IO<String> s = IO.NULL();  
IO<Integer> s = IO.NULL();  
 
```  

The NULL method creates an IO effect that always produces a result of null. It is a generic method that captures the
type of the caller, allowing you to create null effects with different result types. This can be useful when you need to
represent a null result in your functional code. These constants, `TRUE` and `FALSE`, represent IO effects that always
succeed with `true` and `false`, respectively.
  
---  

### <a name="Lambdas"><a/> Lambdas

Since we are going to work with effects all the time, I've defined two functions that take one or two inputs and produce
an effect and call them Lambda and BiLambda respectively:

```code  
  
interface Lambda<I, O> extends Function<I, IO<O>> {}  
  
interface BiLambda<A, B, O> extends BiFunction<A, B, IO<O>> {}  
  
```  

We'll use them all the time.
  
---  

### <a name="Operations-with-effects"><a/> Operations with effects

#### <a name="Making our code more resilient"><a/> Making our code more resilient

**Being persistent!**
Retrying failed operations is a crucial aspect of handling errors effectively in JIO. In real-world
scenarios,errors can sometimes be transient or caused by temporary issues, such as network glitches or resource
unavailability.  
By incorporating retry mechanisms, JIO empowers you to gracefully recover from such errors without compromising the
stability of your application. Whether it's a network request, database query, or any other effect, JIO's built-in
retry functionality allows you to define retry policies, such as exponential backoff or custom strategies, to ensure
that your operations have a higher chance of succeeding. This approach not only enhances the robustness of your
application but also minimizes the impact of transient errors, making JIO a valuable tool for building resilient and
reliable systems.

```code  
  
interface IO<O> extends Supplier<Future<O>> {  
  
  IO<O> retry(Predicate<Throwable> predicate,  
              RetryPolicy policy  
              );  
    
  IO<O> repeat(Predicate<O> predicate,  
               RetryPolicy policy  
              );  
}  
  
```  

While the `retry` method is primarily used to retry an operation when an error occurs (based on a specified exception
condition), the `repeat` method allows you to repeat an operation based on the result or outcome of the effect itself,  
providing flexibility for scenarios where retries are needed for reasons other than errors.  
Retry policies are created in a very declarative and composable way, for example:

```code  
  
import static jio.RetryPolicies.*  
  
Duration oneHundredMillis = Duration.ofMillis(100);  
  
Duration oneSec = Duration.ofSeconds(1);  
  
// up to five retries waiting 100 ms  
constantDelay(oneHundredMillis).append(limitRetries(5))  
  
//during 3 seconds up to 10 times  
limitRetries(10).limitRetriesByCumulativeDelay(Duration.ofSeconds(3))  
  
//5 times without delay and then, if it keeps failing, an incremental delay from 100 ms up to 1 second  
limiteRetries(5).followedBy(incrementalDelay(oneHundredMillis).capDelay(oneSec))  
  
```  

There are very interesting policies implemented based  
on [this article](https://aws.amazon.com/blogs/architecture/exponential-backoff-and-jitter/):  exponential backoff, full
jitter, equal jitter, decorrelated jitter etc

**Having a Backup plan!**
In scenarios where errors persist despite retries, JIO offers robust error-handling mechanisms
to ensure your application maintains resilience. Three key methods come into play:

```code  
  
interface IO<O> extends Supplier<Future<O>> {  
  
  IO<O> recover(Function<Throwable, O> fn);  
    
  IO<O> recoverWith(Lambda<Throwable, O> fn);  
    
  IO<O> fallbackTo(Lambda<Throwable, O> fn);  
  
}  
  
```  

**recover**: This method allows you to gracefully recover from an error by providing a function that maps the
encountered exception to a fallback value of type 'O.' It ensures that your application can continue its operation even
in the face of unexpected errors.

**recoverWith**: This method allows you to gracefully recover from an error by providing a function that maps the
encountered exception to a fallback value of type 'O.' It ensures that your application can continue its operation even
in the face of unexpected errors.

**fallbackTo**: Similar to 'recoverWith,' 'fallbackTo' allows you to switch to an alternative effect (specified by the
provided function) when an error occurs. However, it introduces an important distinction: if the alternative effect also
encounters an error, 'fallbackTo' will return the original error from the first effect. This ensures that error
propagation is maintained while enabling you to gracefully handle errors and fallback to alternative operations when
needed.
  
---  

#### Other common operations

**Being Functional**: JIO encourages a functional programming style with the following methods:

```code  
  
public interface IO<O> extends Supplier<Future<O>> {  
  
    IO<A> map(Function<O, A> fn);  
      
    IO<A> then(Lambda<O, A> fn);  
  
}  
  
```  

- `map`: Transforms the result of an effect using a provided function, allowing you to map values from one type to
  another.
- `then` (akin to `flatMap` or `bind` in other languages and a core function in monads): Applies a lambda function to  
  the result of the effect, creating a new effect that depends on the previous result. The name 'then' is used here for
  conciseness.

**Being Impatient!**: Time is of the essence, and JIO offers methods to deal with timeouts:

```code  
  
interface IO<O> extends Supplier<Future<O>> {  
  
    IO<O> timeout(long time,  
                  TimeUnit unit  
                 );  
      
    IO<O> timeoutOrElse(long time,  
                        TimeUnit unit,  
                        Supplier<O> defaultVal  
                       );    
}  
```  

- `timeout`: Sets a timeout for the effect, allowing you to limit how long you are willing to wait for its completion.
- `timeoutOrElse`: Similar to `timeout`, but with a fallback value that is returned if the timeout is exceeded.

**Being sneaky!**: Sometimes, you need to sneak a peek into the execution of an effect:

```code  
interface IO<O> extends Supplier<Future<O>> {  
  
    IO<O> peekFailure(Consumer<Throwable> failConsumer);  
      
    IO<O> peekSuccess(Consumer<O> successConsumer);  
      
    IO<O> peek(Consumer<O> successConsumer,  
               Consumer<Throwable> failureConsumer  
              );  
}  
  
```  

- `peekFailure`: Allows you to observe and potentially handle failures by providing a consumer that logs exceptions in
  the JFR (Java Flight Recorder) system.
- `peekSuccess`: Similarly, you can observe and process successful results using a success consumer.
- `peek`: Combines both success and failure consumers, giving you full visibility into the effect's execution.  
  Exceptions occurring here are logged in the JFR system and do not alter the result of the effect.

**I race you!**: When you require a result as quickly as possible among multiple alternatives, and you're uncertain
which
one will be the fastest:

```code  
  
interface IO<O> extends Supplier<CompletableFuture<O>> {  
  
    static <O> IO<O> race(IO<O> first, IO<O>... others);  
  
}  
  
```  

`race` method returns the result of the first effect that completes (whether it succeeds or fails), allowing you to make
quick decisions based on the outcome.

"Sometimes, it is valuable to have fine-grained control over the execution context responsible for computing the values
of effects. JIO provides a set of methods with the 'on' suffix to cater to this specific need. These methods allow you  
to specify the execution context or thread pool in which the effect's computation should occur, providing you with
control over concurrency and resource allocation. Here are the key methods with the 'on' suffix:

```code  
interface IO<O> extends Supplier<CompletableFuture<O>> {  
  
    IO<O> recoverWithOn(Lambda<Throwable, O> fn,  
                        Executor executor  
                        );  
      
    IO<O> retryOn(Predicate<Throwable> predicate,  
                  RetryPolicy policy,  
                  Executor executor  
                  );  
      
    <Q> IO<Q> thenOn(Lambda<O, Q> fn,  
                     Executor executor  
                     );  
      
    IO<O> fallbackToOn(Lambda<Throwable, O> lambda,  
                       Executor executor  
                      );  
}  
```  

  
---  

## Expressions

**Using expressions and function composition is how we deal with complexity in Functional Programming**.  
With the following expressions, you will have a comprehensive toolkit to model effects, combine them in powerful ways,
and tame complexity effectively.

### IfElseExp

The `IfElseExp` expression allows you to conditionally choose between two computations based on a predicate. If the  
predicate evaluates to true, the consequence is computed, and if it's false, the alternative computation is chosen. Both
the consequence and alternative are represented as lazy computations of IO effects.

```code  
  
import jio.IfElseExp;  
  
IO<O> exp = IfElseExp.<O>predicate(IO<Boolean> condition)  
                     .consequence(Supplier<IO<O>> consequence)  
                     .alternative(Supplier<IO<O>> alternative);  
  
  
```  

### SwitchExp

The `SwitchExp` expression mimics the behavior of a switch construct, enabling multiple pattern-value branches. It
evaluates an effect or value of type `I` and allows multiple clauses based on the evaluation. The `match` method
compares the value or effect with patterns and selects the corresponding supplier. Patterns can be values, lists of
values, or even predicates.

```code  
  
// matches a value of type I  
  
IO<O> exp =  
          SwitchExp<O>.eval(I value)  
                      .match(I pattern1, Supplier<IO<O>> value1,  
                             I pattern2, Supplier<IO<O>> value2,  
                             I pattern3, Supplier<IO<O>> value3,  
                             Supplier<IO<O>> otherwise  
                            );  
  
// matches an effect of type I  
  
IO<O> exp=  
        SwitchExp<I, O>.eval(IO<I> value)  
                       .match(I pattern1, Supplier<IO<O>> value1,  
                              I pattern2, Supplier<IO<O>> value2,  
                              I pattern3, Supplier<IO<O>> value3,,  
                              Supplier<IO<O>> otherwise  
                             );  
  
  
// For example, the following expression reduces to "Wednesday"  
  
IO<O> exp=  
         SwitchExp<String>.eval(3)  
                          .match(1,() -> IO.succedd("Monday"),  
                                 2,() -> IO.succedd("Tuesday"),  
                                 3,() -> IO.succedd("Wednesday"),  
                                 4,() -> IO.succedd("Thursday"),  
                                 5,() -> IO.succedd("Friday"),  
                                 ()->IO.succedd("weekend")  
                                );  
```  

The same as before but using lists instead of constants as patterns.

```code  
  
IO<O> exp =  
          SwitchExp<I, O>.eval(I value)  
                         .match(List<I> pattern1,Supplier<IO<O>> value1,  
                                List<I> pattern2,Supplier<IO<O>> value2,  
                                List<I> pattern3,Supplier<IO<O>> value3,  
                                Supplier<IO<O>> otherwise  
                                );  
  
// For example, the following expression reduces to "third week"  
IO<O> exp=  
         SwitchExp<Integer, String>.eval(20)  
                                   .match(List.of(1,2,3,4,5,6,7), 
                                          () -> IO.succeed("first week"),  
                                          List.of(8,9,10,11,12,13,14), 
                                          () -> IO.succeed("second week"),  
                                          List.of(15,16,17,18,19,20,10), 
                                          () -> IO.succeed("third week"),  
                                          List.of(21,12,23,24,25,26,27), 
                                          () -> IO.succeedd("forth week"),  
                                          () -> IO.succeed("last days of the month")  
                                         );  
```  

Last but not least, you can use predicates as patterns instead of values or list of values:

```code  
  
IO<O> exp=  
        SwitchExp<I, O>.eval(IO<I> value)  
                       .match(Predicate<I> pattern1, Supplier<IO<O>>value1,  
                              Predicate<I> pattern2, Supplier<IO<O>>value2,  
                              Predicate<I> pattern3, Supplier<IO<O>>value3,  
                              Supplier<IO<O>> otherwise  
                              );  
  
// For example, the following expression reduces to the default value  
  
IO<O> exp=  
        SwitchExp<Integer, String>.eval(IO.succeed(20))  
                                  .match(i -> i < 5, () -> IO.succeed("lower than five"),  
                                         i -> i < 10, () -> IO.succeed("lower than ten"),  
                                         i-> i < 20, () -> IO.succeed("lower than twenty"),  
                                         () -> IO.succeed("greater or equal to twenty")  
                                         );  
```  

### CondExp

`CondExp` is a set of branches and a default value. Each branch consists of an effect that computes a boolean (the  
condition) and its associated effect. The expression is reduced to the value of the first branch with a true
condition,  
making the order of branches significant. If no condition is true, it computes the default effect.

```code  
  
IO<O> exp=  
    CondExp.<O>seq(IO<Boolean> cond1, Supplier<IO<O>> value1,  
                   IO<Boolean> cond2, Supplier<IO<O>> value2,  
                   IO<Boolean> cond3, Supplier<IO<O>> value3,  
                   Supplier<IO<O>> otherwise  
                  );  
  
  
IO<O> exp =  
    CondExp.<O>par(IO<Boolean> cond1, Supplier<IO<O>> value1,  
                   IO<Boolean> cond2, Supplier<IO<O>> value2,  
                   IO<Boolean> cond3,Supplier<IO<O>> value3,  
                   Supplier<IO<O>> otherwise  
                  );  
  
```  

### AllExp and AnyExp

`AllExp` and `AnyExp` provide idiomatic boolean expressions for "AND" and "OR." They allow you to compute multiple  
boolean effects, either sequentially or in parallel.

```code  
  
IO<Boolean> allPar = AllExp.par(IO<Boolean> cond1, IO<Boolean> cond2,....);  
IO<Boolean> allSeq = AllExp.seq(IO<Boolean> cond1, IO<Boolean> cond2,....);  
  
IO<Boolean> anyPar = AnyExp.par(IO<Boolean> cond1, IO<Boolean> cond2,...);  
IO<Boolean> anySeq = AnyExp.seq(IO<Boolean> cond1, IO<Boolean> cond2,...);  
  
```  

### PairExp and TripleExp

`PairExp` and `TripleExp` allow you to work with tuples of two and three elements, respectively. You can compute each  
element either in parallel or sequentially.

```code  
  
IO<Pair<A, B> pairPar = PairExp.par(IO<A> val1,IO<B> val2);  
  
IO<Pair<A, B> pairSeq = PairExp.seq(IO<A> val1,IO<B> val2);  
  
IO<Triple<A, B, C> triplePar = TripleExp.par(IO<A> val1,IO<B> val2,IO<C> val3);  
  
IO<Triple<A, B, C> tripleSeq = TripleExp.seq(IO<A> val1,IO<B> val2,IO<C> val3);  
  
```  

### JsObjExp and JsArrayExp

`JsObjExp` and `JsArrayExp` are data structures resembling raw JSON. You can compute their values sequentially or in  
parallel. You can mix all the expressions discussed so far and nest them, providing you with immense flexibility and  
power in handling complex data structures.

```code  
  
IfElseExp<JsStr> a = IfElseExp.<JsStr>predicate(IO<Boolean> condition)  
                              .consequence(IO<JsStr> consequence)  
                              .alternative(IO<JsStr> alternative);  
  
JsArrayExp b = 
    JsArrayExp.seq(SwitchExp<Integer, JsValue>.match(n)  
                                              .patterns(1, Supplier<IO<JsValue>> value1,  
                                                        2, Supplier<IO<JsValue>> value2,  
                                                        Supplier<IO<JsValue>> defaultValue  
                                                       ),  
                   CondExp.par(IO<Boolean> cond1, Supplier<IO<JsValue>>value1,  
                               IO<Boolean> cond2, Supplier<IO<JsValue>>value3,  
                               Supplier<IO<JsValue>> defaultValue  
                              )  
                 );  
  
JsObjExp c = JsObjExp.seq("d", AnyExp.seq(IO<Boolean> cond1, IO<Boolean> cond2)  
                                     .map(JsBool::of),  
                          "e", AllExp.par(IO<Boolean> cond1, IO<Boolean> cond2)  
                                     .map(JsBool::of),  
                          "f", JsArrayExp.par(IO<JsValue> value1, 
                                              IO<JsValue> value2)  
                          );  
  
JsObjExp exp = JsObjExp.par("a",a,  
                            "b",b,  
                            "c",c  
                           );  
  
JsObj json = exp.result();  
  
```  

Here are some key points about the code example:

1. **Readability**: The code is relatively easy to read and understand, thanks to the fluent API style provided by
   JIO's expressions. This makes it straightforward to follow the logic of constructing a `JsObj` with multiple
   key-value pairs.

2. **Modularity**: Each key-value pair is constructed separately, making it easy to add, modify, or remove components
   without affecting the others. This modularity is a significant advantage when dealing with complex data structures.

3. **Parallelism**: The example demonstrates the ability to perform computations in parallel when constructing  
   the `JsObj`. By using expressions like `JsObjExp.par`, you can take advantage of multicore processors and improve
   performance.

4. **Nesting**: The example also shows that you can nest expressions within each other, allowing for recursive data  
   structures. This is valuable when dealing with deeply nested expressions or other complex data formats.

Overall, the code example effectively illustrates how JIO's expressions enable you to create, manipulate, and compose
functional effects to handle complex data scenarios. It highlights the conciseness and expressiveness of the library  
when dealing with such tasks.
  
---  

## <a name="Clocks"><a/> Clocks

In functional programming, it's crucial to maintain a clear separation between inputs and outputs of a function.
When dealing with time-related operations, such as retrieving the current date or time, it becomes even more critical
to adhere to this principle. This is where the concept of clocks in JIO comes into play.
A clock in JIO is essentially a supplier that returns a numeric value, representing time. There are three types of  
clocks available:

- Realtime: This clock is affected by Network Time Protocol (NTP) adjustments and can move both forwards and backward
  in time. It is implemented using the System.currentTimeMillis() method. Realtime clocks are typically used when you
  need to work with the current wall-clock time.
- Monotonic: Monotonic clocks are useful for measuring time intervals and performing time-related comparisons. They
  are not affected by NTP adjustments and provide a consistent and continuous time source. Monotonic clocks are
  implemented using the System.nanoTime() method.
- Custom: JIO allows you to create your custom clocks. Custom clocks are particularly valuable for testing scenarios  
  where you want to control the flow of time, possibly simulating the past or future.

```code  
  
sealed interface Clock extends Supplier<Long> permits Monotonic, MyClock, RealTime {}  
  
```  

Every time you write _new Date()_ or _Instant.now()_ in the body of a method or function, you are creating a bug.  
Remember that in FP, all the inputs must appear in the signature of a function. Dealing with time, it's even more  
important. Also, it's impossible to control by any test the value of that timestamp which leads to code difficult to  
test.

### Why It Matters

The reason why dealing with time as an input is crucial in functional programming is to make code more predictable,  
testable, and less error-prone. Consider the following scenario, which is a common source of bugs:

**Bug Scenario**:

```code  
public class PaymentService {  
    public boolean processPayment(double amount) {  
        // Get the current date and time  
        Instant currentTime = Instant.now();  
          
        // Perform payment processing logic  
        // ...  
          
        // Check if the payment was made within a specific time window  
        Instant windowStart = currentTime.minus(Duration.ofHours(1));  
        Instant windowEnd = currentTime.plus(Duration.ofHours(1));  
          
        return paymentTime.isAfter(windowStart) && paymentTime.isBefore(windowEnd);  
    }  
}  
  
```  

**Better Version Using a Clock**  
A better approach is to pass a clock as a method parameter:

```code  
  
public class PaymentService {  
    public boolean processPayment(double amount, Clock clock) {  
        // Get the current time from the provided clock  
        Instant currentTime = Instant.ofEpochMilli(clock.get());  
  
        // Perform payment processing logic  
        // ...  
          
        // Check if the payment was made within a specific time window  
        Instant windowStart = currentTime.minus(Duration.ofHours(1));  
        Instant windowEnd = currentTime.plus(Duration.ofHours(1));  
  
        return paymentTime.isAfter(windowStart) && paymentTime.isBefore(windowEnd);  
    }  
}  
  
  
```  

In this improved version, we pass a Clock object as a parameter to the processPayment method. This approach offers  
several advantages:

- Testability: During testing, you can provide a custom clock that allows you to control the current time, making
  tests more predictable and reliable.
- Predictability: The behavior of the method is consistent regardless of when it's called since it depends on the  
  provided clock.

By using a clock as a parameter, you enhance the reliability and maintainability of your code, especially in scenarios  
where time plays a critical role.

## <a name="Debugging-and-JFR-integration"><a/> Debugging and Java Flight Recorder (JFR) Integration

### Why I chose JFR

"In the world of Java, there has long been a multitude of logging libraries and frameworks, each with its strengths
and limitations. However, the introduction of Java Flight Recorder (JFR) has been a game-changer. JFR is a native and
highly efficient profiling and event recording mechanism embedded within the Java Virtual Machine (JVM). Its native
integration means it operates seamlessly with your Java applications, imposing minimal performance overhead. JFR
provides unparalleled visibility into the inner workings of your code, allowing you to capture and analyze events with
precision.  
Unlike external logging libraries, JFR doesn't rely on third-party dependencies or introduce additional complexity to  
your projects. By using JFR within JIO, you harness the power of this built-in tool to gain deep insights into the  
behavior of your functional effects and expressions, all while keeping your codebase clean and efficient. JFR is the  
dream solution for Java developers seeking robust debugging and monitoring capabilities with minimal hassle."

Debugging and monitoring the behavior of your JIO-based applications is essential for troubleshooting, performance  
optimization, and gaining insights into your functional effects and expressions. JIO provides comprehensive support
for debugging and integration with Java Flight Recorder (JFR) to capture and analyze events.
  
---  

### Debugging Individual Effects

You can enable debugging for individual effects using the debug method. This method creates a copy of the effect that  
generates a `RecordedEvent` and sends it to the Flight Recorder system. You can customize the event using the
`EventBuilder` provided. This feature is invaluable for monitoring the behavior of specific effects in your application.

```code  
  
/**  
* Creates a copy of this effect that generates an {@link jdk.jfr.consumer.RecordedEvent} from the result of the  
* computation and sends it to the Flight Recorder system. Customization of the event can be achieved using the  
* provided {@link EventBuilder}.  
*  
* @param builder the builder used to customize the event.  
* @return a new effect with debugging enabled.  
* @see ExpEvent  
*/  
public IO<O> debug(final EventBuilder<O> builder);  
  
```  

You can call debug() without providing an EventBuilder, and JIO will use a default event builder. This simplifies the  
debugging process for common use cases.

### Debugging Expressions

JIO's debugging capabilities extend beyond individual effects. You can attach a debug mechanism to each operand of an  
expression using the `debugEach` method. This allows you to monitor and log the execution of each operand
individually.  
The provided `EventBuilder` or a descriptive context can be used to customize the debug events for each operand.

```code  
  
/**  
* Attaches a debug mechanism to each operand of this expression, allowing you to monitor and log the execution of  
* each operand individually.  
*  
* @param messageBuilder the builder for creating debug events for each operand.  
* @return a new expression with debug behavior applied to each operand.  
*/  
abstract Exp<O> debugEach(final EventBuilder<O> messageBuilder);  
  
/**  
* Attaches a debug mechanism to each operand of this expression, allowing you to monitor and log the execution of  
* each operand individually.  
*  
* @param context a descriptive context for the debug events of each operand.  
* @return a new expression with debug behavior applied to each operand.  
*/  
abstract Exp<O> debugEach(final String context);  
  
```  

By using `debugEach`, you can gain insights into the behavior of complex expressions and identify any issues or  
bottlenecks that may arise during execution. All the subexpressions and the final result will be recorded with the
same context, making it easier to relate them and analyze their interactions.  
JIO's logging and JFR integration features provide valuable tools for contextual logging, debugging, profiling, and  
monitoring your functional effects and expressions, helping you build robust and reliable applications.

### Streaming JFR Events

JIO also provides the `EventDebugger` class, which allows you to stream through the events recorded in JFR. This class  
simplifies event recording and provides methods for configuring event handling and duration.

```code  
  
package jio.jfr;  
  
import jdk.jfr.consumer.EventStream;  
import jdk.jfr.consumer.RecordedEvent;  
import jdk.jfr.consumer.RecordingStream;  
  
import java.io.IOException;  
import java.text.ParseException;  
import java.time.Duration;  
import java.time.Instant;  
import java.util.Objects;  
import java.util.function.Consumer;  
  
/**  
* An abstract base class for debugging events using Java Flight Recorder (JFR).  
* Provides methods for configuring event recording and handling recorded events.  
*/  
public abstract class EventDebugger {  
  
    // ... (constructor and other methods)  
      
    /**  
    * Starts asynchronous event recording for the specified duration.  
    *  
    * @param duration The duration (in milliseconds) for which to capture events.  
    */  
    public void startAsync(final int duration);  
      
    /**  
    * Closes the EventStream, stopping event recording.  
    */  
    public void close();  
      
    /**  
    * Blocks until event recording is terminated.  
    * Throws a RuntimeException if interrupted.  
    */  
    public void awaitTermination();  
}  
  
  
```  

With EventDebugger, you can seamlessly integrate JFR event streaming into your debugging and profiling workflows,  
allowing you to analyze the recorded events in detail.

JIO's logging and JFR integration features provide valuable tools for contextual logging, debugging, profiling, and  
monitoring your functional effects and expressions, helping you build robust and reliable applications.

### EventDebugger example

Here's an example of how to use the `EventDebugger`.

Suppose you have a JIO-based application with various effects and expressions that perform complex computations. You  
want to gain insights into the execution of these effects and expressions by capturing and analyzing events using Java  
Flight Recorder (JFR).

1. **Create a Custom EventDebugger Class**: First, create a custom `EventDebugger` class that extends  
   the `EventDebugger` base class provided by JIO. This custom class allows you to specify the events you want to  
   capture and define how you handle recorded events.

```code  
import jio.jfr.EventDebugger;  
import jdk.jfr.consumer.RecordedEvent;  
  
public class MyEventDebugger extends EventDebugger {  
  
    public MyEventDebugger() {  
        // Initialize the EventDebugger with the event name and event consumer.  
        super("MyAppEvents", event -> handleEvent(event));  
    }  
      
    private void handleEvent(RecordedEvent event) {  
        // Handle the recorded event here.  
        // You can log, analyze, or take any actions based on the event data.  
        System.out.println("Recorded Event: " + event);  
    }  
}  
```  

In this example, we've created a custom `MyEventDebugger` class that captures events with the name "MyAppEvents" and  
defines how to handle each recorded event.

2. **Instrument Your Application**: Next, you need to instrument your application with the `MyEventDebugger` class to  
   start event recording. You can do this in your application's entry point or any relevant location.

```code  
public class MyApp {  
  
    public static void main(String[] args) {  
        // Create an instance of MyEventDebugger to start event recording.  
        MyEventDebugger eventDebugger = new MyEventDebugger();  
          
        // ...  
          
        // Optionally, specify a duration for event recording (e.g., 5000 milliseconds).  
        eventDebugger.startAsync(5000);  
          
        // Ensure the event recording is properly closed when your application exits.  
        eventDebugger.awaitTermination();  
    }  
}  
```  

In this example, we create an instance of `MyEventDebugger` to start event recording. You can perform various
operations and effects within your application, and the `MyEventDebugger` will capture events during this period.

3. **Analyzing Captured Events**: As your application runs, events are captured and handled by the `MyEventDebugger`.  
   You can customize the event handling logic to log, analyze, or perform any actions you need based on the recorded  
   events.

The `handleEvent` method in the `MyEventDebugger` class defines how to handle each recorded event. You can access
event data and decide how to utilize it for debugging, profiling, or monitoring purposes.

By using the `EventDebugger` class, you can gain valuable insights into the behavior of your JIO-based application,  
identify performance bottlenecks, and troubleshoot issues effectively. This approach allows for contextual logging and  
monitoring of events related to your functional effects and expressions.

Feel free to customize the `MyEventDebugger` class and event handling logic to suit your specific debugging and  
monitoring needs.
  
---  

## <a name="Installation"><a/> Installation

```code  
  
<dependency>  
    <groupId>com.github.imrafaelmerino</groupId>  
    <artifactId>jio-exp</artifactId>  
    <version>1.0.0</version>  
</dependency>  
  
```  

  
---  

## <a name="Requirements and dependencies"><a/> Requirements and dependencies

- Java 17 or greater
- [json-values](https://github.com/imrafaelmerino/json-values)