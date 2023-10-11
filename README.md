<img src="logo/package_twitter_itsywb76/black/full/coverphoto/black_logo_white_background.png" alt="logo"/>  

[![Maven](https://img.shields.io/maven-central/v/com.github.imrafaelmerino/jio-exp/1.0.0-RC1)](https://search.maven.org/artifact/com.github.imrafaelmerino/jio-exp/1.0.0-RC1/jar)

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

The age-old "Hello world" example has outlived its usefulness. While it once served as a foundational teaching tool, its
simplicity no longer suffices in today's world. In the current landscape, where real-world scenarios are markedly more
intricate, I present a "Hello world" example that truly mirrors the complexity of modern development.

### Signup Service specification

Let's jump into the implementation of a signup service with the following requirements:

1. The signup service takes a JSON input containing at least two fields: email and address, both expected as strings.
   The service's first step is to validate and standardize the address using the Google Geocode API. The results
   obtained from Google are then presented to the frontend for user selection or rejection.
2. In addition to address validation, the service stores the client's information in a MongoDB database. The MongoDB
   identifier returned becomes the client identifier, which must be sent back to the frontend. If the client is
   successfully saved in the database and the user doesn't exist in the LDAP system, two additional actions occur:
    - The user is sent to the LDAP service.
    - If the operation succeeds, an activation email is sent to the user.
3. The signup service also provides information about the total number of existing clients in the MongoDB database. This
   information can be utilized by the frontend to display a welcoming message to the user, such as "You're the user
   number 3000!" If an error occurs the service returns -1, and the frontend will not display the message.
4. Crucially, the signup service is designed to perform all these operations in parallel. This includes the request to
   Google for address validation and the MongoDB operations, which encompass both data persistence and counting.
5. The signup service also returns a timestamp indicating the instant the user became a member. This timestamp can be
   used by the frontend to show the user the exact date and time they joined.

### Response Structure

The response from the signup service follows this structure:

```code
{
  "number_users": integer,
  "id": string,
  "timestamp": instant,
  "addresses": array
}
```

### Signup Service implementation

The `SignupService` orchestrates all the operations with elegance and efficiency. This service is constructed with a set
of lambdas, where a lambda is essentially a function that takes an input and produces an output. Unlike traditional
functions, lambdas won't throw exceptions; instead, they gracefully return exceptions as regular values.

```java

import jio.*;
import jio.time.Clock;
import jsonvalues.*;
import java.time.Instant;
import static java.util.Objects.requireNonNull;

public class SignupService implements Lambda<JsObj, JsObj> {
  
    Lambda<JsObj, Void> persistLDAP;
    Lambda<String, JsArray> normalizeAddresses;
    Lambda<Void, Integer> countUsers;
    Lambda<JsObj, String> persistMongo;
    Lambda<JsObj, Void> sendEmail;
    Lambda<String, Boolean> existsInLDAP;
    Clock clock;

    //constructor

@Override
public IO<JsObj> apply(JsObj user) {
  
  String email = user.getStr("email");
  String address = user.getStr("address");

  return 
  JsObjExp.par("number_users", countUsers.apply(null)
                                         .recover(exc -> -1)
                                         .map(JsInt::of),
               "id",
               persistMongo.apply(user)
                           .then(id -> IfElseExp.<String>predicate(existsInLDAP.apply(email))
                                          .consequence(() -> IO.succeed(id))
                                          .alternative(() -> PairExp.seq(persistLDAP.apply(user),
                                                                         sendEmail.apply(user)
                                                                         )
                                                                    .debugEach(email)    
                                                                    .map(pair -> id)
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

Noteworthy points:

- **Clocks**: In modern programming, managing time is critical, and using `Instant.now()` directly throughout your code
  can introduce side effects. We advocate using clocks, represented by the `clock` instance in our `SignupService`. A
  clock is a functional alternative to the widespread use of `Instant.now()`. It provides better control and makes your
  code more predictable.


- **JsObjExp**: The `JsObjExp` expression is highly expressive when building JSON objects. It allows us to define the
  structure of the resulting JSON object in a clear and declarative manner. In our code, we use it to construct a JSON
  object with multiple key-value pairs, each representing a specific piece of
  information (`"number_users"`, `"id"`, `"addresses"`, `"timestamp"`, etc.). This approach simplifies the creation of
  complex JSON structures and enhances code readability.


- Error handling is handled gracefully with the `recover` functions, providing alternative values (e.g., -1
  for `countUsers`) in case of errors.

- **IfElseExp**: The `IfElseExp` expression is a clear and concise way to handle conditional logic. It enables us to
  specify the consequence and alternative branches based on a predicate (`existsInLDAP.apply(email)` in this case). This
  expressiveness makes it evident that if the user exists in LDAP, we succeed with an ID, otherwise, we perform a
  sequence of operations using `PairExp`. It enhances the readability of the code, making it easy to understand the
  branching logic.

- **PairExp**: The `PairExp` expression simplifies the creation of tuples or pairs of values. In our case, we
  use `PairExp.seq` to execute two operations (`persistLDAP.apply(user)` and `sendEmail.apply(user)`) sequentially,
  although it's important to note that we are not interested in the pair result, as both of these operations return
  `void`.

- **debugEach**: Debugging is an essential part of software development, and contextual logging is a powerful tool for
  diagnosing issues. JIO simplifies debugging with its `debug` and `debugEach` methods, which allows you to log
  information at various points in your code effortlessly.

- **JFR (Java Flight Recorder)**: JIO leverages JFR for logging purposes. This choice offers several advantages. First,
  it's Java-native, which means it seamlessly integrates with the Java ecosystem, ensuring compatibility and
  performance. Second, it avoids the complexities and potential conflicts associated with using external logging
  libraries, of which there are many in the Java landscape. By relying on JFR, we maintain a lightweight and efficient
  approach to logging that is both reliable and highly effective.

- The backbone of JIO is the `IO` class that we'll explore in detail in the next section.

### Testing the Signup Service with JIO

JIO offers an elegant and efficient approach to testing, especially when it comes to working with lambdas. It eliminates
the need for external libraries like Mockito, making your testing experience smoother and more expressive.

### Simplified Lambda Testing

In your test class, JIO allows you to implement lambda functions directly. This approach enables you to tailor the
behavior of each lambda to your specific test scenario, making your tests highly adaptable and expressive.

Let's see how to test the `SignupService` using JIO:

```code


public class SignupTests {

    @RegisterExtension
    static Debugger debugger = new Debugger(Duration.ofSeconds(2));

    @Test
    public void test() {

        Lambda<JsObj, Void> persistLDAP = user -> IO.NULL();
        
        Lambda<String, JsArray> normalizeAddresses =
                address -> IO.succeed(JsArray.of("address1", "address2"));
        
        Lambda<Void, Integer> countUsers = nill -> IO.succeed(3);
        
        Lambda<JsObj, String> persistMongo = user -> IO.succeed("id");
        
        Lambda<JsObj, Void> sendEmail = user -> IO.NULL();
        
        Lambda<String, Boolean> existsInLDAP = email -> IO.FALSE;

        JsObj user = JsObj.of("email", JsStr.of("imrafaelmerino@gmail.com"),
                              "address", JsStr.of("Elm's Street")
                             );

        JsObj resp = new SignupService(persistLDAP,
                                       normalizeAddresses,
                                       countUsers,
                                       persistMongo,
                                       sendEmail,
                                       existsInLDAP,
                                       Clock.realTime
                                       )
                                      .apply(user)
                                      .result();
        //Junit assertions
        Assertions.assertTrue(resp.containsKey("number_users"));
        
    }

}

```

### Debugging with the Debugger Extension

When it comes to debugging your code during testing, having access to detailed information is invaluable. JIO's Debugger
extension simplifies this process by creating an event stream for a specified duration and printing all the events sent
to the Java Flight Recorder (JFR) system during that period.

Here's a breakdown of how it works:

1. **Debugger Extension Registration**: In your test class, you register the Debugger extension using
   the `@RegisterExtension` annotation. You specify the duration for which the debugger captures events, such
   as `Duration.ofSeconds(2)`.

2. **Using `debug` and `debugEach`**: Within your code, you utilize the `debug` and `debugEach` methods provided by JIO.
   These methods allow you to send events to the JFR system, providing crucial context about the execution flow.

3. **Event Printing**: During the execution of the test for the specified duration, the Debugger extension prints out all 
   the events that were sent to the JFR system. These events include information about the expressions being evaluated, 
   their results, execution durations, contextual data, and more.

4. **Stream Ordering**: Importantly, the event stream is ordered. Events are printed in the order in which they
   occurred, providing a clear chronological view of your code's execution.

5. **Pinpointing Bugs and Issues**: With the event stream and detailed logs in hand, you can easily pinpoint any bugs,
   unexpected behavior, or performance bottlenecks. The chronological order of events helps you understand the sequence
   of actions in your code.

In summary, the Debugger extension in JIO transforms the testing and debugging process into a streamlined and
informative experience with minimal effort from developers. It empowers developers to gain deep insights into their
code's behavior without relying on external logging libraries or complex setups.

Here is the information that is printed out during the previous test:

```

Started JFR stream for 2 sg in SignupTests

event: eval, expression: JsObjExpPar[number_users], result: SUCCESS, output: 3
duration: 1727,208 µs, context: imrafaelmerino@gmail.com, thread: main, event-start-time: 2023-10-10T11:34:36.679769708+02:00

event: eval, expression: JsObjExpPar[addresses], result: SUCCESS, output: ["address1","address2"]
duration: 2553,292 µs, context: imrafaelmerino@gmail.com, thread: main, event-start-time: 2023-10-10T11:34:36.681656208+02:00

event: eval, expression: IfElseExp-predicate, result: SUCCESS, output: false
duration: 10,125 µs, context: imrafaelmerino@gmail.com, thread: main, event-start-time: 2023-10-10T11:34:36.68531675+02:00

event: eval, expression: PairExpSeq[1], result: SUCCESS, output: null
duration: 7,292 µs, context: imrafaelmerino@gmail.com, thread: main, event-start-time: 2023-10-10T11:34:36.686446041+02:00

event: eval, expression: PairExpSeq[2], result: SUCCESS, output: null
duration: 5,125 µs, context: imrafaelmerino@gmail.com, thread: main, event-start-time: 2023-10-10T11:34:36.686579083+02:00

event: eval, expression: PairExpSeq, result: SUCCESS, output: (null, null)
duration: 332,000 µs, context: imrafaelmerino@gmail.com, thread: main, event-start-time: 2023-10-10T11:34:36.686444458+02:00

event: eval, expression: IfElseExp-alternative, result: SUCCESS, output: id
duration: 352,875 µs, context: imrafaelmerino@gmail.com, thread: main, event-start-time: 2023-10-10T11:34:36.686435583+02:00

event: eval, expression: IfElseExp, result: SUCCESS, output: id
duration: 1485,333 µs, context: imrafaelmerino@gmail.com, thread: main, event-start-time: 2023-10-10T11:34:36.685313958+02:00

event: eval, expression: JsObjExpPar[id], result: SUCCESS, output: id
duration: 2555,583 µs, context: imrafaelmerino@gmail.com, thread: main, event-start-time: 2023-10-10T11:34:36.684258833+02:00

event: eval, expression: JsObjExpPar[timestamp], result: SUCCESS, output: 2023-10-10T09:34:36.686Z
duration: 290,042 µs, context: imrafaelmerino@gmail.com, thread: main, event-start-time: 2023-10-10T11:34:36.686827166+02:00

event: eval, expression: JsObjExpPar, result: SUCCESS, output: {"addresses":["address1","address2"],"number_users":3,"timestamp":"2023-10-10T09:34:36.686Z","id":"id"}
duration: 11,663 ms, context: imrafaelmerino@gmail.com, thread: main, event-start-time: 2023-10-10T11:34:36.679046958+02:00

```

where:

- **event**: This field indicates the type of event being traced. For example, "eval" signifies an evaluation event,
  where an expression or value is being evaluated.

- **expression**: The "expression" field specifies the expression or subexpression that is currently under evaluation.
  It gives you a clear indication of which part of your code is being processed.

- **result**: The "result" field shows the outcome of the expression evaluation, either SUCCESS OR FAILURE.
-
- **output**: The final value an expression is reduced to

- **duration**: The "duration" field indicates the time taken for the expression to be evaluated. It's helps in
  profiling and performance analysis.

- **context**: This field is especially valuable as it provides context-specific information. In your example, it's used
  to display the email value being processed. This context data is essential for understanding how different inputs
  affect the execution flow.

- **thread**: It shows the thread in which the event is occurring. Understanding which thread is executing each part of
  your code can be crucial for debugging concurrent or multi-threaded applications.

- **event-start-time**: This timestamp indicates when the event started, providing a chronological view of event
  execution.

These traces serve several important purposes:

1. **Debugging**: Traces help you trace the flow of execution through your code. If something goes wrong, you can
   identify the exact point where an issue occurred and examine the relevant context.

2. **Performance Profiling**: The "duration" field allows you to identify performance bottlenecks. You can see which
   expressions take the most time to execute and focus optimization efforts accordingly.

3. **Contextual Understanding**: The "context" field is particularly helpful for understanding how data flows through
   your code. It clarifies which values are being processed at each step.

4. **Event Ordering**: Timestamps help establish a chronological order of events, which is crucial for understanding the
   sequence of actions in your code.

In summary, these traces are like breadcrumbs that guide you through your code, making testing and debugging more
efficient and effective. They enable you to pinpoint issues, optimize performance, and gain a deeper understanding of
how your code behaves during testing.

In the previous example, you might have noticed that all the evaluations are performed by the main thread, even when
the `JsObjExp.par` operator was used. This behavior occurs because the IO effects returned by the lambdas are just
constants, and no Executor is specified. Even if you were to specify one, there are instances when the
CompletableFuture framework (which JIO relies on extensively) may not switch context between threads if it deems it
unnecessary.

But don't worry, we can introduce some random delays and leverage fibers to create a more realistic example. To do this,
let's use more elaborate stubs with the `StubSupplier` class from the `jio-test` library:

```code

@Test
public void test(){

        Gen<Duration> delayGen = IntGen.arbitrary(0, 200)
                                       .map(Duration::ofMillis);

        Lambda<Void, Integer> countUsers =
                nill -> StubSupplier.ofDelayedGen(IntGen.arbitrary(0, 100000),
                                                  delayGen
                                                 )
                                    .withExecutor(Executors.newVirtualThreadPerTaskExecutor())
                                    .get();

        Lambda<JsObj, String> persistMongo =
                user -> StubSupplier.ofDelayedGen(StrGen.alphabetic(20, 20),
                                                  delayGen
                                                 )
                                    .withExecutor(Executors.newVirtualThreadPerTaskExecutor())
                                    .get();

        Lambda<JsObj, Void> sendEmail =
                user -> StubSupplier.<Void>ofDelayedGen(Gen.cons(null),
                                                  delayGen
                                                 )
                                    .withExecutor(Executors.newVirtualThreadPerTaskExecutor())
                                    .get();

        Lambda<String, Boolean> existsInLDAP =
                email -> StubSupplier.ofDelayedGen(BoolGen.arbitrary(),
                                                   delayGen
                                                  )
                                     .withExecutor(Executors.newVirtualThreadPerTaskExecutor())
                                     .get();
        Lambda<JsObj, Void> persistLDAP =
                obj -> StubSupplier.<Void>ofDelayedGen(Gen.cons(null),
                                                 delayGen
                                                )
                                   .withExecutor(Executors.newVirtualThreadPerTaskExecutor())
                                   .get();

        Lambda<String, JsArray> normalizeAddresses =
                address -> StubSupplier.ofDelayedGen(JsArrayGen.ofN(JsStrGen.alphabetic(), 3),
                                                     delayGen
                                                    )
                                       .withExecutor(Executors.newVirtualThreadPerTaskExecutor())
                                       .get();    

```

In this updated example, we've introduced random delays to simulate more realistic scenarios. We're using
the `StubSupplier` class to generate delayed values and associating each lambda with an Executor that uses virtual
threads (`Executors.newVirtualThreadPerTaskExecutor()`). This approach ensures that evaluations occur asynchronously and
may involve multiple threads, providing a more realistic representation of concurrent operations:

```code
Started JFR stream for 2 sg in SignupTests

event: eval, expression: JsObjExpPar[timestamp], result: SUCCESS, output: 2023-10-10T09:41:27.520Z
duration: 861,417 µs, context: imrafaelmerino@gmail.com, thread: main, event-start-time: 2023-10-10T11:41:27.5204015+02:00

event: eval, expression: IfElseExp-predicate, result: SUCCESS, output: false
duration: 31,238 ms, context: imrafaelmerino@gmail.com, thread: virtual-40, event-start-time: 2023-10-10T11:41:27.549069042+02:00

event: eval, expression: JsObjExpPar[addresses], result: SUCCESS, output: ["u","d","f"]
duration: 77,856 ms, context: imrafaelmerino@gmail.com, thread: virtual-34, event-start-time: 2023-10-10T11:41:27.520052167+02:00

event: eval, expression: PairExpSeq[1], result: SUCCESS, output: null
duration: 111,441 ms, context: imrafaelmerino@gmail.com, thread: virtual-42, event-start-time: 2023-10-10T11:41:27.582186792+02:00

event: eval, expression: JsObjExpPar[number_users], result: SUCCESS, output: 32914
duration: 180,810 ms, context: imrafaelmerino@gmail.com, thread: virtual-32, event-start-time: 2023-10-10T11:41:27.513371334+02:00

event: eval, expression: PairExpSeq[2], result: SUCCESS, output: null
duration: 141,523 ms, context: imrafaelmerino@gmail.com, thread: virtual-44, event-start-time: 2023-10-10T11:41:27.693663125+02:00

event: eval, expression: PairExpSeq, result: SUCCESS, output: (null, null)
duration: 253,256 ms, context: imrafaelmerino@gmail.com, thread: virtual-44, event-start-time: 2023-10-10T11:41:27.582184959+02:00

event: eval, expression: IfElseExp-alternative, result: SUCCESS, output: JOYfTGftYQXYNFGROgNp
duration: 253,302 ms, context: imrafaelmerino@gmail.com, thread: virtual-44, event-start-time: 2023-10-10T11:41:27.582176584+02:00

event: eval, expression: IfElseExp, result: SUCCESS, output: JOYfTGftYQXYNFGROgNp
duration: 286,460 ms, context: imrafaelmerino@gmail.com, thread: virtual-44, event-start-time: 2023-10-10T11:41:27.549066834+02:00

event: eval, expression: JsObjExpPar[id], result: SUCCESS, output: JOYfTGftYQXYNFGROgNp
duration: 315,203 ms, context: imrafaelmerino@gmail.com, thread: virtual-44, event-start-time: 2023-10-10T11:41:27.520363459+02:00

event: eval, expression: JsObjExpPar, result: SUCCESS, output: {"addresses":["u","d","f"],"number_users":32914,"timestamp":"2023-10-10T09:41:27.520Z","id":"JOYfTGftYQXYNFGROgNp"}
duration: 331,995 ms, context: imrafaelmerino@gmail.com, thread: virtual-44, event-start-time: 2023-10-10T11:41:27.512854042+02:00


```

To enhance the resilience of our code, let's introduce some retry logic for the countUsers lambda. We want to allow up
to three retries and, in case of failure, return -1.

``` code
                                    
        // let's add up to three retries 
        countUsers.apply(null)
                  .debug(new EventBuilder<>("count_users", email)) 
                  .retry(RetryPolicies.limitRetries(3))
                  .recover(e -> -1)
                  .map(JsInt::of),                            
          
```

In this code:

- The `countUsers` lambda is executed, and for each execution, the `debug` method creates an event. The `EventBuilder`
  allows you to specify the name of the expression being evaluated ("count_users") and the context ("email").
  This helps customize the events for debugging purposes.

- The `retry` method is used to introduce retry logic. In case of failure, `countUser` will be retried up to three
  times.

- The `recover` method specifies what value to return in case of a failure. In this case, it returns -1.

And to test it, let's change the stub for the `countUser` lambda:

```code

        //let's change the delay of every stub to 1 sec, for the sake of clarity
        Gen<Duration> delayGen = Gen.cons(1).map(Duration::ofSeconds);
        
        Lambda<Void, Integer> countUsers =
                nill -> StubSupplier.ofDelayedIOGen(Gens.seq(n -> n <= 4 ?
                                                                     IO.fail(new RuntimeException(n + "")) :
                                                                     IO.succeed(n)
                                                            ),
                                                    delayGen)
                                    .withExecutor(Executors.newVirtualThreadPerTaskExecutor())
                                    .get();

```

In this code:

- The `Gen.cons(1).map(Duration::ofSeconds)` defines a generator `delayGen` that provides a constant delay of 1 second.

- The `countUsers` lambda is defined to use the `StubSupplier` with a sequence generator (`Gens.seq`) that allows you to
  choose different values for each call. In this case the firs four calls triggers a failure, which is treated as a
  value
  that can be returned.

This setup allows you to test and observe the retry logic in action:

```code

Started JFR stream for 5 sg in SignupTests

event: eval, expression: JsObjExpPar[timestamp], result: SUCCESS, output: 2023-10-10T11:32:31.361Z
duration: 1183,875 µs, context: imrafaelmerino@gmail.com, thread: main, event-start-time: 2023-10-10T13:32:31.361439584+02:00

event: eval, expression: count_users, result: FAILURE, output: java.lang.RuntimeException:1
duration: 1,008 sg, context: imrafaelmerino@gmail.com, thread: virtual-32, event-start-time: 2023-10-10T13:32:31.358466292+02:00

event: eval, expression: JsObjExpPar[addresses], result: SUCCESS, output: ["H","E","N"]
duration: 1,009 sg, context: imrafaelmerino@gmail.com, thread: virtual-34, event-start-time: 2023-10-10T13:32:31.361287042+02:00

event: eval, expression: IfElseExp-predicate, result: SUCCESS, output: true
duration: 1,005 sg, context: imrafaelmerino@gmail.com, thread: virtual-45, event-start-time: 2023-10-10T13:32:32.36795925+02:00

event: eval, expression: count_users, result: FAILURE, output: java.lang.RuntimeException:2
duration: 1,006 sg, context: imrafaelmerino@gmail.com, thread: virtual-32, event-start-time: 2023-10-10T13:32:32.366728167+02:00

event: eval, expression: IfElseExp-consequence, result: SUCCESS, output: fNUAsXflwFYPNaRnMCfN
duration: 16,083 µs, context: imrafaelmerino@gmail.com, thread: virtual-45, event-start-time: 2023-10-10T13:32:33.372809459+02:00

event: eval, expression: IfElseExp, result: SUCCESS, output: fNUAsXflwFYPNaRnMCfN
duration: 1,005 sg, context: imrafaelmerino@gmail.com, thread: virtual-45, event-start-time: 2023-10-10T13:32:32.36795675+02:00

event: eval, expression: JsObjExpPar[id], result: SUCCESS, output: fNUAsXflwFYPNaRnMCfN
duration: 2,012 sg, context: imrafaelmerino@gmail.com, thread: virtual-45, event-start-time: 2023-10-10T13:32:31.361416292+02:00

event: eval, expression: count_users, result: FAILURE, output: java.lang.RuntimeException:3
duration: 1,001 sg, context: imrafaelmerino@gmail.com, thread: not recorded, event-start-time: 2023-10-10T13:32:33.372799375+02:00

event: eval, expression: count_users, result: FAILURE, output: java.lang.RuntimeException:4
duration: 1,006 sg, context: imrafaelmerino@gmail.com, thread: virtual-47, event-start-time: 2023-10-10T13:32:34.374127542+02:00

event: eval, expression: JsObjExpPar[number_users], result: SUCCESS, output: -1
duration: 4,025 sg, context: imrafaelmerino@gmail.com, thread: virtual-47, event-start-time: 2023-10-10T13:32:31.356712292+02:00

event: eval, expression: JsObjExpPar, result: SUCCESS, output: {"addresses":["H","E","N"],"number_users":-1,"timestamp":"2023-10-10T11:32:31.361Z","id":"fNUAsXflwFYPNaRnMCfN"}
duration: 4,036 sg, context: imrafaelmerino@gmail.com, thread: virtual-47, event-start-time: 2023-10-10T13:32:31.355501792+02:00

```

Key points:

1. The `retry` method can accept a predicate, allowing you to specify which errors should trigger a retry. This
   fine-grained control is valuable for handling specific error scenarios.

2. Retry policies in JIO are composable, making it easy to build complex retry strategies. For example, you can create a
   policy like this:

   ```code
   RetryPolicies.constantDelay(Duration.ofMillis(50))
                .limitRetriesByCumulativeDelay(Duration.ofMillis(300))
   ```

   This policy specifies a constant delay of 50 milliseconds between retries and limits retries by a cumulative delay of
   300 milliseconds.

3. JIO excels at scalability. Even when dealing with complex logic, it maintains simplicity in the expressions you
   write, avoiding the complexities of callback hell or other frameworks.

4. JIO offers a high signal-to-noise ratio. It reduces verbosity, allowing you to express complex operations succinctly
   and clearly.

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
  
IO<O> debug(final EventBuilder<O> builder);  
  
```  

You can call debug() without providing an EventBuilder, and JIO will use a default event builder. This simplifies the  
debugging process for common use cases.

### Debugging Expressions

JIO's debugging capabilities extend beyond individual effects. You can attach a debug mechanism to each operand of an  
expression using the `debugEach` method. This allows you to monitor and log the execution of each operand
individually.  
The provided `EventBuilder` or a descriptive context can be used to customize the debug events for each operand.

```code  
  

Exp<O> debugEach(final EventBuilder<O> builder);  
  
Exp<O> debugEach(final String context);  
  
```  

By using `debugEach`, you can gain insights into the behavior of complex expressions and identify any issues or  
bottlenecks that may arise during execution. All the subexpressions and the final result will be recorded with the
same context, making it easier to relate them and analyze their interactions.  
JIO's logging and JFR integration features provide valuable tools for contextual logging, debugging, profiling, and  
monitoring your functional effects and expressions, helping you build robust and reliable applications.



## <a name="Installation"><a/> Installation

```code  
  
<dependency>  
    <groupId>com.github.imrafaelmerino</groupId>  
    <artifactId>jio-exp</artifactId>  
    <version>1.0.0-RC1</version>  
</dependency>  
  
```  

  
---  

## <a name="Requirements and dependencies"><a/> Requirements and dependencies

- Java 17 or greater
- [json-values](https://github.com/imrafaelmerino/json-values)