<img src="logo/package_twitter_itsywb76/black/full/coverphoto/black_logo_white_background.png" alt="logo"/>

- [Code wins arguments](#cwa)
- [Introduction](#Introduction)
- [jio-exp](#jio-exp)
  - [Creating effects](#creating-effects)
  - [Lambdas](#lambdas)
  - [Operations with effects](#operations-with-effects)
  - [Expressions](#expressions)
  - [Clocks](#Clocks)
  - [Debugging and JFR integration](#Debugging-and-JFR-integration)
  - [Installation](#exp-installation)
- [jio-http](#jio-http)
  - [HTTP server](#httpserver)
  - [HTTP client](#httpclient)
  - [OAUTH HTTP client](#oauth)
  - [Installation](#http-installation)
- [jio-test](#jio-test)
  - [Junit integration](#junit)
  - [Stubs](#stubs)
    - [IO stubs](#iostubs)
    - [Clock stubs](#clockstubs)
    - [Http Server Stubs](#httpserverstubs)
  - [Property based testing](#pbt)
  - [Installation](#test-installation)
- [jio-mongodb](#jio-mongodb)
  - [MongoLambda](#monglambda)
  - [API](#jio-mongodb-gs)
    - [Find Operations](#find-operations)
    - [Insert Operations](#insert-operations)
    - [Delete Operations](#delete-operations)
    - [Update and Replace Operations](#update-and-replace-operations)
    - [Count](#count)
    - [FindAndXXX operations](#findoneandxxx-operations)
    - [Aggregate](#aggregate)
    - [Watcher](#watcher)
    - [Specifying an Executor](#mongo-executors)
    - [Configuring options](#mongo-options)
  - [Transactions](#transactions)
  - [Common exceptions](#common-exceptions)
  - [Debugging and JFR integration](#mongo-Debugging-and-JFR-integration)
  - [Installation](#mongo-installation)
- [jio-console](#jio-console)
- [jio-jdbc](#jio-jdbc)

## <a name="cwa"><a/> Code wins arguments

I think the age-old "Hello world" example has outlived its usefulness. While it once served as a
foundational teaching tool, its simplicity no longer suffices in today's world. In the current
landscape, where real-world scenarios are markedly more intricate, I present a "Hello world" example
that truly mirrors the complexity of modern development.

### Signup Service specification

Let's jump into the implementation of a signup service with the following requirements:

1. The signup service takes a JSON input containing at least two fields: email and address, both
   expected as strings. The service's first step is to validate and standardize the address using
   the Google Geocode API. The results obtained from Google are then presented to the frontend for
   user selection or rejection.
2. In addition to address validation, the service stores the client's information in a MongoDB
   database. The MongoDB identifier returned becomes the client identifier, which must be sent back
   to the frontend. If the client is successfully saved in the database and the user doesn't exist
   in an LDAP system, two additional actions occur:
   - The user is sent to the LDAP service.
   - If the previous operation succeeds, an activation email is sent to the user.
3. The signup service also provides information about the total number of existing clients in the
   MongoDB database. This information can be utilized by the frontend to display a welcoming message
   to the user, such as "You're the user number 3000!" If an error occurs the service returns -1,
   and the frontend will not display the message.
4. Crucially, the signup service is designed to perform all these operations in parallel. This
   includes the request to Google for address validation and the MongoDB operations, which encompass
   both data persistence and counting.
5. The signup service also returns a timestamp indicating the instant the service started composing
   the response. This timestamp can be used by the frontend to show the user the exact date and time
   they joined.

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

The `SignupService` orchestrates all the operations with elegance and efficiency. This service is
constructed with a set of [lambdas](#lambdas), where a lambda is essentially a function that takes
an input and produces an output. Unlike traditional functions, lambdas don't throw exceptions;
instead, they gracefully return exceptions as regular values.

```java
import jio.*;
import jio.time.Clock;
import jsonvalues.*;

import java.time.Instant;

import static java.util.Objects.requireNonNull;

public class SignupService implements Lambda<JsObj, JsObj> {

  Lambda<JsObj, Void> persistLDAP;
  Lambda<String, JsArray> normalizeAddress;
  Supplier<IO<Integer>> countUsers;
  Lambda<JsObj, String> persistMongo;
  Lambda<JsObj, Void> sendEmail;
  Lambda<String, Boolean> existsInLDAP;
  Clock clock;

  //constructor

  @Override
  public IO<JsObj> apply(JsObj user) {

    String email = user.getStr("email");
    String address = user.getStr("address");
    String context = "signup";

    Lambda<String, String> LDAPFlow =
        id -> IfElseExp.<String>predicate(existsInLDAP.apply(email))
                       .consequence(() -> IO.succeed(id))
                       .alternative(() -> PairExp.seq(persistLDAP.apply(user),
                                                      sendEmail.apply(user)
                                                     )
                                                 .debugEach(context)
                                                 .map(n -> id)
                                   )
                       .debugEach(context);

    return JsObjExp.par("number_users", countUsers.get()
                                                  .recover(exc -> -1)
                                                  .map(JsInt::of),

                        "id", persistMongo.then(LDAPFlow)
                                          .apply(user)
                                          .map(JsStr::of),

                        "addresses", normalizeAddress.apply(address),

                        "timestamp", IO.lazy(clock)
                                       .map(ms -> JsInstant.of(Instant.ofEpochMilli(ms)))
                       )
                   .debugEach(context);
  }
}

```

Noteworthy points:

- **Clocks**: In modern programming, managing time is critical, and using `Instant.now()` directly
  throughout your code introduces side effects. I advocate using clocks, represented by the `clock`
  instance in our `SignupService`. They provide better control and makes your code more predictable.

- **JsObjExp**: The `JsObjExp` expression is highly expressive. It allows us to define the structure
  of the resulting JSON object in a clear and declarative manner. In our code, we use it to
  construct a JSON object with multiple key-value pairs, each representing a specific piece of
  information (`"number_users"`, `"id"`, `"addresses"`, `"timestamp"`, etc.). This approach
  simplifies the creation of complex JSON structures and enhances code readability.

- Error handling is handled gracefully with the `recover` functions, providing alternative values
  (e.g., -1 for `countUsers`) in case of errors.

- **IfElseExp**: The `IfElseExp` expression is a clear and concise way to handle conditional logic.
  It enables us to specify the consequence and alternative branches based on a predicate
  (`existsInLDAP.apply(email)` in this case). This expressiveness makes it evident that if the user
  exists in LDAP, we succeed with an ID, otherwise, we perform a sequence of operations using
  `PairExp`. It enhances the readability of the code, making it easy to understand the branching
  logic.

- **PairExp**: The `PairExp` expression streamlines the execution of two effects, either
  sequentially or in parallel, and then combines their results into a pair. In this scenario, we
  utilize `PairExp.seq` to execute the `persistLDAP` and `sendEmail` operations sequentially.
  However, it's essential to emphasize that in this particular example, our primary concern is the
  successful completion of both operations. Therefore, in the absence of any failures, the result
  will be a pair containing two `null` values: (null, null), as both operations return `Void`.

- **debugEach**: Debugging plays a pivotal role in software development, and real-world applications
  often handle a multitude of messages from various users and requests. When issues arise,
  identifying which log events are pertinent to the problem can be challenging, particularly in
  high-traffic scenarios. JIO streamlines the debugging process and enhances contextual logging
  through its `debug` and `debugEach` methods.

- **JFR (Java Flight Recorder)**: JIO leverages JFR for logging and debugging purposes. This choice
  offers several advantages. First, it's Java-native, which means it seamlessly integrates with the
  Java ecosystem, ensuring compatibility and performance. Second, it avoids the complexities and
  potential conflicts associated with using external logging libraries, of which there are many in
  the Java landscape. By relying on JFR, we maintain a lightweight and efficient approach to logging
  that is both reliable and highly effective.

- Last but not least, the backbone of JIO is the `IO` class that we'll explore in detail in the next
  section.

### Testing the Signup Service with JIO

JIO offers an elegant and efficient approach to testing. It eliminates the need for external
libraries like Mockito, making your testing experience smoother and more expressive. Since Lambdas
are just functions, you can implement them in your test class, directly. This approach enables you
to tailor the behavior of each lambda to your specific test scenario, making your tests highly
adaptable and expressive:

```java


public class SignupTests {

  @RegisterExtension
  static Debugger debugger = Debugger.of(Duration.ofSeconds(2));

  @Test
  public void test() {

    Lambda<JsObj, Void> persistLDAP = _ -> IO.NULL();

    Lambda<String, JsArray> normalizeAddress =
        _ -> IO.succeed(JsArray.of("address1", "address2"));

    Supplier<IO<Integer>> countUsers = () -> IO.succeed(3);

    Lambda<JsObj, String> persistMongo = _ -> IO.succeed("id");

    Lambda<JsObj, Void> sendEmail = _ -> IO.NULL();

    Lambda<String, Boolean> existsInLDAP = _ -> IO.FALSE;

    JsObj user = JsObj.of("email", JsStr.of("imrafaelmerino@gmail.com"),
                          "address", JsStr.of("Elm's Street")
                         );

    JsObj resp = new SignupService(persistLDAP,
                                   normalizeAddress,
                                   countUsers,
                                   persistMongo,
                                   sendEmail,
                                   existsInLDAP,
                                   Clock.realTime
    )
        .apply(user)
        .result();

    Assertions.assertTrue(resp.containsKey("number_users"));

  }

}

```

### Debugging with the Debugger Extension

When it comes to debugging your code during testing, having access to detailed information is
invaluable. JIO's Debugger extension simplifies this process by creating an event stream for a
specified duration and printing all the events sent to the Java Flight Recorder (JFR) system during
that period.

Here's a breakdown of how it works:

1. **Debugger Extension Registration**: In your test class, you register the Debugger JUnit
   extension using the `@RegisterExtension` annotation. You specify the duration for which the
   debugger captures events.

2. **Using `debug` and `debugEach`**: Within your code, you utilize the `debug` and `debugEach`
   methods provided by JIO. These methods allow you to send events to the JFR system after a value
   or expression is evaluated.

3. **Event Printing**: During the execution of the test for the specified duration, the Debugger
   extension prints out all the events that were sent to the JFR system. These events include
   information about the expressions being evaluated, their results, execution durations, contextual
   data, and more.

4. **Stream Ordering**: Importantly, the event stream is ordered. Events are printed in the order in
   which they occurred, providing a clear chronological view of your code's execution.

5. **Pinpointing Bugs and Issues**: With the event stream and detailed logs in hand, you can easily
   pinpoint any bugs, unexpected behavior, or performance bottlenecks.

In summary, the Debugger extension in JIO transforms the testing and debugging process into a
streamlined and informative experience with minimal effort from developers. It empowers developers
to gain deep insights into their code's behavior without relying on external logging libraries or
complex setups.

Find below all the events that are printed out during the execution of the previous JUnit test.

```text

Started JFR stream for 2,000 sg in SignupTests

------ eval-exp --------
|  Expression: count_number_users
|  Result: SUCCESS
|  Duration: 69,833 µs
|  Output: 3
|  Context: signup
|  Thread: main
|  Event Start Time: 2024-02-13T10:08:09.920563792+01:00
-------------------------

------ eval-exp --------
|  Expression: JsObjExpPar[number_users]
|  Result: SUCCESS
|  Duration: 1,418 ms
|  Output: 3
|  Context: signup
|  Thread: main
|  Event Start Time: 2024-02-13T10:08:09.91973025+01:00
-------------------------

------ eval-exp --------
|  Expression: JsObjExpPar[addresses]
|  Result: SUCCESS
|  Duration: 4,583 µs
|  Output: ["address1","address2"]
|  Context: signup
|  Thread: main
|  Event Start Time: 2024-02-13T10:08:09.921166292+01:00
-------------------------

------ eval-exp --------
|  Expression: IfElseExp-predicate
|  Result: SUCCESS
|  Duration: 5,958 µs
|  Output: false
|  Context: signup
|  Thread: main
|  Event Start Time: 2024-02-13T10:08:09.924032792+01:00
-------------------------

------ eval-exp --------
|  Expression: PairExpSeq[1]
|  Result: SUCCESS
|  Duration: 4,709 µs
|  Output: null
|  Context: signup
|  Thread: main
|  Event Start Time: 2024-02-13T10:08:09.924848208+01:00
-------------------------

------ eval-exp --------
|  Expression: PairExpSeq[2]
|  Result: SUCCESS
|  Duration: 4,291 µs
|  Output: null
|  Context: signup
|  Thread: main
|  Event Start Time: 2024-02-13T10:08:09.924969417+01:00
-------------------------

------ eval-exp --------
|  Expression: PairExpSeq
|  Result: SUCCESS
|  Duration: 284,875 µs
|  Output: (null, null)
|  Context: signup
|  Thread: main
|  Event Start Time: 2024-02-13T10:08:09.924846917+01:00
-------------------------

------ eval-exp --------
|  Expression: IfElseExp-alternative
|  Result: SUCCESS
|  Duration: 2,744 ms
|  Output: id
|  Context: signup
|  Thread: main
|  Event Start Time: 2024-02-13T10:08:09.924842+01:00
-------------------------

------ eval-exp --------
|  Expression: IfElseExp
|  Result: SUCCESS
|  Duration: 3,568 ms
|  Output: id
|  Context: signup
|  Thread: main
|  Event Start Time: 2024-02-13T10:08:09.924030958+01:00
-------------------------

------ eval-exp --------
|  Expression: JsObjExpPar[id]
|  Result: SUCCESS
|  Duration: 4,058 ms
|  Output: id
|  Context: signup
|  Thread: main
|  Event Start Time: 2024-02-13T10:08:09.923546958+01:00
-------------------------

------ eval-exp --------
|  Expression: JsObjExpPar[timestamp]
|  Result: SUCCESS
|  Duration: 219,208 µs
|  Output: 2024-02-13T09:08:09.927Z
|  Context: signup
|  Thread: main
|  Event Start Time: 2024-02-13T10:08:09.927616125+01:00
-------------------------

------ eval-exp --------
|  Expression: JsObjExpPar
|  Result: SUCCESS
|  Duration: 8,925 ms
|  Output: {"addresses":["address1","address2"],"number_users":3,"timestamp":"2024-02-13T09:08:09.927Z","id":"id"}
|  Context: signup
|  Thread: main
|  Event Start Time: 2024-02-13T10:08:09.919238167+01:00
-------------------------

```

The events printed out are self-explanatory.

In summary, these traces are like breadcrumbs that guide you through your code, making testing and
debugging more efficient and effective. They enable you to pinpoint issues, optimize performance,
and gain a deeper understanding of how your code behaves during testing.

In the previous example, you may have observed that all evaluations were performed by the main
thread. This is because the IO effects returned by the lambdas were essentially constants, and no
specific `Executor` was defined. Even if an `Executor` were specified, there are cases where the
CompletableFuture framework, heavily relied upon by JIO, may choose not to switch contexts between
threads if it deems it unnecessary.

However, you can introduce random delays and leverage virtual threads to create a more realistic
example. To achieve this, more complex stubs are used from the `jio-test` library through the
`StubBuilder` class. These stubs allow you to specify generators for their creation, ensuring
different values are returned every time. Here's how you can utilize them:

```java

@Test
public void test(){

    Gen<Duration> delayGen = IntGen.arbitrary(0, 200)
                                   .map(Duration::ofMillis);

    Supplier<IO<Integer>> countUsers =
           () -> StubBuilder.ofSucGen(IntGen.arbitrary(0, 100000))
                            .withDelays(delayGen)
                            .withExecutor(Executors.newVirtualThreadPerTaskExecutor())
                            .get();

    Lambda<JsObj, String> persistMongo =
            _ -> StubBuilder.ofSucGen(StrGen.alphabetic(20, 20))
                            .withDelays(delayGen)
                            .withExecutor(Executors.newVirtualThreadPerTaskExecutor())
                            .get();

    Lambda<JsObj, Void> sendEmail =
            _ -> StubBuilder.<Void>ofSucGen(Gen.cons(null))
                            .withDelays(delayGen)
                            .withExecutor(Executors.newVirtualThreadPerTaskExecutor())
                            .get();

    Lambda<String, Boolean> existsInLDAP =
            _ -> StubBuilder.ofSucGen(BoolGen.arbitrary())
                            .withDelays(delayGen)
                            .withExecutor(Executors.newVirtualThreadPerTaskExecutor())
                            .get();

    Lambda<JsObj, Void> persistLDAP =
            _ -> StubBuilder.<Void>ofSucGen(Gen.cons(null))
                            .withDelays(delayGen)
                            .withExecutor(Executors.newVirtualThreadPerTaskExecutor())
                            .get();

    Lambda<String, JsArray> normalizeAddresses =
            _ -> StubBuilder.ofSucGen(JsArrayGen.ofN(JsStrGen.alphabetic(), 3))
                            .withDelays(delayGen)
                            .withExecutor(Executors.newVirtualThreadPerTaskExecutor())
                            .get();
}
```

These `StubBuilder` instances are essentially builders that create IO stubs. They allow you to
introduce variability and randomness into your tests, making them more realistic and ensuring your
code can handle different scenarios effectively. I recommend you take a look at
[jio-test](#jio-test) and [property-based-testing](#pbt).

Using these stubs, the following events were printed out:

```text
Started JFR stream for 2,000 sg in SignupTests

------ eval-exp --------
|  Expression: JsObjExpPar[timestamp]
|  Result: SUCCESS
|  Duration: 293,209 µs
|  Output: 2024-02-13T09:18:21.071Z
|  Context: signup
|  Thread: main
|  Event Start Time: 2024-02-13T10:18:21.071499125+01:00
-------------------------

------ eval-exp --------
|  Expression: count_number_users
|  Result: SUCCESS
|  Duration: 65,372 ms
|  Output: 32634
|  Context: signup
|  Thread: virtual--1
|  Event Start Time: 2024-02-13T10:18:21.066073417+01:00
-------------------------

------ eval-exp --------
|  Expression: JsObjExpPar[number_users]
|  Result: SUCCESS
|  Duration: 66,663 ms
|  Output: 32634
|  Context: signup
|  Thread: virtual--1
|  Event Start Time: 2024-02-13T10:18:21.065248375+01:00
-------------------------

------ eval-exp --------
|  Expression: JsObjExpPar[addresses]
|  Result: SUCCESS
|  Duration: 116,944 ms
|  Output: ["m","n","g"]
|  Context: signup
|  Thread: virtual--1
|  Event Start Time: 2024-02-13T10:18:21.071124667+01:00
-------------------------

------ eval-exp --------
|  Expression: IfElseExp-predicate
|  Result: SUCCESS
|  Duration: 37,233 ms
|  Output: true
|  Context: signup
|  Thread: virtual--1
|  Event Start Time: 2024-02-13T10:18:21.218791959+01:00
-------------------------

------ eval-exp --------
|  Expression: IfElseExp-consequence
|  Result: SUCCESS
|  Duration: 9,667 µs
|  Output: QREuMrvmtunCvhbZxykT
|  Context: signup
|  Thread: virtual--1
|  Event Start Time: 2024-02-13T10:18:21.256122125+01:00
-------------------------

------ eval-exp --------
|  Expression: IfElseExp
|  Result: SUCCESS
|  Duration: 37,386 ms
|  Output: QREuMrvmtunCvhbZxykT
|  Context: signup
|  Thread: virtual--1
|  Event Start Time: 2024-02-13T10:18:21.218788042+01:00
-------------------------

------ eval-exp --------
|  Expression: JsObjExpPar[id]
|  Result: SUCCESS
|  Duration: 184,728 ms
|  Output: QREuMrvmtunCvhbZxykT
|  Context: signup
|  Thread: virtual--1
|  Event Start Time: 2024-02-13T10:18:21.071473417+01:00
-------------------------

------ eval-exp --------
|  Expression: JsObjExpPar
|  Result: SUCCESS
|  Duration: 191,692 ms
|  Output: {"addresses":["m","n","g"],"number_users":32634,"timestamp":"2024-02-13T09:18:21.071Z","id":"QREuMrvmtunCvhbZxykT"}
|  Context: signup
|  Thread: virtual--1
|  Event Start Time: 2024-02-13T10:18:21.064957792+01:00
-------------------------

```

To enhance the resilience of our code, let's introduce some retry logic for the `countUsers`
supplier. We want to allow up to three retries:

```code
        // let's add up to three retries
        countUsers.get()
                  .debug(EventBuilder.of("count_users", context))
                  .retry(RetryPolicies.limitRetries(3))
                  .recover(_ -> -1)
```

In this code:

- The `countUsers` supplier is executed, and for each execution, the `debug` method creates an
  event. The `EventBuilder` allows you to specify the name of the expression being evaluated
  ("count_users") and the context. This helps customize the events sent to the JFR system.

- The `retry` method is used to introduce retry logic. In case of failure, `countUser` will be
  retried up to three times.

- The `recover` method specifies what value to return in case of a failure.

And to test it, let's change the stub for the `countUser` supplier:

```java

//let's change the delay of every stub to 1 sec, for the sake of clarity
Gen<Duration> delayGen = Gen.cons(1).map(Duration::ofSeconds);

Supplier<IO<Integer>> countUsers =
       _ -> StubBuilder.ofGen(Gen.seq(n -> n <= 4 ?
                                           IO.fail(new RuntimeException(n + "")) :
                                           IO.succeed(n)
                                     )
                              )
                       .withDelays(delayGen)
                       .withExecutor(Executors.newVirtualThreadPerTaskExecutor())
                       .get();

```

In this code:

- The generator `delayGen` provides a constant delay of 1 second.

- The `countUsers` supplier is defined to use the `StubBuilder` with a sequence generator
  (`Gen.seq`) that allows you to choose different values for each call. In this case the first four
  calls triggers a failure, which is treated as a value that can be returned.

This setup allows you to test and observe the retry logic in action:

```text
Started JFR stream for 10,000 sg in SignupTests

------ eval-exp --------
|  Expression: JsObjExpPar[timestamp]
|  Result: SUCCESS
|  Duration: 281,583 µs
|  Output: 2024-02-13T09:30:42.681Z
|  Context: signup
|  Thread: main
|  Event Start Time: 2024-02-13T10:30:42.681326792+01:00
-------------------------

------ eval-exp --------
|  Expression: count_number_users
|  Result: FAILURE
|  Duration: 1,010 sg
|  Output: java.lang.RuntimeException: 1
|  Context: signup
|  Thread: virtual--1
|  Event Start Time: 2024-02-13T10:30:42.678849959+01:00
-------------------------

------ eval-exp --------
|  Expression: JsObjExpPar[addresses]
|  Result: SUCCESS
|  Duration: 1,007 sg
|  Output: ["l","e","B"]
|  Context: signup
|  Thread: virtual--1
|  Event Start Time: 2024-02-13T10:30:42.681127792+01:00
-------------------------

------ eval-exp --------
|  Expression: IfElseExp-predicate
|  Result: SUCCESS
|  Duration: 1,006 sg
|  Output: false
|  Context: signup
|  Thread: virtual--1
|  Event Start Time: 2024-02-13T10:30:43.6904075+01:00
-------------------------

------ eval-exp --------
|  Expression: count_number_users
|  Result: FAILURE
|  Duration: 1,006 sg
|  Output: java.lang.RuntimeException: 2
|  Context: signup
|  Thread: virtual--1
|  Event Start Time: 2024-02-13T10:30:43.690528334+01:00
-------------------------

------ eval-exp --------
|  Expression: count_number_users
|  Result: FAILURE
|  Duration: 1,004 sg
|  Output: java.lang.RuntimeException: 3
|  Context: signup
|  Thread: not recorded
|  Event Start Time: 2024-02-13T10:30:44.696579667+01:00
-------------------------

------ eval-exp --------
|  Expression: PairExpSeq[1]
|  Result: SUCCESS
|  Duration: 1,001 sg
|  Output: null
|  Context: signup
|  Thread: not recorded
|  Event Start Time: 2024-02-13T10:30:44.702844292+01:00
-------------------------

------ eval-exp --------
|  Expression: PairExpSeq[2]
|  Result: SUCCESS
|  Duration: 1,003 sg
|  Output: null
|  Context: signup
|  Thread: virtual--1
|  Event Start Time: 2024-02-13T10:30:45.704042667+01:00
-------------------------

------ eval-exp --------
|  Expression: count_number_users
|  Result: FAILURE
|  Duration: 1,006 sg
|  Output: java.lang.RuntimeException: 4
|  Context: signup
|  Thread: virtual--1
|  Event Start Time: 2024-02-13T10:30:45.700588667+01:00
-------------------------

------ eval-exp --------
|  Expression: PairExpSeq
|  Result: SUCCESS
|  Duration: 2,004 sg
|  Output: (null, null)
|  Context: signup
|  Thread: virtual--1
|  Event Start Time: 2024-02-13T10:30:44.702836584+01:00
-------------------------

------ eval-exp --------
|  Expression: JsObjExpPar[number_users]
|  Result: SUCCESS
|  Duration: 4,030 sg
|  Output: -1
|  Context: signup
|  Thread: virtual--1
|  Event Start Time: 2024-02-13T10:30:42.67800425+01:00
-------------------------

------ eval-exp --------
|  Expression: IfElseExp-alternative
|  Result: SUCCESS
|  Duration: 2,015 sg
|  Output: adnhvqDPCgmEINgqiteV
|  Context: signup
|  Thread: virtual--1
|  Event Start Time: 2024-02-13T10:30:44.702804125+01:00
-------------------------

------ eval-exp --------
|  Expression: IfElseExp
|  Result: SUCCESS
|  Duration: 3,028 sg
|  Output: adnhvqDPCgmEINgqiteV
|  Context: signup
|  Thread: virtual--1
|  Event Start Time: 2024-02-13T10:30:43.690404584+01:00
-------------------------

------ eval-exp --------
|  Expression: JsObjExpPar[id]
|  Result: SUCCESS
|  Duration: 4,037 sg
|  Output: adnhvqDPCgmEINgqiteV
|  Context: signup
|  Thread: virtual--1
|  Event Start Time: 2024-02-13T10:30:42.681302584+01:00
-------------------------

------ eval-exp --------
|  Expression: JsObjExpPar
|  Result: SUCCESS
|  Duration: 4,042 sg
|  Output: {"addresses":["l","e","B"],"number_users":-1,"timestamp":"2024-02-13T09:30:42.681Z","id":"adnhvqDPCgmEINgqiteV"}
|  Context: signup
|  Thread: virtual--1
|  Event Start Time: 2024-02-13T10:30:42.676874584+01:00
-------------------------

```

Key points:

1. After the first failure and three retries, the value -1 from the `recover` function is returned

2. The `retry` method can accept a predicate, allowing you to specify which errors should trigger a
   retry. This fine-grained control is valuable for handling specific error scenarios.

3. Retry policies in JIO are composable, making it easy to build complex retry strategies. For
   example, you can create a policy like this:

   ```code
   RetryPolicies.constantDelay(Duration.ofMillis(50))
                .limitRetriesByCumulativeDelay(Duration.ofMillis(300))
   ```

   This policy specifies a constant delay of 50 milliseconds between retries and limits retries by a
   cumulative delay of 300 milliseconds.

4. JIO excels at scalability. Even when dealing with complex logic, it maintains simplicity in the
   expressions you write, avoiding the complexities of callback hell or other frameworks.

5. JIO offers a high signal-to-noise ratio. It reduces verbosity, allowing you to express complex
   operations succinctly and clearly.

## <a name="Introduction"><a/> Introduction

Functional Programming is all about working with pure functions and values. That's all. **However,
where FP especially shines is dealing with effects**.

But what is an effect?

First take a look at the following piece of code:

```code

int a = sum(1,2) + 3;

int b = sum(1,2) + 1;

```

As far as the function `sum` is **pure**, you can refactor the previous piece of code and call the
function just once:

```code

int c = sum(1,2);

int a = c + 3;

int b = c + 1;

```

Both programs are equivalents and wherever you see `sum(1,2)` you can replace it by `c` without
changing the meaning of the program at all.

An effect, on the other hand, is something you can't call more than once unless you intended to:

```code

Instant a = Instant.now().plus(Period.ofDays(1));

Instant b = Instant.now().plus(Period.ofDays(2));

```

Because _now()_ returns a different value each time it's called and therefore is not a pure
function, the following refactoring would change completely the meaning of the program (and still
your favourite IDE suggests you to do it at times!):

```code

Instant now = Instant.now();

Instant a = now.plus(Period.ofDays(1));

Instant b = now.plus(Period.ofDays(2));

```

Here's when laziness comes into play. Since Java 8, we have suppliers. They are indispensable to do
FP in Java. The following piece of code is equivalent to the original one without changing the
meaning of the program:

```code

Supplier<Instant> now = () -> Instant.now();

Instant a = now.get().plus(Period.ofDays(1));

Instant b = now.get().plus(Period.ofDays(2));

```

This property that allows you to factor out expressions is called **referential transparency**, and
it's fundamental to create and compose expressions.

What can you expect from JIO:

- Simple and powerful API
- Errors are first-class citizens
- Simple and powerful testing tools ([jio-test](#jio-test))
- Easy to extend and get benefit from all the above. Examples are [jio-http](#jio-http),
  [jio-mongodb](#jio-mongodb) and [jio-jdbc](#jio-jdbc). And you can create your own integrations!
- I don't fall into the logging-library war. This is something that sucks in Java. I just use Java
  Flight Recording!
- Almost zero dependencies (just plain Java!)
- JIO doesn't transliterate any functional API from other languages. This way, any standard Java
  programmer will find JIO quite easy and familiar.

---

## <a name="jio-exp"><a/> jio-exp

[![Maven](https://img.shields.io/maven-central/v/com.github.imrafaelmerino/jio-exp/4.0.0)](https://search.maven.org/artifact/com.github.imrafaelmerino/jio-exp/4.0.0/jar
"jio-ex")

Let's model a functional effect in Java!

```code

import java.util.function.Supplier;
import java.util.concurrent.CompletableFuture;

public abstract class IO<O> implements Supplier<CompletableFuture<O>> {

    CompletableFuture<O> get();

    O result();
}

```

Key Concepts:

- **`IO` Definition**: The `IO` class is a fundamental component of JIO. It's an abstract class
  designed to represent functional effects or computations.

- **Lazy Computation**: `IO` is a lazy computation and is realized as a `Supplier`. In essence, it
  merely outlines a computation without immediate execution, awaiting the explicit invocation of
  methods like `get()` or `result()`. It's important to note that `result` operation is blocking,
  which isn't an issue when employing virtual threads. As you'll soon discover, integrating virtual
  threads with JIO is a straightforward process!

- **Asynchronous Effects**: `IO` leverages `CompletableFuture` to represent asynchronous effects.
  Asynchronous effects are essential for avoiding thread blocking, especially when dealing with
  operations that might introduce latency.

- **Handling Errors**: A critical aspect of JIO is that `CompletableFuture` can represent both
  successful and failed computations. This approach ensures that errors are treated as first-class
  citizens, avoiding the need to throw  
  exceptions whenever an error occurs.

- According to Erik Meyer, as mentioned in [this
  video](https://www.youtube.com/watch?v=z0N1aZ6SnBk), honesty is at the core of functional
  programming. I find this perspective to be quite insightful. Latency and failures hold such a
  significance that they should be explicitly denoted in a function or method's signature with the
  `IO` type. Without this distinction, it becomes impossible to differentiate functions that are
  free from failure and latency from those that aren't, making our code difficult to reason about.

It's worth noting that JIO utilizes CompletableFuture under the hood but shields the client from
having to work with futures and callbacks. The JIO API remains functional and expressive, providing
a straightforward experience for developers.

---

### <a name="creating-effects"><a/> Creating effects

Now that we got the ball rolling, let's learn how to create IO effects.

**From a constant or a computed value**

```code

IO<String> effect = IO.succeed("hi");

JsObj get(int id) { ??? }
IO<String> effect = IO.succeed(get(1)); //get(1) is invoked before constructing the effect

```

In both of the above examples, the effect will always compute the same value: either "hi" or the
result of calling `get(1)`. There is no lazynes here, a value is computed right away and used to
create the IO effect

**From an exception**

```code

IO<String> effect = IO.fail(new RuntimeException("something went wrong :("));

```

Like with `succeed`, the effect will always produce the same result, in this case it fails always
with the same exception, which is instantiated before creating the effect. Do notice that no
exception is thrown!

**From a lazy computation or a supplier**

This is a very common case, and you will use it all the time to create effects.

```code

Suplier<JsObj> computation = ???;
IO<Long> effect = IO.lazy(computation);

```

In this example and effect is created but not like in `succeed` and `fail`, **nothing is evaluated**
since a `Supplier` is lazy. It's very important to notice the difference. On the other hand, each
time the `get` or `result` methods are invoked a potentially new value can be returned.

**From a callable**

We can think of a `Callable` as lazy computations like `Suppliers`, but with the important
difference that they can fail.

```code

Callable<Long> callable = ???;

IO<Long> effect = IO.task(callable);

```

**From a CompletableFuture**:

This is a common case when working with APIs that returns futures like the `sendAsync` methods of
Java HttpClient introduced in Java 11

```code

CompletableFuture<JsObj> get(String id){ ??? }

IO<JsObj> effect = IO.effect( () -> get(1) );

```

Like with `lazy` and `task`, the previous example doesn't evaluate anything to create the effect  
since the effect method takes in a `Supplier`.

In all the above examples, when the `get` or `result`methods are invoked, the values will be
computed on the caller  
thread. Sometimes we need to control on what thread to perform a computation, especially when it's
blocking.  
Whe can specify an executor, or to make use of the **ForkJoin** pool, which is not a problem since
JIO uses internally the
[ManagedBlocker](https://docs.oracle.com/javase/7/docs/api/java/util/concurrent/ForkJoinPool.ManagedBlocker.html)
interface, or you can even get benefit from the Loom project and use fibers!

**From a lazy computation or supplier that has to be executed on a specific pool**

```code

Suplier<Long> computation = ???;

IO<Long> effect = IO.lazy(computation,
                          Executors.newCachedThreadPool()
                         );
```

**From a blocking operation that has to be executed on the ForkJoin Pool by the ManagedBlocker**

```code

Supplier<JsObj> blockingComputation = ???;
Supplier<JsObj> blockingTask = ???;

IO<JsObj> effect = IO.managedLazy(blockingComputation);
IO<JsObj> effect = IO.managedTask(blockingTask);

```

**With virtual threads**

```code

IO<JsObj> effect = IO.lazy(blockingTask,
                           Executors.newVirtualThreadPerTaskExecutor()
                           );

```

**From auto-closable resources**

The `resource` method is used to create an IO effect that manages a resource implementing the
`AutoCloseable` interface. It takes a `Callable` that supplies the closable resource and a mapping
function to transform the resource into a value. This method ensures proper resource management,
including automatic closing of the resource after the map function is executed, to prevent memory
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

The `NULL` method creates an IO effect that always produces a result of null. It is a generic method
that captures the type of the caller, allowing you to create null effects with different result
types. This can be useful when you need to type of the caller, allowing you to create null effects
with different result types. This can be useful when you need to represent a null result in your
functional code. These constants, `TRUE` and `FALSE`, represent IO effects that always succeed with
`true` and `false`, respectively.

---

### <a name="lambdas"><a/> Lambdas

In the world of JIO, working with effectful functions is a common practice. The following functions
return `IO` effects, and you'll often encounter them in your code:

```code

Function<I, IO<O>>

BiFunction<A,B, IO<O>>

```

To make our code more concise and readable, we can give these effectful functions an alias. Let's
call them "Lambdas":

```code

interface Lambda<I, O> extends Function<I, IO<O>> {}

interface BiLambda<A, B, O> extends BiFunction<A, B, IO<O>> {}

```

Lambdas are similar to regular functions, but there's one key difference: they never throw
exceptions. In JIO, exceptions are treated as first-class citizens, just like regular values.

Converting regular functions or predicates into Lambdas is straightforward using the lift methods:

```

Function<Integer, Integer> opposite = n -> -n;
BiFunction<Integer, Integer, Integer> sum = (a,b) -> a + b;
Predicate<Integer> isOdd = n -> n % 2 == 1;

Lambda<Integer, Integer> l1 = Lambda.liftFunction(opposite);
Lambda<Boolean, Integer> l2 = Lambda.liftPredicate(isOdd);
BiLambda<Integer, Integer, Integer> l3 = BiLambda.liftFunction(sum);

```

The `then` method is a powerful feature of the `Lambda` interface in JIO that allows you to compose
and sequence effects in a functional and expressive manner. When you have two `Lambda` instances,
you can use the `then` method to create a new `Lambda` that combines the effects of the two original
lambdas. When you apply the composed `Lambda` to an input, it executes the first `Lambda`, followed
by the second `Lambda`, creating a sequence of effects. This composition is especially useful when
building complex workflows or pipelines of operations. It enhances the readability and
expressiveness of your code by chaining together effects in a natural and intuitive way.

```code

Lambda<A,B> first = ???;
Lambda<B,C> second = ???

Lambda<A,C> third = first.then(second);


```

---

### <a name="operations-with-effects"><a/> Operations with effects

#### Making our code more resilient being persistent!

Retrying failed operations is a crucial aspect of handling errors effectively in JIO. In real-world
scenarios,errors can sometimes be transient or caused by temporary issues, such as network glitches
or resource unavailability.  
By incorporating retry mechanisms, JIO empowers you to gracefully recover from such errors without
compromising the stability of your application. Whether it's a network request, database query, or
any other effect, JIO's built-in retry functionality allows you to define retry policies, such as
exponential backoff or custom strategies, to ensure that your operations have a higher chance of
succeeding. This approach not only enhances the robustness of your application but also minimizes
the impact of transient errors, making JIO a valuable tool for building resilient and reliable
systems.

```code

public abstract class IO<O> extends Supplier<CompletableFuture<O>> {

  IO<O> retry(Predicate<Throwable> predicate,
              RetryPolicy policy
              );

  IO<O> repeat(Predicate<O> predicate,
               RetryPolicy policy
              );
}

```

While the `retry` method is primarily used to retry an operation when an error occurs (based on a
specified exception condition), the `repeat` method allows you to repeat an operation based on the
result or outcome of the effect itself,  
providing flexibility for scenarios where retries are needed for reasons other than errors. Retry
policies are created in a very declarative and composable way, for example:

```code

Duration oneHundredMillis = Duration.ofMillis(100);

Duration oneSec = Duration.ofSeconds(1);

// up to five retries waiting 100 ms
RetryPolicies.constantDelay(oneHundredMillis)
             .append(limitRetries(5))

// during 3 seconds up to 10 times
RetryPolicies.limitRetries(10)
             .limitRetriesByCumulativeDelay(Duration.ofSeconds(3))

// 5 times without delay and then, if it keeps failing,
// an incremental delay from 100 ms up to 1 second
RetryPolicies.limiteRetries(5)
             .followedBy(incrementalDelay(oneHundredMillis)
             .capDelay(oneSec))

```

There are very interesting policies implemented based on [this
article](https://aws.amazon.com/blogs/architecture/exponential-backoff-and-jitter/): exponential
backoff, full jitter, equal jitter, decorrelated jitter etc

#### Making our code more resilient having a Backup plan!

In scenarios where errors persist despite retries, JIO offers robust error-handling mechanisms to
ensure your application maintains resilience. Three key methods come into play:

```code

public abstract class IO<O> extends Supplier<CompletableFuture<O>> {

  IO<O> recover(Function<Throwable, O> fn);

  IO<O> recoverWith(Lambda<Throwable, O> fn);

  IO<O> fallbackTo(Lambda<Throwable, O> fn);

}

```

**recover**: This method allows you to gracefully recover from an error by providing a function that
maps the encountered exception to a fallback value of type 'O.' It ensures that your application can
continue its operation even in the face of unexpected errors.

**recoverWith**: This method allows you to gracefully recover from an error by providing a function
that maps the encountered exception to a fallback value of type 'O.' It ensures that your
application can continue its operation even in the face of unexpected errors.

**fallbackTo**: Similar to 'recoverWith,' 'fallbackTo' allows you to switch to an alternative effect
(specified by the provided function) when an error occurs. However, it introduces an important
distinction: if the alternative effect also encounters an error, 'fallbackTo' will return the
original error from the first effect. This ensures that error propagation is maintained while
enabling you to gracefully handle errors and fallback to alternative operations when needed.

#### Being Functional!

JIO encourages a functional programming style with the following methods:

```code

public abstract class IO<O> extends Supplier<CompletableFuture<O>> {

    IO<A> map(Function<O, A> fn);
    
    IO<A> mapFailure(Function<Throwable, Throwable>);

    IO<A> then(Lambda<O, A> fn);

    IO<O> debug();

    IO<O> debug(EventBuilder builder);
}

```

- `map`: Transforms the successful result of an effect using a provided function, allowing you to map values
  from one type to another.
- `mapFailure`: Transforms the failure result of an effect using a provided function, allowing you to map exceptions
  from one type to another.
- `then` (akin to `flatMap` or `bind` in other languages and a core function in monads): Applies a
  lambda function to  
  the result of the effect, creating a new effect that depends on the previous result. The name
  'then' is used here for conciseness.

#### Houston, we have a problem!

```code

sealed abstract class IO<O> extends Supplier<CompletableFuture<O>> {

    IO<O> debug();

    IO<O> debug(EventBuilder builder);
}

// all the JIO expressions are subtypes of Exp
sealed abstract class Exp<O> extends IO<O> {

    Exp<O> debugEach(String context);

    Exp<O> debugEach(EventBuilder<O> messageBuilder)

}


```

Debugging and logging events play a pivotal role in software development. Some of the advantages
that you'll get are:

1. **Simplifying Debugging**: Debugging is the process of identifying and fixing issues in your
   code. When testing an application, developers often need to trace the execution of specific parts
   of their code to locate bugs or performance bottlenecks. These debugging methods allow you to
   attach debug mechanisms to expressions, making it easier to monitor and log the execution of each
   operand individually. This granular level of insight is invaluable when diagnosing and resolving
   problems.

2. **Testing Efficiency**: During the testing phase, the ability to debug and log events efficiently
   is a time-saving and productivity-enhancing feature. These methods let you generate and send
   events to the Flight Recorder system, which can then be analyzed to gain insights into the
   behavior of your code. You can customize these events using the provided `EventBuilder`,
   tailoring the information collected to suit your specific testing needs.

3. **No Setup Overhead**: One of the key advantages of these methods is that they require minimal
   setup. This means that you can integrate debugging and event logging into your codebase without
   the need for extensive configuration or external tools. It's a straightforward and hassle-free
   way to gather essential information about the execution of your code.

4. **Recursive Debugging with debugEach**: When you apply the debugEach method to an expression, it
   attaches a debugging mechanism to that expression and all its subexpressions. If any
   subexpression within the main expression is itself an expression, debugEach is applied to it
   recursively, passing along the same context.

5. **Customization with EventBuilder**

Customization with the `EventBuilder` is a powerful feature that streamlines your debugging and
event logging. It allows you to tailor events to your specific needs and focus on the most relevant
information:

- **Event Naming**: You can categorize events by specifying the name for the expression being
  evaluated and the context you're observing, making it easier to analyze different aspects of your
  code.

- **Mapping Success Output**: Transform the result of a successful expression into a format that
  provides clear and concise information, helping you capture expected outcomes.

- **Mapping Failure Output**: Customize how exceptions are presented by mapping `Throwable` objects
  into a format that aids in debugging and troubleshooting. This can include error messages, stack
  traces, or other relevant details.

In conclusion, the ability to debug and log events with minimal setup is highly valuable for
developers. It simplifies the debugging process, enhances testing efficiency, and provides data-rich
insights into code execution. These methods are indispensable tools for maintaining software
quality, diagnosing issues, and optimizing performance during the development and testing phases.

#### Being Impatient!

Time is of the essence, and JIO offers methods to deal with timeouts:

```code

public abstract class IO<O> extends Supplier<CompletableFuture<O>> {

    IO<O> timeout(long time,
                  TimeUnit unit
                 );

    IO<O> timeoutOrElse(long time,
                        TimeUnit unit,
                        Supplier<O> defaultVal
                       );

    IO<Void> async();
}
```

- `timeout`: Sets a timeout for the effect, allowing you to limit how long you are willing to wait
  for its completion.
- `timeoutOrElse`: Similar to `timeout`, but with a fallback value that is returned if the timeout
  is exceeded.
- `async`: Allows to execute an action without waiting for its result and returns immediately. It is
  useful when you are not interested in the outcome of the effect (returns void) and want to trigger
  it asynchronously.

#### Being sneaky!

Sometimes, you need to sneak a peek into the execution of an effect:

```code
public abstract class IO<O> extends Supplier<CompletableFuture<O>> {

    IO<O> peekFailure(Consumer<Throwable> failConsumer);

    IO<O> peekSuccess(Consumer<O> successConsumer);

    IO<O> peek(Consumer<O> successConsumer,
               Consumer<Throwable> failureConsumer
              );
}

```

- `peekFailure`: Allows you to observe and potentially handle failures by providing a consumer that
  logs exceptions in the JFR (Java Flight Recorder) system.
- `peekSuccess`: Similarly, you can observe and process successful results using a success consumer.
- `peek`: Combines both success and failure consumers, giving you full visibility into the effect's
  execution.  
  Exceptions occurring here are logged in the JFR system and do not alter the result of the effect.

### I race you!

When you require a result as quickly as possible among multiple alternatives, and you're uncertain
which one will be the fastest:

```code

public abstract class IO<O> extends Supplier<CompletableFuture<O>> {

    static <O> IO<O> race(IO<O> first, IO<O>... others);

}

```

`race` method returns the result of the first effect that completes (whether it succeeds or fails),
allowing you to make quick decisions based on the outcome.

#### Pulling the trigger!

Given an `IO<O>` effect, how do you trigger the execution to compute the final value of type `O`?
There are three ways:

- Getting a future of the evaluation with the method `get()`
- Block and wait till the evaluation is done with the method `join()`.It can throw a
  `CompletionException`. It's syntactic sugar for `get().join()`. If you are using virtual threads,
  blocking is not a problem.
- Block and wait till the evaluation is done with the method `result()`.It can throw a checked
  exception. Not like `join` it throws the real cause of the failure and not `CompletionException`.
- Callback style, Call Call Call Callback style :) with the method `onResult(ouput -> {}, failure -> {})`

```code

IO<O> effect = ???;

CompletableFuture<O> future = effect.get();

O output = effect.result(); //throws checked exception

O output1 = effect.join(); 

effect.onResult(output -> System.out.println(" :) "),
                exc -> System.out.println(" :( ")
               );

```

---

### <a name="expressions"><a/> Expressions

**Using expressions and function composition is how we deal with complexity in Functional
Programming**.  
With the following expressions, you will have a comprehensive toolkit to model effects, combine them
in powerful ways, and tame complexity effectively.

### IfElseExp

The `IfElseExp` expression allows you to conditionally choose between two computations based on a
predicate. If the  
predicate evaluates to true, the consequence is computed, and if it's false, the alternative
computation is chosen. Both the consequence and alternative are represented as lazy computations of
IO effects.

```code

IO<O> exp = IfElseExp.<O>predicate(IO<Boolean> condition)
                     .consequence(Supplier<IO<O>> consequence)
                     .alternative(Supplier<IO<O>> alternative);


```

In this code, the `consequence` and `alternative` parameters are represented as `Supplier`
instances, which means they are not computed during the construction of the expression but rather
evaluated only when needed, based on the result of the condition. This deferred execution allows for
efficient conditional evaluation of IO effects.

### SwitchExp

Your original sentence is mostly correct but could benefit from a minor improvement for clarity:

The `SwitchExp` expression simulates the behavior of a switch construct, enabling multiple
pattern-value branches. It evaluates an effect or a value of type `I` and facilitates the use of
multiple clauses based on the evaluation. The `match` method compares the value or effect with
patterns and selects the corresponding lambda (which takes in the value of type `I`). Patterns can
encompass values, lists of values, or even predicates. It's possible to specify a default branch, in
case no pattern matches the input (otherwise the expression is reduced to `IO.NULL`).

```code

// matches a value of type I

IO<O> exp =
          SwitchExp<I,O>.eval(I value)
                      .match(I pattern1, Lambda<I,O> lambda1,
                             I pattern2, Lambda<I,O> lambda2,
                             I pattern3, Lambda<I,O> lambda3,
                             Lambda<I,O> otherwise          //optional
                            );

// matches an effect of type I

IO<O> exp=
        SwitchExp<I, O>.eval(IO<I> effect)
                       .match(I pattern1, Lambda<I,O> lambda1,
                              I pattern2, Lambda<I,O> lambda2,
                              I pattern3, Lambda<I,O> lambda3,
                              Lambda<I,O> otherwise         //optional
                             );


// For example, the following expression reduces to "3 is Wednesday"

IO<O> exp=
         SwitchExp<String>.eval(3)
                          .match(1, n -> IO.succedd(n + " is Monday"),
                                 2, n -> IO.succedd(n + " is Tuesday"),
                                 3, n -> IO.succedd(n + " is Wednesday"),
                                 4, n -> IO.succedd(n + " is Thursday"),
                                 5, n -> IO.succedd(n + " is Friday"),
                                 n -> IO.succedd(n + " is weekend")
                                );
```

The same as before but using lists instead of constants as patterns.

```code

IO<O> exp =
    SwitchExp<I, O>.eval(I value)
                   .match(List<I> pattern1, Lambda<I,O> lambda1,
                          List<I> pattern2, Lambda<I,O> lambda2,
                          List<I> pattern3, Lambda<I,O> lambda3,
                          Lamda<I,O> otherwise
                         );

// For example, the following expression reduces to "20 falls into the third week"
IO<O> exp=
    SwitchExp<Integer, String>.eval(20)
                              .match(List.of(1, 2, 3, 4, 5, 6, 7),
                                     n -> IO.succeed(n + " falls into the first week"),
                                     List.of(8, 9, 10, 11, 12, 13, 14),
                                     n -> IO.succeed(n + " falls into the second week"),
                                     List.of(15, 16, 17, 18, 19, 20),
                                     n -> IO.succeed(n + " falls into the third week"),
                                     List.of(21, 12, 23, 24, 25, 26, 27),
                                     n -> IO.succeedd(n + " falls into the forth week"),
                                     n -> IO.succeed(n + " falls into the last days of the month")
                                     );
```

Last but not least, you can use predicates as patterns instead of values or list of values:

```code

IO<O> exp=
        SwitchExp<I, O>.eval(IO<I> value)
                       .match(Predicate<I> pattern1, Lambda<I,O> lambda1,
                              Predicate<I> pattern2, Lambda<I,O> lambda2,
                              Predicate<I> pattern3, Lambda<I,O> lambda3
                              );

// For example, the following expression reduces to the default value:
// "20 is greater or equal to ten"

IO<O> exp=
        SwitchExp<Integer, String>.eval(IO.succeed(20))
                                  .match(n -> n < 5,
                                         n -> IO.succeed(n + "is lower than five"),
                                         n -> n < 10,
                                         n -> IO.succeed(n + "is lower than ten"),
                                         n-> n > 10,
                                         n -> IO.succeed(n + "is greater or equal to ten")
                                         );
```

### CondExp

`CondExp` is a set of branches and a default effect. Each branch consists of an effect that computes
a boolean (the  
condition) and its associated effect. The expression is reduced to the value of the first branch
with a true condition, making the order of branches significant. If no condition is true, it
computes the default effect if specified (otherwise the expression is reduced to `IO.NULL`)

```code

IO<O> exp=
    CondExp.<O>seq(IO<Boolean> cond1, Supplier<IO<O>> effect1,
                   IO<Boolean> cond2, Supplier<IO<O>> effect2,
                   IO<Boolean> cond3, Supplier<IO<O>> effect3,
                   Supplier<IO<O>> otherwise                   //optional
                  );


IO<O> exp =
    CondExp.<O>par(IO<Boolean> cond1, Supplier<IO<O>> effect1,
                   IO<Boolean> cond2, Supplier<IO<O>> effect2,
                   IO<Boolean> cond3, Supplier<IO<O>> effect3,
                   Supplier<IO<O>> otherwise                  //optional
                  );

```

### AllExp and AnyExp

`AllExp` and `AnyExp` provide idiomatic boolean expressions for "AND" and "OR." They allow you to
compute multiple boolean effects, either sequentially or in parallel.

```code

IO<Boolean> allPar = AllExp.par(IO<Boolean> cond1, IO<Boolean> cond2,....);
IO<Boolean> allSeq = AllExp.seq(IO<Boolean> cond1, IO<Boolean> cond2,....);

IO<Boolean> anyPar = AnyExp.par(IO<Boolean> cond1, IO<Boolean> cond2,...);
IO<Boolean> anySeq = AnyExp.seq(IO<Boolean> cond1, IO<Boolean> cond2,...);

```

You can also create AllExp or AnyExp from streams of IO<Boolean> using the `parCollector` and
`seqCollector`

```code

Lambda<Vehicle, Boolean> isFerrari = ???

List<Vehicle> vehicles = ???;

AllExp allFerrariPar = vehicles.stream()
                               .map(isFerrary)
                               .collector(AllExp.parCollector());

AllExp allFerrariSeq = vehicles.stream()
                               .map(isFerrary)
                               .collector(AllExp.seqCollector());

AnyExp anyFerrariSeq = vehicles.stream()
                               .map(isFerrary)
                               .collector(AnyExp.seqCollector());


```

### PairExp and TripleExp

`PairExp` and `TripleExp` allow you to zip effects into tuples of two and three elements,
respectively. You can compute each element either in parallel or sequentially.

```code

IO<Pair<A, B> pairPar = PairExp.par(IO<A> effect1,
                                    IO<B> effect2);

IO<Pair<A, B> pairSeq = PairExp.seq(IO<A> effect1,
                                    IO<B> effect2);

IO<Triple<A, B, C> triplePar = TripleExp.par(IO<A> effect1,
                                             IO<B> effect2,
                                             IO<C> effect3);

IO<Triple<A, B, C> tripleSeq = TripleExp.seq(IO<A> effect1,
                                             IO<B> effect2,
                                             IO<C> effect3);

```

### JsObjExp and JsArrayExp

`JsObjExp` and `JsArrayExp` are data structures resembling raw JSON. You can compute their values
sequentially or in parallel. You can mix all the expressions discussed so far and nest them,
providing you with immense flexibility and  
power in handling complex data structures.

```code

IfElseExp<JsStr> a = IfElseExp.<JsStr>predicate(IO<Boolean> cond1)
                              .consequence(Supplier<IO<JsStr>> consequence)
                              .alternative(Supplier<IO<JsStr>> alternative);

JsArrayExp b =
    JsArrayExp.seq(SwitchExp<Integer, JsValue>.match(n)
                                              .patterns(n -> n <= 0, Supplier<IO<JsValue>> e1,
                                                        n -> n  > 0, Supplier<IO<JsValue>> e2
                                                       ),
                   CondExp.par(IO<Boolean> cond2, Supplier<IO<JsValue>> e3,
                               IO<Boolean> cond3, Supplier<IO<JsValue>> e4,
                               Supplier<IO<JsValue>> otherwise
                              )
                 );

JsObjExp c = JsObjExp.seq("d", AnyExp.seq(IO<JsBool> cond1, IO<JsBool> cond2)
                          "e", AllExp.par(IO<JsBool> cond2, IO<JsBool> cond3)
                          "f", JsArrayExp.par(IO<JsValue> e5, IO<JsValue> e6)
                          );

JsObjExp exp = JsObjExp.par("a",a,
                            "b",b,
                            "c",c
                           );

JsObj json = exp.result();

```

Here are some key points about the code example:

1. **Readability**: The code is relatively easy to read and understand, thanks to the fluent API
   style provided by JIO's expressions. This makes it straightforward to follow the logic of
   constructing a `JsObj` with multiple key-value pairs.

2. **Modularity**: Each key-value pair is constructed separately, making it easy to add, modify, or
   remove components without affecting the others. This modularity is a significant advantage when
   dealing with complex data structures.

3. **Parallelism**: The example demonstrates the ability to perform computations in parallel when
   constructing  
   the `JsObj`. By using expressions like `JsObjExp.par`, you can take advantage of multicore
   processors and improve performance.

4. **Nesting**: The example also shows that you can nest expressions within each other, allowing for
   recursive data  
   structures. This is valuable when dealing with deeply nested expressions or other complex data
   formats.

Overall, the code example effectively illustrates how JIO's expressions enable you to create,
manipulate, and compose functional effects to handle complex data scenarios. It highlights the
conciseness and expressiveness of the library  
when dealing with such tasks.

### ListExp

Represents an expression that is reduced to a list of values. You can create ListExp expressions
using the `seq` method to evaluate effects sequentially or using the `par` method to evaluate
effects in parallel. If one effect fails, the entire expression fails.

```code

ListExp<String> par = ListExp.par(IO<String> effect1, IO<String> effect2, ...)

ListExp<Integer> seq = ListExp.seq(IO<String> effect3, IO<String> effect3, ...)

List<String> xs = par.result();
List<Integer> ys = seq.result();

```

It's possible to create ListExp from stream of effects of the same type using the collectors
`parCollector` and `seqCollector`:

```code

Lambda<String, Person> getPersonFromId = ???;

List<String> ids = ???;

ListExp<Person> xs = ids.stream()
                        .filter(id -> id > 0)
                        .map(getPersonFromId)
                        .collect(ListExp.parCollector());

List<Person> persons = xs.result();

```

---

## <a name="Clocks"><a/> Clocks

In functional programming, it's crucial to maintain a clear separation between inputs and outputs of
a function. When dealing with time-related operations, such as retrieving the current date or time,
it becomes even more critical to adhere to this principle. This is where the concept of clocks in
JIO comes into play. A clock in JIO is essentially a supplier that returns a numeric value,
representing time. There are three types of  
clocks available:

- Realtime: This clock is affected by Network Time Protocol (NTP) adjustments and can move both
  forwards and backward in time. It is implemented using the System.currentTimeMillis() method.
  Realtime clocks are typically used when you need to work with the current wall-clock time.
- Monotonic: Monotonic clocks are useful for measuring time intervals and performing time-related
  comparisons. They are not affected by NTP adjustments and provide a consistent and continuous time
  source. Monotonic clocks are implemented using the System.nanoTime() method.
- Custom: JIO allows you to create your custom clocks. Custom clocks are particularly valuable for
  testing scenarios  
  where you want to control the flow of time, possibly simulating the past or future.

```code

sealed interface Clock extends Supplier<Long> permits Monotonic,RealTime, CustomClock {}

```

Every time you write _new Date()_ or _Instant.now()_ in the body of a method or function, you are
creating a side effect.  
Remember that in FP, all the inputs must appear in the signature of a function. Dealing with time,
it's even more  
important. Also, it's impossible to control by any test the value of that timestamp which leads to
code difficult to  
test.

### Why It Matters

The reason why dealing with time as an input is crucial in functional programming is to make code
more predictable,  
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

In this improved version, we pass a Clock object as a parameter to the processPayment method. This
approach offers  
several advantages:

- Testability: During testing, you can provide a custom clock that allows you to control the current
  time, making tests more predictable and reliable.
- Predictability: The behavior of the method is consistent regardless of when it's called since it
  depends on the  
  provided clock.

By using a clock as a parameter, you enhance the reliability and maintainability of your code,
especially in scenarios  
where time plays a critical role.

## <a name="Debugging-and-JFR-integration"><a/> Debugging and Java Flight Recorder (JFR) Integration

### Why I chose JFR

In the world of Java, there has long been a multitude of logging libraries and frameworks, each with
its strengths and limitations. However, the introduction of Java Flight Recorder (JFR) has been a
game-changer. JFR is a native and highly efficient profiling and event recording mechanism embedded
within the Java Virtual Machine (JVM). Its native integration means it operates seamlessly with your
Java applications, imposing minimal performance overhead. JFR provides unparalleled visibility into
the inner workings of your code, allowing you to capture and analyze events with precision.

Unlike external logging libraries, JFR doesn't rely on third-party dependencies or introduce
additional complexity to  
your projects. By using JFR within JIO, you harness the power of this built-in tool to gain deep
insights into the  
behavior of your functional effects and expressions, all while keeping your codebase clean and
efficient. JFR is the  
dream solution for Java developers seeking robust debugging and monitoring capabilities with minimal
hassle."

Debugging and monitoring the behavior of your JIO-based applications is essential for
troubleshooting, performance  
optimization, and gaining insights into your functional effects and expressions. JIO provides
comprehensive support for debugging and integration with Java Flight Recorder (JFR) to capture and
analyze events.

---

### Debugging Individual Effects

You have the option to enable debugging for individual effects using the `debug` method. When this
method is used, a new effect is created that generates a `RecordedEvent` and sends it to the Flight
Recorder system. You can also customize the event by providing an `EventBuilder`. Here's an
overview:

The `IO` class has the following methods for debugging:

```code
sealed abstract class IO<O> extends Supplier<CompletableFuture<O>> {

    IO<O> debug();

    IO<O> debug(EventBuilder<O> builder);
}
```

The resulting JFR event is defined as follows:

```code
import jdk.jfr.*;

@Label("jio-eval-expression")
@Name("jio.exp")
@Category({"JIO"})
@Description("JIO expressions and subexpressions results")
class ExpEvent extends Event {

    @Label("exp")
    public String expression;

    @Label("value")
    public String value;

    @Label("context")
    public String context;

    @Label("result")
    public String result;
    public enum RESULT {SUCCESS, FAILURE}

    @Label("exception")
    public String exception;

}
```

You can use the [JIO debugger](#junit) to print the events sent to the JFR system. Here's an
example:

```code
@RegisterExtension
static Debugger debugger = Debugger of (Duration.ofSeconds(1));

@Test
public void test() {

    Integer value = IO.succeed(10).debug().result();

    // result() would throw an exception!
    CompletableFuture<Integer> failure =
        IO.<Integer>fail(new RuntimeException("JIO is great!"))
          .debug()
          .get();
}
```

The result includes events like this:

```text
event: eval, expression: Val, result: SUCCESS, output: 10
duration: 2,693 ms, context: , thread: main, event-start-time: 2023-10-24T23:42:51.6197205+02:00

event: eval, expression: Val, result: FAILURE, output: java.lang.RuntimeException: JIO is great!
duration: 59,291 µs, context: , thread: main, event-start-time: 2023-10-24T23:49:01.073597625+02:00
```

The event type is always "eval" (for evaluation), and the expression is "Val" (for Value),
reflecting the evaluation of two irreducible expressions. The result is "SUCCESS" for the first
evaluation, and "FAILURE" for the second. The context in both cases is the default (an empty
string).

You can customize event messages using an `EventBuilder`. For example:

```code
EventBuilder<Integer> eb =
    EventBuilder.<Integer>of("other_exp_name", "fun")
                .withSuccessOutput(output -> "XXX")
                .withFailureOutput(Throwable::getMessage);

Integer value = IO.succeed(10).debug(eb).result();

// result() would throw an exception!
CompletableFuture<Integer> failure = IO.<Integer>fail(new RuntimeException("JIO is great!"))
                                       .debug(eb)
                                       .get();
```

The result with this customization is:

```text
event: eval, expression: other_exp_name, result: SUCCESS, output: XXX
duration: 4,071 ms, context: fun, thread: main, event-start-time: 2023-10-24T23:55:36.360573875+02:00

event: eval, expression: other_exp_name, result: FAILURE, output: JIO is great!
duration: 38,750 µs, context: fun, thread: main, event-start-time: 2023-10-24T23:55:36.364976084+02:00
```

The `EventBuilder` provides key points for customization, including specifying event messages for
successful and failed computations and associating events with specific expressions and contexts.

### Debugging Expressions

JIO's debugging capabilities extend beyond individual effects. You can attach a debug mechanism to
each operand of an expression using the `debugEach` method. This allows you to monitor and log the
execution of each operand individually. Here's an overview:

```code
sealed abstract class Exp<O> extends Supplier<CompletableFuture<O>> {

    Exp<O> debugEach(EventBuilder<O> builder);

    Exp<O> debugEach(String context);
}
```

By using `debugEach`, you can gain insights into the behavior of complex expressions and identify
any issues or bottlenecks that may arise during execution. This mechanism is recursive, meaning that
if subexpressions are expressions themselves, `debugEach` will be called on them, copying the
context of the event (if specified).

You can also provide an `EventBuilder` or a descriptive context to customize the debug events for
each operand.

Here's an example of using `debugEach`:

```code

@RegisterExtension
static Debugger debugger = Debugger.of(Duration.ofSeconds(2));

@Test
public void test() {

    Supplier<Boolean> isLowerCase = BoolGen.arbitrary().sample();
    Supplier<String> lowerCase = Combinators.oneOf("a", "e", "i", "o", "u").sample();
    Supplier<String> upperCase = Combinators.oneOf("A", "E", "I", "O", "U").sample();

    SwitchExp<String, String> match =
        SwitchExp.<String, String>eval(IfElseExp.<String>predicate(IO.lazy(isLowerCase))
                                                .consequence(() -> IO.lazy(lowerCase))
                                                .alternative(() -> IO.lazy(upperCase))
                                       )
                 .match(List.of("a", "e", "i", "o", "u"),
                        s -> IO.succeed("%s %s".formatted(s, s.toUpperCase())),
                        List.of("A", "E", "I", "O", "U"),
                        s -> IO.succeed("%s %s".formatted(s, s.toLowerCase()))
                       )
                 .debugEach("context");

    System.out.println("The output is " + match.result());

}
```

The result after executing this test includes events related to each operand:

```text
The output is E e

event: eval, expression: SwitchExp-eval-predicate, result: SUCCESS, output: false
duration: 2,535 ms, context: context, thread: main, event-start-time: 2023-10-15T10:32:58.749536666+02:00

event: eval, expression: SwitchExp-eval-alternative, result: SUCCESS, output: E
duration: 12,541 µs, context: context, thread: main, event-start-time: 2023-10-15T10:32:58.752555+02:00

event: eval, expression: SwitchExp-eval, result: SUCCESS, output: E
duration: 3,213 ms, context: context, thread: main, event-start-time: 2023-10-15T10:32:58.749529541+02:00

event: eval, expression: SwitchExp-branch[1], result: SUCCESS, output: E e
duration: 7,250 µs, context: context, thread: main, event-start-time: 2023-10-15T10:32:58.75355575+02:00

event: eval, expression: SwitchExp, result: SUCCESS, output: E e
duration: 4,057 ms, context: context, thread: main, event-start-time: 2023-10-15T10:32:58.749524+02:00

```

As mentioned earlier, the `debugExp` function is recursive. Since the `eval` subexpression of the
`SwitchExp` is an `IfElseExp`, you can see events associated with it: `SwitchExp-eval-predicate` and
`SwitchExp-eval-alternative`.

## <a name="exp-installation"><a/> Installation

It requires Java 21 or greater

```code

<dependency>
    <groupId>com.github.imrafaelmerino</groupId>
    <artifactId>jio-exp</artifactId>
    <version>4.0.0</version>
</dependency>

```

[json-values](https://github.com/imrafaelmerino/json-values) is the only dependency

## <a name="jio-http"><a/> jio-http

[![Maven](https://img.shields.io/maven-central/v/com.github.imrafaelmerino/jio-http/4.0.0)](https://search.maven.org/artifact/com.github.imrafaelmerino/jio-http/4.0.0/jar
"jio-http")

### <a name="httpserver"><a/> HTTP server

In JIO, you can build and deploy HTTP servers using the `HttpServerBuilder`. This builder is a
versatile tool for defining and launching HTTP servers for various purposes, including testing. The
`HttpServerBuilder` allows you to create `HttpServer` or `HttpsServer` instances with ease.

Employing the `HttpServer` native class to initiate servers for your tests simplifies both setup and
teardown procedures, as the server is embedded within the Java process running the test. **This
ensures that you'll never leave a port lingering**.

**Specifying the Request Handlers**

To handle specific URI paths, you can associate each path with an HTTP request handler. For each
path, specify a handler that will be invoked for incoming requests.

```code
HttpHandler handler = ???;

HttpHandler handler1 = ???;

HttpServerBuilder serverBuilder = HttpServerBuilder.of("/your-path", handler,
                                                       "/your-path1", handler1);

```

**Specifying an Executor**

When creating an `HttpServer` is possible to specify an `Executor`. All HTTP requests received by
the server will be handled in tasks provided to this executor. By default virtual threads are used:
`Executors.newVirtualThreadPerTaskExecutor()`

**Setting the Socket Backlog**

The `HttpServerBuilder` allows you to specify the socket backlog, which defines the number of
incoming connections that can be queued for acceptance. You can set the backlog using the
`withBacklog(int backlog)` method.

```code
int backlog = ???; // Your desired backlog value

serverBuilder.withBacklog(backlog);
```

**Enabling SSL**

If you want to accept only SSL connections:

```code

HttpsConfigurator httpsConfigurator = ???;

serverBuilder.withSSL(httpsConfigurator);

```

**Recording JFR Events (Java Flight Recorder)**

By default, the `HttpServer` records Java Flight Recorder (JFR) events for HTTP requests, which can
be helpful for debugging and performance analysis. However, you can disable this feature:

```code

serverBuilder.withoutRecordedEvents();

```

**Starting the server on a Specific Port**

The start methods allow you to create and start the HTTP server at your convenience.

```code
String host = "localhost";
int port = 8080;

HttpServer server = serverBuilder.start(host, port);
```

**Starting the server on a Random Available Port**

You can even pick a random port, which is useful for local testing as we'll see later.

```code
int startPort = 8000;

int endPort = 9000;

HttpServer server = serverBuilder.startAtRandom(startPort, endPort);
```

In conclusion, with the `HttpServerBuilder`, you can easily create and deploy HTTP/HTTPS servers in
your JIO applications, making it convenient for testing and development. Whether you need to specify
an executor, add request handlers, or start on specific or random ports, this builder provides the
flexibility and functionality to meet your server deployment needs.

Find below a complete example and the events sent to the JFR system:

```code
 import com.sun.net.httpserver.HttpHandler;

 HttpHandler tokenHandler =
            PostStub.of(BodyStub.gen(JsObjGen.of("access_token", JsStrGen.alphanumeric(10, 10))
                                             .map(JsObj::toString)),
                        StatusCodeStub.cons(200)
                        );
 HttpHandler thankHandler =
        GetStub.of(BodyStub.cons("your welcome!"),
                   StatusCodeStub.gen(Combinators.freq(Pair.of(5, IntGen.arbitrary(200, 299)),
                                                       Pair.of(1, Gen.cons(401))))
                  );

 HttpServer server =
        HttpServerBuilder.of(Map.of("/token", tokenHandler,
                                    "/thanks", thankHandler
                                    )
                            )
                         .startAtRandom(8000, 9000);

```

The example code sets up a test environment for a HTTP client with OAuth support (Client Credentials
flow). It uses stubs from [jio-test](#jio-test) to create HTTP handlers for testing different
scenarios. The `tokenHandler` simulates an OAuth token request, and the `thankHandler` simulates a
response that includes a "your welcome!" message. The status code for the `thankHandler` is
generated to return a 401 response approximately 1 out of 6 times, simulating the case where the
access token has expired. The `HttpServerBuilder` is used to create an HTTP server on a random port
to handle these requests. This setup allows testing of various scenarios, including token expiration
handling.

---

### <a name="httpclient"><a/> HTTP client

In JIO, I create an HTTP client on top of the Java HttpClient introduced in Java 11. JIO's goal is
to work with Java's native objects (no abstraction on top of them) while treating errors as normal
values (Lambdas can help us here!). This approach allows us to define an HTTP exchange as a
function, where the input is an HTTP request (modeled using `java.net.http.HttpRequest.Builder`),
and the output is an `IO` object representing the response. The function signature for this is as
follows:

```code

<O> Lambda<HttpRequest.Builder, HttpResponse<O>>

```

To make this type more concise, we give it an alias in JIO-HTTP. We call the previous function an
`HttpLambda<O>`, where `O` represents the response body type (typically `String` or `byte[]`):

```code

interface HttpLambda<O> extends Lambda<HttpRequest.Builder, HttpResponse<O>> {}

```

JIO-HTTP offers an HTTP client with various options for handling different response types. Depending
on your desired response type, you can use one of the following methods:

```java

public interface JioHttpClient {

  HttpLambda<String> ofString();

  HttpLambda<byte[]> ofBytes();

  HttpLambda<Void> discarding();

  <T> HttpLambda<T> bodyHandler(final HttpResponse.BodyHandler<T> handler);

}


```

You can create and configure a `JioHttpClient` using the builder `JioHttpClientBuilder`. This
builder allows you to customize the HTTP client, including specifying a retry policy, a retry
predicate for selecting what errors to retry, and enabling or disabling the recording of Java Flight
Recorder (JFR) events for HTTP requests and responses. JFR event recording is enabled by default:

- `withRetryPolicy`: Sets a default retry policy for handling exceptions during requests.
- `withRetryPredicate`: Sets a default predicate for selectively applying the retry policy based on
  the type or condition of the exception.
- `withoutRecordEvents`: Disables the recording of JFR events for HTTP requests.

Below is a complete example, making requests to the famous PetStore service, illustrating how to use
create and use the JIO HTTP client.

```code

public class TestHttpClient {

    @RegisterExtension
    static Debugger debugger = Debugger.of(Duration.ofSeconds(2));

    static JioHttpClient client =
         JioHttpClientBuilder.of(HttpClient.newBuilder()
                                           .connectTimeout(Duration.ofMillis(300))
                                )
                             .setRetryPolicy(RetryPolicies.incrementalDelay(Duration.ofMillis(10))
                                                          .append(RetryPolicies.limitRetries(5)))
                             .setRetryPredicate(CONNECTION_TIMEOUT.or(NETWORK_UNREACHABLE))
                             .get();

    static String uri = "https://petstore.swagger.io/v2/%s/%s";
    static BiFunction<String, String, HttpRequest.Builder> GET =
            (entity, id) -> HttpRequest.newBuilder()
                                       .GET()
                                       .uri(URI.create(uri.formatted(entity,id)));


    @Test
    public void testGetPetStoreMethods() {

        IO<HttpResponse<String>> getPet = client.ofString().apply(GET.apply("pet", "1"));

        IO<HttpResponse<String>> getOrder = client.ofString().apply(GET.apply("store/order", "1"));

        List<Integer> status = ListExp.par(getPet, getOrder)
                                      .map(responses -> responses.stream()
                                                                 .map(HttpResponse::statusCode)
                                                                 .toList()
                                          )
                                      .result();

        Assertions.assertTrue(status.size() == 2);


    }

}

```

One possible outcome is

```text
Started JFR stream for 2,000 sg in Properties

event: httpclient-req, result: FAILURE, exception: java.net.http.HttpConnectTimeoutException:HTTP connect timed out
duration: 321,280 ms method: GET, uri: https://petstore.swagger.io/v2/store/order/1, req-counter: 2
thread: ForkJoinPool.commonPool-worker-1, event-start-time: 2023-10-11T20:30:12.33834325+02:00

event: httpclient-req, result: FAILURE, exception: java.net.http.HttpConnectTimeoutException:HTTP connect timed out
duration: 332,082 ms method: GET, uri: https://petstore.swagger.io/v2/pet/1, req-counter: 1
thread: ForkJoinPool.commonPool-worker-2, event-start-time: 2023-10-11T20:30:12.327645459+02:00

event: httpclient-req, result: SUCCESS, status-code: 200
duration: 382,032 ms method: GET, uri: https://petstore.swagger.io/v2/store/order/1, req-counter: 3
thread: ForkJoinPool.commonPool-worker-1, event-start-time: 2023-10-11T20:30:12.671884375+02:00

event: httpclient-req, result: SUCCESS, status-code: 200
duration: 382,203 ms method: GET, uri: https://petstore.swagger.io/v2/pet/1, req-counter: 4
thread: ForkJoinPool.commonPool-worker-1, event-start-time: 2023-10-11T20:30:12.67190275+02:00


```

Some errors occurred due to the connection timeout being too short for this particular scenario.
Thankfully, the retry mechanism came to the rescue! Additionally, the `HttpExceptions` class
provides numerous predicates to help identify the most common errors that can occur during request
execution. As you can see in the thread field, jio-http client uses virtual threads.

---

### <a name="oauth"><a/> OAUTH HTTP client

jio-http provides support for client credentials flow OAuth. Here are the possible customizations
for the `ClientCredsBuilder` builder:

1. The request sent to the server to get the access token:

   - `accessTokenReq` parameter: A lambda that takes the regular HTTP client and returns the HTTP
     request to get the token. There is a factory method to build a specific request in the class
     `AccessTokenRequest`. For example one that takes in the client id and secret and an `URI` to
     make the following request:

     ```shell

     curl -X POST -H "Accept: application/json" \
                  -H "Authorization: Basic ${Base64(ClientId:ClientSecret)}" \
                  -H "Content-Type: application/x-www-form-urlencoded" \
                  -d "grant_type=client_credentials" \
                  https://host:port/token

     ```

2. A function to read the access token from the server response:

   - `getAccessToken` parameter: A lambda that takes the server response and returns the OAuth
     token. You can use the existing implementation `GetAccessToken`, which parses the response into
     a `JsObj` and returns the access token located at the "access_token" field. If the token is not
     found, the lambda fails with the exception `AccessTokenNotFound`. The `GetAccessToken` class is
     a singleton with a private constructor, and you can use the `GetAccessToken.DEFAULT` instance
     for this purpose.

3. A predicate that checks if the access token needs to be refreshed:

   - `refreshTokenPredicate` parameter: A predicate that checks the response to determine if the
     access token needs to be refreshed.

4. The authorization header name:

   - `authorizationHeaderName` field: The name of the authorization header, which is set to
     "Authorization" by default.

5. A function to create the authorization header value from the access token:
   - `authorizationHeaderValue` field: A function that takes the access token and returns the
     authorization header value. By default, it is set to "Bearer ${Access Token}".

You can customize these options when creating an instance of `ClientCredsBuilder` to configure the
behavior of the OAuth client credentials flow support in your HTTP client. Since you need a
JioHttpClientBuilder instance to create `ClientCredsBuilder`, you can specify retry policies and
predicates, and of course you can disable the recording of JFR events for every exchange.

The builder returns an instance of `ClientCredsClient`, which is an implementation of
`OauthHttpClient`:

```code
package jio.http.client.oauth;

import jio.http.client.HttpLambda;
import jio.http.client.JioHttpClient;
import java.net.http.HttpResponse;

public interface OauthHttpClient extends JioHttpClient {

   // since it extends JioHttpClient: ofString() ofBytes() and so on are available as well!

    HttpLambda<String> oauthOfString();

    HttpLambda<byte[]> oauthOfBytes();

    HttpLambda<Void> oauthDiscarding();

    <T> HttpLambda<T> oauthBodyHandler(final HttpResponse.BodyHandler<T> handler);

}


```

The advantage of the `oauthXXX` methods is that they handle all token retrieval and refresh requests
on behalf of the client, relieving developers from the burden of implementing these processes.

Here's an illustrative example:

```code
import java.net.URI;

public class TestOauthHttpClient {

    @RegisterExtension
    static Debugger debugger = Debugger.of(Duration.ofSeconds(2));

    //HttpServer creation from one of the previous examples!!!

    static JioHttpClientBuilder clientBuilder =
        JioHttpClientBuilder.of(HttpClient.newBuilder()
                                          .connectTimeout(Duration.ofMillis(300)))
                            .withRetryPolicy(RetryPolicies.incrementalDelay(Duration.ofMillis(10))
                                                          .append(RetryPolicies.limitRetries(5)))
                            .withRetryPredicate(CONNECTION_TIMEOUT.or(NETWORK_UNREACHABLE));

    static tokenUri = URI.create("http://localhost:%s/token".formatted(port));
    static OauthHttpClient client =
            ClientCredsBuilder.of(clientBuilder,
                                  AccessTokenRequest.of("client_id",
                                                        "client_secret",
                                                        tokenUri
                                                        ),
                                  GetAccessToken.DEFAULT, //token in access_token key in a JSON
                                  resp ->resp.statusCode() == 401 // if 401 go for a new token
                                  )
                               .get();

    @Test
    public void testOuth() {
        URI uri = URI.create("http://localhost:%s/thanks".formatted(port));
        client.oauthOfString()
              .apply(HttpRequest.newBuilder().uri(uri))
              .repeat(resp -> true, RetryPolicies.limitRetries(10))
              .result();

    }

}

```

Let's pick some events from the console. Notice that both the events from the server and the client
are printed.

```text

event: httpserver-req, result: CLIENT_ERROR, status-code: 401, duration: 156,500 µs
protocol: HTTP/1.1, method: GET, uri: /thanks, req-counter: 8
remoteHostAddress: localhost, remoteHostPort: 62102, headers: Connection:Upgrade, HTTP2-Settings, Http2-settings:AAEAAEAAAAIAAAABAAMAAABkAAQBAAAAAAUAAEAA, Host:localhost:8000, User-agent:Java-http-client/21, Upgrade:h2c, Authorization:Bearer Rm281jv4I9
thread: HTTP-Dispatcher, event-start-time: 2023-10-13T10:50:23.505934208+02:00

event: httpclient-req, result: CLIENT_ERROR, status-code: 401
duration: 1521,083 µs method: GET, uri: http://localhost:8000/thanks, req-counter: 8
thread: ForkJoinPool.commonPool-worker-1, event-start-time: 2023-10-13T10:50:23.505256+02:00

event: httpserver-req, result: SUCCESS, status-code: 200, duration: 302,250 µs
protocol: HTTP/1.1, method: POST, uri: /token, req-counter: 9
remoteHostAddress: localhost, remoteHostPort: 62102, headers: Accept:application/json, Connection:Upgrade, HTTP2-Settings, Http2-settings:AAEAAEAAAAIAAAABAAMAAABkAAQBAAAAAAUAAEAA, Host:localhost:8000, User-agent:Java-http-client/21, Upgrade:h2c, Authorization:Basic Y2xpZW50X2lkOmNsaWVudF9zZWNyZXQ=, Content-type:application/x-www-form-urlencoded, Content-length:29
thread: HTTP-Dispatcher, event-start-time: 2023-10-13T10:50:23.507630458+02:00

event: httpclient-req, result: SUCCESS, status-code: 200
duration: 1359,459 µs method: POST, uri: http://localhost:8000/token, req-counter: 9
thread: ForkJoinPool.commonPool-worker-1, event-start-time: 2023-10-13T10:50:23.506893833+02:00

event: httpserver-req, result: SUCCESS, status-code: 210, duration: 117,459 µs
protocol: HTTP/1.1, method: GET, uri: /thanks, req-counter: 10
remoteHostAddress: localhost, remoteHostPort: 62102, headers: Connection:Upgrade, HTTP2-Settings, Http2-settings:AAEAAEAAAAIAAAABAAMAAABkAAQBAAAAAAUAAEAA, Host:localhost:8000, User-agent:Java-http-client/21, Upgrade:h2c, Authorization:Bearer O389KKC467
thread: HTTP-Dispatcher, event-start-time: 2023-10-13T10:50:23.508969833+02:00

event: httpclient-req, result: SUCCESS, status-code: 210
duration: 1222,042 µs method: GET, uri: http://localhost:8000/thanks, req-counter: 10
thread: ForkJoinPool.commonPool-worker-1, event-start-time: 2023-10-13T10:50:23.50832375+02:00



```

In the server event generated during the token request, you can observe the Authorization header
sent by the client, with the value "Basic Y2xpZW50X2lkOmNsaWVudF9zZWNyZXQ=". If we decode this value
from Base64, we obtain "client_id: client_secret," which corresponds to the exact values we provided
when configuring the `ClientCredsBuilder`.

---

### <a name="http-installation"><a/> Installation

It requires Java 21 or greater

```code

<dependency>
    <groupId>com.github.imrafaelmerino</groupId>
    <artifactId>jio-http</artifactId>
    <version>4.0.0</version>
</dependency>

```

---

## <a name="jio-test"><a/> jio-test

[![Maven](https://img.shields.io/maven-central/v/com.github.imrafaelmerino/jio-test/4.0.0)](https://search.maven.org/artifact/com.github.imrafaelmerino/jio-test/4.0.0/jar
"jio-test")

### <a name="junit"><a/> Junit integration

To enable debugging Jio provides a JUnit extension called `Debugger`. This extension offers the
flexibility to enable and configure debugging for different JIO components, such as HTTP clients and
HTTP servers from jio-http, MongoDB clients from jio-mongodb, and any expression evaluation
(jio-exp).

#### Usage Example

Here's an example of how to use the `Debugger` extension in your JUnit test class:

```java
import jio.test.junit.Debugger;
import org.junit.jupiter.api.extension.RegisterExtension;

public class MyTest {

  @RegisterExtension
  static Debugger debugger = Debugger.of(Duration.ofSeconds(2));

  // Your test methods go here
}
```

In this example, we've created a `Debugger` instance with a duration of 2 seconds. This means that
the test execution will be monitored for debugging events for a duration of 2 seconds.

#### Configuring Debugging

You can configure debugging by specifying a custom Java Flight Recorder (JFR) configuration

```code

    @RegisterExtension
    static Debugger debugger = Debugger.of("profile", Duration.ofSeconds(2));

```

For testing purposes, leaving the configuration parameter empty and using the default one is
sufficient.

There are two pre-installed configurations: Default and Profile. The Default configuration has low
overhead (about 1%). That's why it works well for continuous profiling. The Profile configuration
has overhead about 2% and can be used for more detailed application profiling.

#### Important Considerations

It's essential to set an appropriate debugging duration to avoid unnecessary delays in test
execution. Keep in mind that the test execution may not finish until the stream duration has
elapsed.

By using the Jio `Debugger` extension, you can gain valuable insights into the behavior of the
components in your tests, helping you identify and resolve issues more effectively.

---

### <a name="stubs"><a/> Stubs

#### <a name="iostubs"><a/> Creating IO Stubs

In the realm of testing, it's often necessary to construct stubs that simulate specific behaviors or
responses within your code. To address this need, the `StubBuilder` and `Gens` classes offer
practical solutions for crafting `IO` instances with tailored behaviors designed for testing
scenarios. The `StubBuilder` class empowers you to produce stubs for generating `IO` instances
through generators. These stubs offer extensive customization and are instrumental for simulating a
wide range of behaviors, spanning from successful executions to failures and controlled delays. The
`Gens` class complements this by providing a diverse set of generator methods, each adept at
generating IO instances with unique behaviors.

You can create a `StubBuilder` using various methods, depending on your testing needs:

- **`ofGen`:** Creates a stub using a generator of `IO` effects. IO generators can produce
  exceptions as normal values, which is useful for testing how your code reacts to errors.

- **`ofSucGen`:** Creates a stub using a generator of values (never fails). It's syntactic sugar for
  mapping values into effects:
  ```code
    Gen<O> gen = ???; //generator of values
    Gen<IO<O>> io = gen.map(IO::succeed) //generator of effects
  ```

You can also configure your stub as follows:

- **`withExecutor`:** Set an executor to generate values using threads from this executor. This can
  be useful for controlling the concurrency of value generation.

- **`withDelays`:** Specify delays for the stub using a generator of `Duration`. This can be useful
  for testing retry policies where retries are executed after waiting for some time.

Some examples:

```code

IO<Integer> x =
        StubBuilder.ofGen(Gen.seq(n -> n < 3 ? IO.fail(new RuntimeException(n +" is < 3"))
                                             : IO.succeed(n)
                                 )
                         )
                   .withDelays(Gen.seq(n -> n < 3 ? Duration.ofSeconds(1) : Duration.ZERO))
                   .withExecutor(Executors.newVirtualThreadPerTaskExecutor())
                   .get();

x.get(); // fails after 1 second

x.get(); // fails after 1 second

x.get(); // 3 immediatly

x.get(); // 4 immediatly

```

With these tools, you can easily create stubs and generators for testing your code with various
scenarios, behaviors, and timing conditions. This flexibility makes it easier to ensure the
robustness of your code in different situations.

Happy testing!

---

#### <a name="clockstubs"><a/> Clock stubs

The `ClockStub` class provides a simple way to create clock stubs for controlling time-related
behavior in your applications during testing and development. Clock stubs allow you to simulate
various time scenarios, making it easier to test time-dependent functionalities in your code.

##### Overview

In testing and development scenarios, controlling time can be crucial when working with applications
that have time-sensitive functionality. The `ClockStub` class is designed to create clock stubs that
mimic the behavior of actual clocks. These clock stubs give you the ability to manipulate
time-related behavior to ensure your application behaves as expected under various time conditions.

##### Creating a Clock Stub

You can create a clock stub using one of the following methods:

###### From a Reference Time

The `fromReference` static factory method allows you to create a clock stub that starts ticking from
the provided reference time. This method is useful when you want the clock to behave as if it
started at a specific instant.

```code
Instant reference = Instant.parse("2023-01-15T12:00:00.00Z");

Clock clock = ClockStub.fromReference(reference);

```

##### Using a Function

The `fromSeqCalls` static factory method enables you to create a clock stub where you can control
the ticking time based on the number of calls made to the clock. This method provides dynamic time
simulation, allowing you to simulate time progression based on your specific requirements.

```code
// Simulate time progressing by 1 hour with each call
Function<Integer, Long> timeFunction = n -> Instant.now()
                                                   .plus(Duration.ofHours(n))
                                                   .toEpochMilli();

Clock clock = ClockStub.fromCalls(timeFunction);

```

---

#### Http Server Stubs

jio-http provides dedicated stubs for various HTTP methods, allowing you to define the `HttpHandler`
of an HTTP server with precision. Here's a list of available HTTP method stubs:

- DeleteStub: Simulates the DELETE HTTP method.
- GetStub: Simulates the GET HTTP method.
- OptionsStub: Simulates the OPTIONS HTTP method.
- PatchStub: Simulates the PATCH HTTP method.
- PostStub: Simulates the POST HTTP method.
- PutStub: Simulates the PUT HTTP method.

Each of these HTTP method stubs is constructed from three core functions: `BodyStub`,
`StatusCodeStub`, and `HeadersStub`. These functions each accept four input parameters with the
following signature:

```code

IntFunction<Function<InputStream, Function<URI, Function<Headers, R>>>

```

Here's what each parameter represents:

1. The first integer parameter is the request number received by the server. In Jio-HTTP, the server
   maintains an internal counter that increments with each incoming request.

2. The `InputStream` parameter stands for the input of the request body.

3. The `URI` parameter represents the request's URI, including all query and path parameters.

4. The `Headers` parameter captures the request headers.

The type `R` can be a string in the case of `BodyStub`, an integer for `StatusCodeStub`, and headers
for `HeadersStub`.

There are some predefined `BodyStub` options:

```code
interface BodyStub {

   static BodyStub gen(Gen<String> gen);

   static BodyStub cons(String body);

   static BodyStub consAfter(String body, Duration delay);
}
```

You can create your own `BodyStub` by implementing a function like this:

```code
BodyStub myStub = reqCounter -> bodyStream -> uri -> reqHeaders -> {
    String responseBody = ???;
    // Your custom stub implementation here
    return responseBody;
}
```

Predefined `StatusCodeStub` options include:

```code
interface StatusCodeStub {

  static StatusCodeStub cons(int code);
}
```

Just like `BodyStub`, you can craft your own `StatusCodeStub` by creating a function like this:

```code
StatusCodeStub myStub = reqCounter -> bodyStream -> uri -> reqHeaders -> {
    int statusCodeResponse = ???;
    // Your custom stub implementation here
    return statusCodeResponse;
}
```

For `HeadersStub`, there are some predefined options:

```code
interface HeadersStub {

    HeadersStub EMPTY;

    static HeadersStub cons(Map<String, List<String>> map);
}
```

You can also define a custom `HeadersStub` function. For instance, here's one that returns the exact
headers of the request:

```code
HeadersStub bounceHeaders = reqCounter -> bodyStream -> uri -> reqHeaders -> reqHeaders;
```

Now, let's put it all together with a complete example:

```code
HttpHandler saludate = GetStub.of(BodyStub.consAfter("hi", Duration.ofSeconds(1)),
                                  StatusCodeStub.cons(200),
                                  HeadersStub.EMPTY
                                  );

JsObjGen personGen = ???;

HttpHandler create = PostStub.of(BodyStub.gen(personGen),
                                  StatusCodeStub.gen(Combinators.oneOf(200, 201)),
                                  HeadersStub.EMPTY
                                  );

HttpServer server = HttpServerBuilder.of(Map.of("/saludate", saludate))
                                     .startAtRandom("localhost", 8000, 9000);
```

Creating servers and adding stubs for testing purposes with jio-http is remarkably straightforward.
Here's why:

1. **Abundance of Stub Types**: Jio-HTTP offers a variety of pre-defined stubs, such as
   `DeleteStub`, `GetStub`, `OptionsStub`, and more. You can choose the stub that fits your testing
   scenario.

2. **Customizable Behavior**: With `BodyStub`, `StatusCodeStub`, and `HeadersStub`, you have
   fine-grained control over your stubs. These stubs allow you to tailor the responses, status
   codes, and headers, as needed.

3. **Simple Stub Creation**: You can create your own custom stubs with just a simple function
   implementation. This makes it easy to simulate specific behaviors or responses that your test
   cases require.

4. **Ease of Testing**: The ability to add these stubs to your `HttpServer` facilitates efficient
   and reliable testing. You can simulate various scenarios, including successes, failures, and
   delays.

5. **Integration with Generators**: The stubs can be integrated with generators to further diversify
   your test cases. For instance, you can use `JsObjGen` to generate complex JSON objects for
   testing.

In summary, jio-http simplifies the process of creating HTTP servers and adding stubs for testing
purposes. Its user-friendly design and flexibility make it a valuable tool for ensuring the
robustness and reliability of your HTTP-based applications.

---

### <a name="pbt"><a/> Property based testing

#### Quick Example: Using Property-Based Testing to Find Hard-to-Reproduce Bugs

Consider a seemingly straightforward function, medium, designed to calculate the average of two
integers.

```code

BiFunction<Integer, Integer, Integer> medium = (a, b) -> (a + b) / 2;

```

At first glance, you might think it's bug-free – after all, it's just a sum and a division. However,
in the world of software development, assumptions like this can be misleading. Bugs can lurk even in
the simplest-looking code.

Let's start by creating a generator to produce inputs, which are pairs of integers (`a` and `b`)
with the constraint that `a` is less than `b`. We can achieve this using the
[java-fun](https://github.com/imrafaelmerino/java-fun) library:

```code
Gen<Pair<Integer, Integer>> gen =
    PairGen.of(IntGen.biased(0),
               IntGen.biased(0))
          .suchThat(pair -> pair.first() < pair.second());
```

Next, we need to define a meaningful property that the `medium` function must satisfy. For example,
we can ensure that the `medium` value always stays within the bounds defined by `a` and `b`:

```code
Function<Pair<Integer, Integer>, TestResult> prop =
    pair -> {
        var a = pair.first();
        var b = pair.second();
        var mean = medium.apply(a, b);
        if (mean < a) return TestFailure.reason("mean lower than a");
        if (mean > b) return TestFailure.reason("mean greater than b");
        return TestResult.SUCCESS;
    };
```

Finally, we can create the `Property` using the `PropBuilder`:

```code
public class TestProperties {

    static BiFunction<Integer, Integer, Integer> medium = (a, b) -> (a + b) / 2;

    static Property<Pair<Integer, Integer>> mediumProperty =
            PropBuilder.of("medium",
                           gen,
                           prop
                           )
                       .withDescription("medium must fall between bounds")
                       .get();

    @Test
    public void testMedium() {
        Resport report = mediumProperty.check();
        report.assertAllSuccess();

    }
}
```

Upon executing the test, a summary of the `Report` is displayed in the console, providing essential
information about the test run. Here's what you'll see:

```text
Property medium executed 1000 times at 2023-10-15T20:23:52.879607+02:00 for 7,434 ms:
  ! KO, passed 661 tests (66.1%) and 339 tests (33.9%) ended with a failure.
  Some generated values that caused a failure:
   (2147483647, 2147483647), (1533547426, 2147483647), (1430393771, 1952366707), (361315880, 2147483647), (1810849822, 2147483647), (127, 2147483647), (1571969759, 1917332428), (32767, 2147483647), (571967758, 2131558478), (1725653506, 2147483647), (650519903, 1591944271), (720273507, 1909947616), (2147483647, 2147483647), (127, 2147483647), (32767, 2147483647), (1878197279, 2147483647), (1074371667, 1961393980), (1560183752, 2147483647), (1104900558, 1836571965), (771721558, 2147483647)
```

Additionally, JUnit provides a more detailed report. If there are any issues or failures during
testing, a message from JUnit containing the full report is printed. This comprehensive report is
invaluable for identifying and addressing any problems in your code.

```text
org.opentest4j.AssertionFailedError: Property medium with failures. JSON report: {"exceptions":[],"max_time":796000,"failures":[{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.880368Z","seq_number":1,"input":"(2147483647, 2147483647)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.882210Z","seq_number":2,"input":"(1533547426, 2147483647)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.882431Z","seq_number":9,"input":"(1430393771, 1952366707)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.882449Z","seq_number":10,"input":"(361315880, 2147483647)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.882472Z","seq_number":11,"input":"(1810849822, 2147483647)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.882499Z","seq_number":12,"input":"(127, 2147483647)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.882698Z","seq_number":17,"input":"(1571969759, 1917332428)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.882766Z","seq_number":21,"input":"(32767, 2147483647)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.882795Z","seq_number":23,"input":"(571967758, 2131558478)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.882884Z","seq_number":29,"input":"(1725653506, 2147483647)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.882913Z","seq_number":31,"input":"(650519903, 1591944271)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.882971Z","seq_number":35,"input":"(720273507, 1909947616)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.882987Z","seq_number":36,"input":"(2147483647, 2147483647)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.883034Z","seq_number":39,"input":"(127, 2147483647)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.883049Z","seq_number":40,"input":"(32767, 2147483647)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.883114Z","seq_number":43,"input":"(1878197279, 2147483647)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.883176Z","seq_number":48,"input":"(1074371667, 1961393980)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.883205Z","seq_number":50,"input":"(1560183752, 2147483647)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.883310Z","seq_number":59,"input":"(1104900558, 1836571965)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.883427Z","seq_number":68,"input":"(771721558, 2147483647)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.883443Z","seq_number":69,"input":"(731930322, 1719814114)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.883464Z","seq_number":70,"input":"(2029797654, 2147483647)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.883478Z","seq_number":71,"input":"(1394973094, 1707960802)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.883578Z","seq_number":79,"input":"(567176572, 1738145432)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.883705Z","seq_number":90,"input":"(2147483647, 2147483647)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.883720Z","seq_number":91,"input":"(1423813192, 2147483647)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.883750Z","seq_number":93,"input":"(1554444177, 2035900421)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.883766Z","seq_number":94,"input":"(2147483647, 2147483647)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.883878Z","seq_number":102,"input":"(1048619250, 2091571032)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.883963Z","seq_number":107,"input":"(1343494247, 2119894620)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.883980Z","seq_number":108,"input":"(1780942969, 2147483647)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.884084Z","seq_number":116,"input":"(706379444, 2147483647)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.884097Z","seq_number":117,"input":"(674724176, 2147483647)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.884111Z","seq_number":118,"input":"(366376675, 2147483647)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.884127Z","seq_number":119,"input":"(1152380205, 2147483647)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.884139Z","seq_number":120,"input":"(1055540429, 2147483647)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.884152Z","seq_number":121,"input":"(734494005, 2147483647)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.884302Z","seq_number":132,"input":"(909065340, 2147483647)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.884354Z","seq_number":137,"input":"(1513674269, 1559743529)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.884366Z","seq_number":138,"input":"(1620658962, 2147483647)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.884391Z","seq_number":140,"input":"(1735048665, 1744964508)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.884427Z","seq_number":141,"input":"(2017221897, 2147483647)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.884477Z","seq_number":142,"input":"(32767, 2147483647)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.884601Z","seq_number":152,"input":"(436667400, 2147483647)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.884637Z","seq_number":155,"input":"(505112404, 2147483647)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.884660Z","seq_number":157,"input":"(2147483647, 2147483647)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.884674Z","seq_number":158,"input":"(127, 2147483647)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.884728Z","seq_number":164,"input":"(127, 2147483647)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.884741Z","seq_number":165,"input":"(218907640, 2147483647)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.884765Z","seq_number":167,"input":"(2077759235, 2147483647)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.884797Z","seq_number":170,"input":"(2004905038, 2147483647)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.884839Z","seq_number":172,"input":"(2147483647, 2147483647)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.884851Z","seq_number":173,"input":"(382907835, 1848300132)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.884952Z","seq_number":183,"input":"(1414453865, 2147483647)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.885072Z","seq_number":195,"input":"(622296040, 1799782576)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.885087Z","seq_number":196,"input":"(1079508732, 1613762528)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.885132Z","seq_number":201,"input":"(1017853265, 1645045517)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.885175Z","seq_number":205,"input":"(2147483647, 2147483647)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.885189Z","seq_number":206,"input":"(32767, 2147483647)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.885214Z","seq_number":208,"input":"(808300701, 1508868391)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.885237Z","seq_number":210,"input":"(2147483647, 2147483647)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.885249Z","seq_number":211,"input":"(127, 2147483647)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.885261Z","seq_number":212,"input":"(1280816822, 1838911650)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.885391Z","seq_number":214,"input":"(1176996537, 1552966381)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.885658Z","seq_number":220,"input":"(534655680, 1682705416)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.885757Z","seq_number":223,"input":"(840735221, 1865377218)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.885837Z","seq_number":226,"input":"(127, 2147483647)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.886003Z","seq_number":232,"input":"(127, 2147483647)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.886071Z","seq_number":235,"input":"(2110462688, 2147483647)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.886112Z","seq_number":240,"input":"(2147483647, 2147483647)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.886126Z","seq_number":241,"input":"(2147483647, 2147483647)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.886185Z","seq_number":248,"input":"(330895231, 2147483647)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.886243Z","seq_number":255,"input":"(2006720972, 2147483647)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.886320Z","seq_number":259,"input":"(1039274967, 2096915296)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.886336Z","seq_number":260,"input":"(127, 2147483647)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.886363Z","seq_number":263,"input":"(32767, 2147483647)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.886374Z","seq_number":264,"input":"(1771073553, 1909337551)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.886429Z","seq_number":271,"input":"(2147483647, 2147483647)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.886440Z","seq_number":272,"input":"(1393438628, 2147483647)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.886472Z","seq_number":275,"input":"(1920536036, 1977100767)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.886484Z","seq_number":276,"input":"(1560328013, 1829719404)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.886513Z","seq_number":279,"input":"(1868735802, 2147483647)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.886523Z","seq_number":280,"input":"(2091377121, 2147483647)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.886548Z","seq_number":283,"input":"(127, 2147483647)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.886565Z","seq_number":285,"input":"(1911190342, 2147483647)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.886580Z","seq_number":286,"input":"(1423433283, 2118621215)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.886608Z","seq_number":289,"input":"(1156816267, 1891419274)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.886618Z","seq_number":290,"input":"(2084155731, 2147483647)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.886629Z","seq_number":291,"input":"(688685438, 2082711398)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.886647Z","seq_number":293,"input":"(2147483647, 2147483647)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.886666Z","seq_number":295,"input":"(710722643, 1943259540)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.886685Z","seq_number":297,"input":"(1989883084, 2147483647)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.886718Z","seq_number":301,"input":"(32767, 2147483647)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.886737Z","seq_number":303,"input":"(952934441, 1264833469)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.886747Z","seq_number":304,"input":"(1479506487, 2144218067)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.886761Z","seq_number":305,"input":"(995701763, 2147483647)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.886780Z","seq_number":307,"input":"(1065422693, 1141060350)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.886797Z","seq_number":309,"input":"(838787512, 1570227631)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.886837Z","seq_number":313,"input":"(1922938597, 2147483647)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.886847Z","seq_number":314,"input":"(1384459064, 2147483647)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.886879Z","seq_number":318,"input":"(2103714568, 2147483647)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.886977Z","seq_number":326,"input":"(629883470, 2147483647)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.886995Z","seq_number":328,"input":"(1222774129, 2147483647)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.887188Z","seq_number":333,"input":"(436704288, 2147483647)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.887316Z","seq_number":338,"input":"(1416215288, 1528151612)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.887365Z","seq_number":340,"input":"(1944946961, 2147483647)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.887514Z","seq_number":345,"input":"(1382945771, 1736161785)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.887758Z","seq_number":348,"input":"(1036530737, 1130341056)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.887929Z","seq_number":353,"input":"(1117591028, 2147483647)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.887995Z","seq_number":354,"input":"(1051688088, 2147483647)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.888037Z","seq_number":357,"input":"(32767, 2147483647)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.888068Z","seq_number":361,"input":"(868669640, 1832432901)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.888083Z","seq_number":363,"input":"(882929322, 1696248740)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.888166Z","seq_number":375,"input":"(1033032313, 1470736814)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.888192Z","seq_number":379,"input":"(113260360, 2147483647)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.888216Z","seq_number":382,"input":"(127, 2147483647)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.888261Z","seq_number":387,"input":"(855281174, 1466139241)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.888275Z","seq_number":389,"input":"(2147483647, 2147483647)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.888310Z","seq_number":395,"input":"(1519370977, 2147483647)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.888328Z","seq_number":397,"input":"(1976125086, 2147483647)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.888359Z","seq_number":402,"input":"(986813649, 1391802775)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.888389Z","seq_number":407,"input":"(127, 2147483647)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.888416Z","seq_number":411,"input":"(970877341, 1954125251)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.888425Z","seq_number":412,"input":"(32767, 2147483647)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.888455Z","seq_number":417,"input":"(805566230, 2147483647)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.888492Z","seq_number":421,"input":"(882645466, 1948381384)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.888504Z","seq_number":422,"input":"(32767, 2147483647)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.888531Z","seq_number":425,"input":"(127, 2147483647)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.888625Z","seq_number":427,"input":"(1299405979, 2147483647)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.888647Z","seq_number":429,"input":"(982556170, 2147483647)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.888725Z","seq_number":443,"input":"(1740380870, 2147483647)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.888734Z","seq_number":444,"input":"(2147483647, 2147483647)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.888743Z","seq_number":445,"input":"(344239145, 2147483647)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.888759Z","seq_number":447,"input":"(906361824, 1942743697)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.888769Z","seq_number":448,"input":"(425551720, 2147483647)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.888792Z","seq_number":452,"input":"(1155656966, 2147483647)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.888803Z","seq_number":453,"input":"(907954938, 2147483647)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.888823Z","seq_number":456,"input":"(518529371, 1855173894)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.888859Z","seq_number":462,"input":"(836410708, 1957909592)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.888874Z","seq_number":464,"input":"(903880823, 2147483647)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.888882Z","seq_number":465,"input":"(616202131, 2133953153)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.888922Z","seq_number":470,"input":"(923678298, 1806218916)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.888937Z","seq_number":472,"input":"(127, 2147483647)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.888946Z","seq_number":473,"input":"(2147483647, 2147483647)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.888954Z","seq_number":474,"input":"(189462362, 2098996680)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.889015Z","seq_number":485,"input":"(1051312913, 1499092845)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.889055Z","seq_number":490,"input":"(805804501, 1945212418)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.889064Z","seq_number":491,"input":"(277242231, 2147483647)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.889080Z","seq_number":493,"input":"(1317055049, 1998339480)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.889102Z","seq_number":496,"input":"(482842086, 2147483647)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.889111Z","seq_number":497,"input":"(32767, 2147483647)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.889119Z","seq_number":498,"input":"(444387494, 2146872001)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.889128Z","seq_number":499,"input":"(1617230533, 2147483647)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.889137Z","seq_number":500,"input":"(2147483647, 2147483647)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.889154Z","seq_number":501,"input":"(91280385, 2147483647)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.889163Z","seq_number":502,"input":"(1454427132, 1661157669)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.889183Z","seq_number":504,"input":"(269008726, 2124929106)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.889197Z","seq_number":506,"input":"(1307767957, 1391173566)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.889214Z","seq_number":509,"input":"(893786159, 1964223850)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.889226Z","seq_number":510,"input":"(32767, 2147483647)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.889490Z","seq_number":513,"input":"(1166711722, 1251978122)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.889539Z","seq_number":514,"input":"(1043382459, 1707616638)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.889569Z","seq_number":515,"input":"(270411969, 2147483647)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.889812Z","seq_number":517,"input":"(32767, 2147483647)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.889908Z","seq_number":519,"input":"(2147483647, 2147483647)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.889945Z","seq_number":523,"input":"(127, 2147483647)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.889969Z","seq_number":527,"input":"(127, 2147483647)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.889984Z","seq_number":529,"input":"(1324763053, 1736659860)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.889996Z","seq_number":530,"input":"(2147483647, 2147483647)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.890040Z","seq_number":538,"input":"(2147483647, 2147483647)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.890054Z","seq_number":540,"input":"(1135649012, 1834245677)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.890069Z","seq_number":541,"input":"(602837450, 2141544313)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.890090Z","seq_number":544,"input":"(41896800, 2147483647)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.890101Z","seq_number":545,"input":"(32767, 2147483647)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.890115Z","seq_number":547,"input":"(37227507, 2147483647)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.890129Z","seq_number":549,"input":"(1440018474, 1958760257)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.890145Z","seq_number":551,"input":"(1697124112, 1944740028)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.890183Z","seq_number":558,"input":"(1295204224, 2137668739)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.890211Z","seq_number":562,"input":"(1550938271, 1932209568)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.890243Z","seq_number":567,"input":"(1738722138, 2147483647)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.890251Z","seq_number":568,"input":"(713621152, 1780866493)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.890265Z","seq_number":570,"input":"(69642088, 2147483647)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.890273Z","seq_number":571,"input":"(551912087, 2052962274)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.890282Z","seq_number":572,"input":"(1214726683, 1504893506)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.890292Z","seq_number":573,"input":"(127, 2147483647)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.890301Z","seq_number":574,"input":"(2147483647, 2147483647)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.890319Z","seq_number":577,"input":"(578265987, 2147483647)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.890337Z","seq_number":580,"input":"(32767, 2147483647)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.890351Z","seq_number":582,"input":"(2147483647, 2147483647)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.890375Z","seq_number":586,"input":"(1066328829, 2147483647)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.890407Z","seq_number":591,"input":"(285273422, 2147483647)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.890416Z","seq_number":592,"input":"(1413157748, 2098424603)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.890430Z","seq_number":594,"input":"(407555756, 2147483647)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.890446Z","seq_number":596,"input":"(851447433, 2092038634)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.890459Z","seq_number":598,"input":"(2147483647, 2147483647)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.890517Z","seq_number":609,"input":"(773270128, 2147483647)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.890540Z","seq_number":613,"input":"(2139444449, 2147483647)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.890554Z","seq_number":615,"input":"(127, 2147483647)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.890562Z","seq_number":616,"input":"(127, 2147483647)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.890570Z","seq_number":617,"input":"(1620792408, 2147483647)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.890580Z","seq_number":618,"input":"(1583038973, 2147483647)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.890599Z","seq_number":621,"input":"(127, 2147483647)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.890617Z","seq_number":624,"input":"(1797938107, 2147483647)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.890630Z","seq_number":626,"input":"(39190679, 2147483647)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.890639Z","seq_number":627,"input":"(32767, 2147483647)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.890655Z","seq_number":629,"input":"(1535588156, 2147483647)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.890664Z","seq_number":630,"input":"(707958319, 2147483647)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.890673Z","seq_number":631,"input":"(1016134140, 2147483647)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.890682Z","seq_number":632,"input":"(32767, 2147483647)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.890690Z","seq_number":633,"input":"(718701334, 2015075069)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.890709Z","seq_number":636,"input":"(639752449, 2147483647)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.890722Z","seq_number":638,"input":"(1026759501, 1816073653)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.890743Z","seq_number":640,"input":"(755208510, 1630134993)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.890776Z","seq_number":645,"input":"(1087073212, 2000648031)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.890796Z","seq_number":648,"input":"(2107938535, 2132599319)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.890819Z","seq_number":652,"input":"(2147483647, 2147483647)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.890827Z","seq_number":653,"input":"(380412771, 2147483647)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.890836Z","seq_number":654,"input":"(587368522, 2033125475)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.890880Z","seq_number":662,"input":"(32767, 2147483647)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.890889Z","seq_number":663,"input":"(32767, 2147483647)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.890918Z","seq_number":668,"input":"(1949618185, 2147483647)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.890931Z","seq_number":670,"input":"(956826446, 2147483647)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.890946Z","seq_number":672,"input":"(32767, 2147483647)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.890977Z","seq_number":678,"input":"(942928282, 1371882480)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.890990Z","seq_number":680,"input":"(894268799, 2147483647)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.890999Z","seq_number":681,"input":"(32767, 2147483647)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.891012Z","seq_number":682,"input":"(378486568, 1861852690)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.891042Z","seq_number":686,"input":"(874459943, 2093323680)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.891051Z","seq_number":687,"input":"(436802747, 1924781651)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.891059Z","seq_number":688,"input":"(1790563692, 1935297081)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.891067Z","seq_number":689,"input":"(1348194523, 2147483647)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.891104Z","seq_number":692,"input":"(858483285, 2147483647)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.891129Z","seq_number":696,"input":"(2147483647, 2147483647)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.891138Z","seq_number":697,"input":"(1214300513, 1226918032)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.891151Z","seq_number":699,"input":"(683765884, 2147483647)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.891163Z","seq_number":701,"input":"(545407906, 1736824987)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.891172Z","seq_number":702,"input":"(900934545, 1702720800)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.891199Z","seq_number":707,"input":"(1192003054, 1288261712)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.891207Z","seq_number":708,"input":"(565991852, 2147483647)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.891242Z","seq_number":715,"input":"(738007098, 1425430498)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.891263Z","seq_number":719,"input":"(1306676240, 1491381364)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.891275Z","seq_number":721,"input":"(32767, 2147483647)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.891283Z","seq_number":722,"input":"(32767, 2147483647)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.891291Z","seq_number":723,"input":"(32767, 2147483647)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.891304Z","seq_number":725,"input":"(568574277, 1973911480)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.891313Z","seq_number":726,"input":"(1866109956, 1953496580)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.891321Z","seq_number":727,"input":"(2147483647, 2147483647)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.891337Z","seq_number":730,"input":"(2147483647, 2147483647)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.891369Z","seq_number":736,"input":"(684314658, 2147483647)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.891386Z","seq_number":739,"input":"(127, 2147483647)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.891402Z","seq_number":742,"input":"(993182651, 1783676486)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.891427Z","seq_number":747,"input":"(1486492496, 1940960434)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.891450Z","seq_number":751,"input":"(32767, 2147483647)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.891457Z","seq_number":752,"input":"(32767, 2147483647)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.891465Z","seq_number":753,"input":"(32839816, 2147483647)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.891504Z","seq_number":761,"input":"(1213027194, 1323414570)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.891547Z","seq_number":766,"input":"(774507716, 2147483647)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.891557Z","seq_number":767,"input":"(1016203779, 2147483647)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.891574Z","seq_number":768,"input":"(263026299, 2147483647)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.891609Z","seq_number":770,"input":"(127, 2147483647)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.891636Z","seq_number":773,"input":"(534408511, 2147483647)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.891644Z","seq_number":774,"input":"(780687568, 2147483647)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.891678Z","seq_number":781,"input":"(436114393, 1846777799)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.891692Z","seq_number":783,"input":"(32767, 2147483647)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.891713Z","seq_number":787,"input":"(214238791, 2147483647)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.891747Z","seq_number":794,"input":"(865896003, 2147483647)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.891969Z","seq_number":801,"input":"(1267488726, 2147483647)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.892010Z","seq_number":802,"input":"(429573759, 2147483647)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.892085Z","seq_number":804,"input":"(1482568316, 1576959952)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.892187Z","seq_number":807,"input":"(844380207, 2147483647)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.892305Z","seq_number":815,"input":"(2147483647, 2147483647)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.892319Z","seq_number":816,"input":"(32767, 2147483647)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.892351Z","seq_number":822,"input":"(1104582403, 1922248364)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.892367Z","seq_number":824,"input":"(2147483647, 2147483647)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.892384Z","seq_number":827,"input":"(127, 2147483647)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.892414Z","seq_number":833,"input":"(32767, 2147483647)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.892421Z","seq_number":834,"input":"(1451241867, 1624341585)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.892432Z","seq_number":835,"input":"(1090749970, 2147483647)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.892452Z","seq_number":839,"input":"(127, 2147483647)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.892464Z","seq_number":841,"input":"(2147483647, 2147483647)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.892472Z","seq_number":842,"input":"(111986732, 2147483647)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.892480Z","seq_number":843,"input":"(1505837398, 1967307454)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.892494Z","seq_number":845,"input":"(32767, 2147483647)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.892502Z","seq_number":846,"input":"(1260787896, 2037136929)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.892515Z","seq_number":848,"input":"(127, 2147483647)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.892544Z","seq_number":854,"input":"(2147483647, 2147483647)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.892572Z","seq_number":857,"input":"(1529152543, 2121232889)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.892584Z","seq_number":859,"input":"(167160142, 2147483647)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.892598Z","seq_number":861,"input":"(936522204, 1966244605)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.892610Z","seq_number":863,"input":"(953060657, 1213407250)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.892808Z","seq_number":876,"input":"(1663866546, 2147483647)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.892824Z","seq_number":877,"input":"(2147483647, 2147483647)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.892833Z","seq_number":878,"input":"(610223755, 2147483647)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.892850Z","seq_number":880,"input":"(1622538860, 1982476399)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.892865Z","seq_number":883,"input":"(1095492927, 2024002429)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.892877Z","seq_number":885,"input":"(1228064703, 2147483647)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.892913Z","seq_number":891,"input":"(612987569, 2147483647)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.892927Z","seq_number":893,"input":"(1377208541, 2147483647)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.892945Z","seq_number":896,"input":"(832044347, 2061995995)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.893010Z","seq_number":900,"input":"(127, 2147483647)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.893030Z","seq_number":904,"input":"(1400023269, 2147483647)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.893043Z","seq_number":906,"input":"(2144941239, 2147483647)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.893069Z","seq_number":912,"input":"(1766078309, 2147483647)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.893077Z","seq_number":913,"input":"(1395540578, 2015357750)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.893084Z","seq_number":914,"input":"(1022812679, 2101600553)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.893094Z","seq_number":915,"input":"(2147483647, 2147483647)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.893112Z","seq_number":919,"input":"(863738927, 2147483647)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.893124Z","seq_number":921,"input":"(747538073, 1940070970)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.893156Z","seq_number":927,"input":"(1499271332, 2147483647)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.893167Z","seq_number":928,"input":"(1629081796, 2147483647)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.893182Z","seq_number":931,"input":"(768103063, 2147483647)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.893197Z","seq_number":934,"input":"(1494904277, 1924084632)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.893205Z","seq_number":935,"input":"(1723488605, 2147483647)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.893218Z","seq_number":937,"input":"(127, 2147483647)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.893239Z","seq_number":940,"input":"(2147483647, 2147483647)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.893260Z","seq_number":945,"input":"(488587306, 2023394003)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.893268Z","seq_number":946,"input":"(598474836, 1724197832)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.893275Z","seq_number":947,"input":"(1164446800, 1583907093)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.893292Z","seq_number":950,"input":"(101347090, 2139479838)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.893317Z","seq_number":956,"input":"(1082871153, 1531510322)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.893326Z","seq_number":957,"input":"(2147483647, 2147483647)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.893346Z","seq_number":961,"input":"(1513907009, 2071928523)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.893354Z","seq_number":962,"input":"(717055627, 2147483647)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.893362Z","seq_number":963,"input":"(2147483647, 2147483647)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.893381Z","seq_number":967,"input":"(2147483647, 2147483647)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.893405Z","seq_number":972,"input":"(32767, 2147483647)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.893423Z","seq_number":976,"input":"(32767, 2147483647)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.893434Z","seq_number":978,"input":"(1604943254, 1956899451)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.893445Z","seq_number":980,"input":"(752835236, 1740776594)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.893461Z","seq_number":983,"input":"(536848588, 2147483647)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.893479Z","seq_number":987,"input":"(328953301, 2147483647)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.893487Z","seq_number":988,"input":"(2041725148, 2147483647)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.893504Z","seq_number":991,"input":"(915426351, 1384635069)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.893512Z","seq_number":992,"input":"(310305665, 2043297547)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.893525Z","seq_number":994,"input":"(85861090, 2147483647)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.893532Z","seq_number":995,"input":"(1262801644, 2147483647)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.893540Z","seq_number":996,"input":"(2147483647, 2147483647)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.893547Z","seq_number":997,"input":"(127, 2147483647)","seed":-8287889810276844822,"tags":""}},{"reason":"mean lower than a","context":{"start":"2023-10-15T18:23:52.893554Z","seq_number":998,"input":"(396828458, 2147483647)","seed":-8287889810276844822,"tags":""}}],"start_time":"2023-10-15T18:23:52.879607Z","name":"medium","n_tests":1000,"end_time":"2023-10-15T18:23:52.893568Z","n_exceptions":0,"accumulative_time":7434000,"avg_time":7434,"min_time":1000,"n_failures":339,"description":""} ==>

```

You might not anticipate a failure in such a straightforward function, but don't be too concerned.
It's worth noting that this issue has persisted for a considerable period in various programming
languages, even affecting binary search algorithms. For more insights into this matter, I recommend
reading the detailed account provided by Joshua Bloch in his informative post: [Google Research
Blog](https://blog.research.google/2006/06/extra-extra-read-all-about-it-nearly.html).

It's important to mention that, as of now, jio-test doesn't include a feature called "shrinking,"
which is a technique used to minimize the failing example to its simplest form. However, the
framework does offer methods to help you identify the reasons behind test failures. One useful
practice is to classify the generated data. In the example you've shared, it's evident that larger
numbers, specifically 2147483647 (which corresponds to `Integer.MAX_VALUE`), are causing failures
more frequently. This is because biased integer generators tend to produce this value with a higher
probability.

To address this, you can classify the generated data by adding tags to the pairs `(a,b)` based on
their characteristics:

- "both" for pairs where both values are greater than `Integer.MAX_VALUE / 2`.
- "none" for pairs where neither value exceeds `Integer.MAX_VALUE / 2`.
- "one" for pairs where only one element of the pair surpasses `Integer.MAX_VALUE / 2."

This classification allows you to gain better insights into the nature of test failures and helps
you focus on specific scenarios where problems are more likely to occur. This can be incredibly
powerful for spotting and addressing bugs that might be extremely challenging to reproduce
otherwise.

```
static Property<Pair<Integer, Integer>> mediumProperty =
            PropBuilder.of("medium",
                           gen,
                           prop
                           )
                       .withClassifiers(Map.of("both",
                                                p -> p.first() > Integer.MAX_VALUE / 2
                                                    && p.second() > Integer.MAX_VALUE / 2,
                                               "none",
                                                p -> p.first() < Integer.MAX_VALUE / 2
                                                    && p.second() < Integer.MAX_VALUE / 2
                                                ),
                                         "one"
                                        )
                       .withDescription("medium must fall between bounds")
                       .get();


```

Now, if we examine the output, you'll notice the following:

```text
Property medium executed 1000 times at 2023-10-15T20:28:11.580574+02:00 for 19,361 ms:
  ! KO, passed 674 tests (67,4 %) and 326 (32,6 %) ended with a failure.
  Some generated values that caused a failure:
   ((32767, 2147483647), one),((2147483647, 2147483647), both),((127, 2147483647), one),((32767, 2147483647), one),((1367738157, 2147483647), both),((32767, 2147483647), one),((2147483647, 2147483647), both),((1365877821, 1519800076), both),((929386296, 2147483647), one),((1663592320, 2147483647), both),((687170227, 2147483647), one),((127, 2147483647), one),((127, 2147483647), one),((32767, 2147483647), one),((686789583, 1494899307), one),((261853601, 2066273042), one),((23902795, 2147483647), one),((2147483647, 2147483647), both),((1746060008, 2147483647), both),((32767, 2147483647), one)
  1000 values collected in total:
     44,4 % one
     40,7 % none
     14,9 % both
```

From this output, you can see that there were no test failures when the "none" tag was applied to
the generated values. However, when at least one element, either `a` or `b`, is greater than
`Integer.MAX_VALUE / 2`, it may result in an overflow. If you read Joshua Bloch's article, you'll
recognize that the correct way to calculate the medium is as follows:

By changing the implementation of the `medium` property, you can avoid the error:

```code

static BiFunction<Integer, Integer, Integer> medium = (a,b) -> a+(b-a)/2;

// Alternatively
static BiFunction<Integer, Integer, Integer> medium = (a,b) -> (a+b)>>>1;
```

With this corrected implementation, there are no test failures:

```text
Property medium executed 1000 times at 2023-10-15T20:34:15.031869+02:00 for 15,734 ms:
  + OK, passed 1000 tests.
  1000 values collected in total:
     45.8 % one
     39.7 % none
     14.5 % both
```

This change ensures that the average of two integers is calculated correctly, and all tests pass
without any failures.

#### Introduction

In the realm of property-based testing, a "property" is a fundamental concept representing a
condition or invariant that a piece of code or a program should always satisfy, without failing
under any circumstances. These properties serve as essential checks to ensure the correctness and
reliability of software.

A "Property" is represented in jio-test by a class called `Property`. This class encapsulates a
specific property that you want to test. The primary components of a `Property` include:

- **Name:** A descriptive label that identifies the property being tested.
- **Data Generator (`Gen<O>`):** A generator that produces pseudorandom data of type `O`. This data
  is used to feed the property tests.
- **Testing Function or Lambda:** A function or lambda that tests the property, taking the generated
  data as input and producing a `TestResult`, which indicates whether the property holds or fails.

Let's look at the key elements that constitute a `Property`:

##### Name

The name provides a descriptive label for the property being tested. It should succinctly describe
the behavior or condition that the property is checking.

##### Data Generator

The data generator, represented by a `java.fun.Gen<O>` object, is responsible for creating
pseudorandom data. This data serves as input for property testing. The quality and diversity of the
generated data play a crucial role in the effectiveness of property-based testing. In jio-test, I
utilize the data generators provided by the `java-fun` library. These generators can create a wide
range of data types, from simple values like integers and strings to more complex data structures.

##### Biased Generators for Exploratory Testing

For exploratory purposes, it's often recommended to use biased generators. Biased generators assign
higher probabilities to values that are known to produce more bugs or exceptional cases. For
example, consider values like zero, empty strings, blank strings, `Integer.MAX_VALUE`, and more.
Biased generators can help you uncover hidden issues in your code by focusing on these critical
cases during testing.

##### Testing Function

The testing function is a critical part of the `Property`. It is the code or logic that evaluates
whether the property holds true for the generated data. There are two primary ways to define the
testing function:

1. **Function (`Function<O, TestResult>`):** This form of the testing function takes the generated
   data (`O`) and returns a `TestResult`. The `TestResult` indicates the success or failure of the
   property test.

2. **Lambda (`Lambda<O, TestResult>`):** Certain properties may be defined using Lambdas instead of
   functions, especially when they involve IO operations where exceptions can occur. In such cases,
   these exceptions do not halt the property's execution but are considered and reported in the
   final test report.

#### Creating a Property

Creating a property involves creating a `PropBuilder` instance to build the property you want to
test:

- `PropBuilder.of(String name, Gen<O> gen, Function<O, TestResult> property)`: Use this method when
  your testing function takes generated data (`O`) as input. This form of the testing function is
  useful when no additional configuration is needed for the property test.

- `PropBuilder.ofLambda(String name, Gen<O> gen, Lambda<O, TestResult> property)`: When your testing
  function is a lambda that only requires generated data (`O`), this method is the appropriate
  choice.

With the builder instance you can further customize the property by specifying the number of test
executions, providing a description, setting classifiers, and more.

#### Using Classifiers for Categorization

Exploratory testing can benefit from classifying generated data into different categories.
Classifiers are created using the
`withClassifiers(Map<String, Predicate<O>> classifiers, String defaultTag)` method of the builder.
These classifiers can help you group data into various categories based on specific criteria and
identify tags assigned to values that produce errors.

#### Collecting Data for Analysis

To gather data for debugging and analysis, you can enable data collection using the
`withCollector()` method of the builder. This feature allows you to collect data about the generated
values, helping you identify patterns or trends in the generated data.

#### Analyzing Results

After executing a property, you obtain a `Report` containing detailed information about the test
execution. The `Report` class has the following fields and their meanings (which can be serialized
into a JSON format):

- `name`: The name of the property.
- `n_tests`: The number of executed tests.
- `n_failures`: The number of test failures.
- `n_exceptions`: The number of exceptions raised during testing.
- `description`: The description of the property.
- `start_time`: The start time of test execution.
- `end_time`: The end time of test execution.
- `avg_time`: The average execution time in milliseconds.
- `max_time`: The maximum execution time in milliseconds.
- `min_time`: The minimum execution time in milliseconds.
- `accumulative_time`: The accumulative execution time in milliseconds.
- `failures`: An array of failure contexts, each containing a reason and context.
- `exceptions`: An array of exception contexts, each containing a message, type, and stack trace.

Exceptions or failures has an associated context (`Context` class) with the following fields:

- `start`: Represents the instant when a test starts. This timestamp is essential for tracking the
  timing of test execution.
- `seed`: Signifies the seed for random data generation. This seed is crucial for reproducing bugs
  since pseudo-random generators always produce the same sequence of values when fed with the same
  seed. It ensures the ability to recreate the exact data sequence.
- `generatedSeqNumber`: Denotes the sequence number for data generation. This number helps in
  understanding the order of data generation and identifying patterns or issues in the data.
- `input`: Represents the input data of the test. This field provides insight into the specific data
  that was used during a test execution. It's important for analyzing the test's behavior and
  identifying problematic inputs.
- `tags`: Contains a string that can be used to categorize the input data, based on classifiers or
  conditions. This information helps in identifying and categorizing specific inputs and associating
  them with potential issues.

These fields collectively provide valuable context information for each test execution. In
particular, the `seed` and `generatedSeqNumber` fields are vital for reproducing bugs because they
allow you to precisely recreate the sequence of random data that led to a specific issue.

By examining the `Report` class, you can gain valuable insights into the performance of your
property-based tests, identify failures, and pinpoint exceptions.

The `Report` class defines several methods that are used for assertions in JUnit tests. Here's a
breakdown of these methods and how they are used for assertions:

1. `assertAllSuccess()`: This method is used to assert that all tests associated with the report
   have passed successfully. If there are any failures or exceptions, this assertion will fail. It
   checks if both `getExceptions()` and `getFailures()` lists are empty. If they are not empty, it
   raises an assertion failure with a message indicating the presence of failures and exceptions.

2. `assertNoFailures()`: This method is used to assert that there are no failures associated with
   the report. If there are any failures, this assertion will fail. It checks if the `getFailures()`
   list is empty. If it's not empty, it raises an assertion failure with a message indicating the
   presence of failures.

3. `assertThat(Predicate<Report> condition, Supplier<String> message)`: This is a custom assertion
   method that allows for a user-defined condition to be evaluated against the report. The
   `condition` parameter is a predicate that is applied to the report, and the `message` parameter
   is a supplier that provides a message to be used if the condition fails. If the condition is not
   satisfied, an assertion failure is raised with the message supplied by the `message` supplier.

These methods are used in unit tests to verify the correctness of property-based tests and to check
if the reported results match the expected outcomes. They help ensure that the properties defined in
property-based tests hold true and that the tests are executed without failures or exceptions.

#### Exporting Reports

The results of property testing can be exported to a file using the `withExportPath(Path path)`
method. This file contains a JSON representation of the test report, which is useful for sharing and
archiving test results.

The `Property` class is part of a property-based testing framework in Java. It represents a property
of a piece of code that should always be true. The class allows you to define, customize, execute,
and report on property tests.

Here, I'll explain two methods within the `Property` class: `repeatSeq` and `repeatPar`.

#### `repeatSeq(int n)`

This method returns a new `Property` instance that represents the property and specifies that it
should be executed sequentially for the specified number of times (`n`).

- `n` (int): The number of sequential executions for the property.

Use Case:

- This method is useful when you want to run the property test multiple times in a sequential
  manner, one after the other. It's suitable for situations where you don't require parallel
  execution and want to ensure the property holds true consistently.

#### `repeatPar(int n)`

This method returns a new `Property` instance that represents the property and specifies that it
should be executed in parallel for the specified number of times (`n`). Parallel execution involves
running the property tests concurrently using multiple threads from the common ForkJoinPool.

- `n` (int): The number of parallel executions for the property.

Use Case:

- When you want to take advantage of parallelism to speed up the property-based testing process, you
  can use this method. It's beneficial for running multiple property tests concurrently, which can
  be more time-efficient.
- Provides the ability to conduct property tests in parallel, executing them a specified number of
  times. Parallel property testing can be a valuable tool for detecting bugs related to concurrency,
  as it runs multiple tests concurrently using multiple threads. This can help uncover issues that
  might not be apparent in sequential testing, making it particularly useful for identifying and
  addressing concurrency-related defects in your code.

In both cases, the original `Property` instance is not modified, and a new instance is returned with
the specified execution mode.

These methods allow you to customize how property tests are executed, whether sequentially or in
parallel, depending on your testing requirements and constraints. They provide flexibility in
adapting the testing strategy to the specific needs of your codebase or project.

---

## <a name="test-installation"><a/> Installation

It requires Java 21 or greater

```code

<dependency>
    <groupId>com.github.imrafaelmerino</groupId>
    <artifactId>jio-test</artifactId>
    <version>4.0.0</version>
</dependency>

```

## <a name="jio-mongodb"><a/> jio-mongodb

[![Maven](https://img.shields.io/maven-central/v/com.github.imrafaelmerino/jio-mongodb/4.0.0)](https://search.maven.org/artifact/com.github.imrafaelmerino/jio-mongodb/4.0.0/jar
"jio-mongodb")

`jio-mongodb` leverages the persistent JSON from
[json-values](https://github.com/imrafaelmerino/json-values) and the set of codecs defined in
[mongo-values](https://github.com/imrafaelmerino/mongo-values), making it an efficient solution for
MongoDB operations.

It uses Virtual threads from Java 21.

jio-mongodb is composed of a set of MongoLambdas to perform operations against the database. With
Lambdas, you can benefit from all the powerful features of `jio-exp` and `jio-test`. `jio-mongodb`
is an example of how you can make any API under the sun jio-friendly, unleashing the full potential
of your code.

### <a name="monglambda"><a/> MongoLambda

The `MongoLambda` interface in Jio provides a versatile way to define MongoDB operations that
produce IO effects within a MongoDB client session. These lambdas can be used with or without
transactions, offering flexibility in working with MongoDB databases.

The `MongoLambda<I, O>` interface represents a function that takes an input of type `I` and produces
an IO effect of type `O` within a MongoDB client session.

```code

interface MongoLambda<I, O> extends BiLambda<ClientSession, I, O> {

      Lambda<I, O> standalone();
      <B> MongoLambda<I, B> then(MongoLambda<O, B> other) { }
      <B> MongoLambda<I, B> then(Lambda<O, B> other) {}
      <C> MongoLambda<I, C> map(Function<O, C> fn) {}
}

```

Key points:

- Using transactions with MongoDB is optional. You can create a `MongoLambda` that produces effects
  independently of any transaction by calling the `standalone()` method. This is useful when you
  want to perform operations that don't require transactional behavior.

- One powerful feature of `MongoLambda` is the ability to chain operations together. You can chain
  this interface with another `MongoLambda` to create a new `MongoLambda`. The result is a sequence
  of operations executed within the same MongoDB client session, with the output of one operation
  becoming the input to the next one.

- You can also chain a `MongoLambda` with a non-transactional `Lambda` to create a new
  `MongoLambda`. This allows you to mix transactional and non-transactional operations while
  maintaining session continuity.

- The `MongoLambda` interface lets you map the output of an operation using a provided function. You
  can create a new `MongoLambda` where the mapping function is applied to the output of the original
  operation.

This allows you to create flexible and expressive MongoDB operations with transactional or
non-transactional behavior as required.

In the upcoming sections, we'll explore a variety of pre-defined `MongoLambda` functions that are
available in the `jio-mongodb`.

---

### <a name="jio-mongodb-gs"><a/> API

To get started, you need a `MongoClient`, a `DatabaseBuilder`, and finally a `CollectionBuilder`
that provides access to a MongoDB collection. Below is an example of how to create both:

```code
String connectionStr = "mongodb://localhost:27017,localhost:27018,localhost:27019/?replicaSet=rs0"

MongoClient mongoClient = MongoClientBuilder.DEFAULT.build(connectionStr);

String databaseName = "test";

DatabaseBuilder database =  DatabaseBuilder.of(mongoClient,databaseName);

String collectionName = "Data";

CollectionBuilder collection = CollectionBuilder.of(database,collectionName);
```

The `MongoClientBuilder` class is for creating MongoDB client instances with custom configurations.
This class provides flexibility in building MongoDB client instances and allows you to specify your
own connection string and settings functions. The default instance, `DEFAULT` is a pre-configured
builder with the default settings and codecs from
[mongo-values](https://github.com/imrafaelmerino/mongo-values) to work with JSON data from the
[json-values](https://github.com/imrafaelmerino/json-values) library.

Now that you have a `CollectionBuilder`, you can perform various operations on it.

#### <a name="find-operations"><a/> Find Operations

The key class for creating queries and specifying options for find operations is `FindBuilder`.
Here's how to perform find operations:

##### FindOne

```code
CollectionBuilder collection = ???;
JsObj query = ???;

Lambda<FindBuilder, JsObj> find = FindOne.of(collection).standalone();

FindBuilder builder = FindBuilder.of(query);

IO<JsObj> io = find.apply(builder);

```

##### FindAll

```code

CollectionBuilder collection = ???;
JsObj query = ???;

Lambda<FindBuilder, FindIterable<JsObj>>find = FindAll.of(collection).standalone();

FindBuilder builder = FindBuilder.of(query);

IO<FindIterable<JsObj>> xs = find.apply(builder);

//map the output with predefined converters
IO<List<JsObj>> ioList = xs.map(Converters::toListOfJsObj);

IO<JsArray> ioArray = xs.map(Converters::toJsArray);
```

#### Insert Operations<a name="insert-operations"></a>

##### InsertOne

```code

JsObj doc = ???;

Lambda<JsObj, InsertOneResult> insert = InsertOne.of(collection).standalone();

IO<String> x = insert.apply(doc).map(Converters::toHexId);

```

##### InsertMany

```code
List<JsObj> docs = ???;

Lambda<List<JsObj>, InsertManyResult> insert = InsertMany.of(collection).standalone();

IO<List<String>> xs = insert.apply(docs).map(Converters::toListOfHexIds);

```

#### Delete Operations<a name="delete-operations"></a>

##### DeleteOne

```code

JsObj query = ???;

Lambda<JsObj, DeleteResult> deleteOne = DeleteOne.of(collection).standalone();

IO<JsObj> x = deleteOne.apply(query).map(Converters::toJsObj);

```

##### DeleteMany

```code

JsObj query = ???;

Lambda<JsObj, DeleteResult> deleteMany = DeleteMany.of(collection).standalone();

IO<JsObj> x = deleteMany.apply(query).map(Converters::toJsObj);

```

#### Update and Replace Operations<a name="update-and-replace-operations"></a>

##### UpdateOne

```code

JsObj query = ???;
JsObj update = ???;

Lambda<QueryUpdate, UpdateResult> updateOne = UpdateOne.of(collection).standalone();

IO<JsObj> x = updateOne.apply(new QueryUpdate(query,update)).map(Converters::toJsObj);

```

##### UpdateMany

```code

JsObj query = ???;
JsObj update = ???;

Lambda<QueryUpdate, UpdateResult> updateOne = UpdateMany.of(collection).standalone();

IO<JsObj> x = updateOne.apply(new QueryUpdate(query,update)).map(Converters::toJsObj);

```

##### ReplaceOne

```code

JsObj query = ???;
JsObj newDoc = ???;

Lambda<QueryReplace, UpdateResult> replaceOne = ReplaceOne.of(collection).standalone();

IO<JsObj> x = replaceOne.apply(new QueryReplace(query,newDoc)).map(Converters::toJsObj);

```

#### Count<a name="count"></a>

```code
JsObj query = ???;

Lambda<JsObj, Long> count = Count.of(collection).standalone();

IO<Long> io = count.apply(query);
```

#### FindOneAndXXX Operations<a name="findoneandxxx-operations"></a>

##### FindOneAndUpdate

```code
JsObj query = ???;

JsObj update = ???;

Lambda<QueryUpdate, JsObj> findOneUpdate = FindOneAndUpdate.of(collection).standalone();

IO<JsObj> x = findOneUpdate.apply(new QueryUpdate(query,update));
```

##### FindOneAndReplace

```code
JsObj query = ???;

JsObj newDoc = ???;

Lambda<QueryReplace, JsObj> findOneReplace = FindOneAndReplace.of(collection).standalone();

IO<JsObj> x = findOneReplace.apply(new QueryReplace(query,newDoc));
```

##### FindOneAndDelete

```code
JsObj query = ???;

Lambda<JsObj, JsObj> findOneDelete = FindOneAndDelete.of(collection).standalone();

IO<JsObj> x = findOneDelete.apply(query);
```

#### Aggregate<a name="aggregate"></a>

```code

List<Bson> stages = ???;

Lambda<List<Bson>, AggregateIterable<JsObj>> aggregate = Aggregate.of(collection).standalone();

IO<List<JsObj>> x = aggregate.apply(stages).map(Converters::toListOfJsObj);

//or using JsObj as an input instead of Bson

Lambda<List<JsObj>, List<JsObj>> y =
    list -> {
                List<Bson> bsons = list.stream().map(Converters::toBson).toList();
                return aggregate.apply(bsons).map(Converters::toListOfJsObj);
    };

```

#### Watcher<a name="watcher"></a>

You can set up a change stream on a MongoDB collection to monitor changes using the `Watcher` class:

```code

CollectionBuilder builder = CollectionBuilder.of(database,collectionName);

Consumer<ChangeStreamIterable<JsObj>> consumer = iter -> { ??? };

Watcher.of(consumer).accept(builder);
```

#### Configuring options <a name="mongo-options"></a>

You can also configure various options for MongoDB operations using the `withOptions` method
available for some operations. Options allow you to specify things like the write concern, bypass
document validation, and more.

For instance, if you want to configure custom `UpdateOptions` for an `UpdateOne` operation:

```code

UpdateOptions customOptions = new UpdateOptions().upsert(true)  // Example option

Lambda<QueryUpdate, UpdateResult> updateOne =
    UpdateOne.of(collection)
             .withOptions(customOptions)
             .standalone();

```

By passing custom options, you can fine-tune the behavior of your MongoDB operations to match your
specific use case.

### Transactions<a name="transactions"></a>

Up to this point, we have been using the standalone method provided by MongoLambdas for operations
that do not require transactions. Now, let's explore how to create and work with transactions in
jio-mongodb.

jio-mongodb provides a convenient way to work with MongoDB transactions. Transactions in MongoDB
allow you to perform multiple operations within a single session, ensuring that either all the
operations are executed or none of them, providing a consistent view of your data.

#### TxBuilder

The `TxBuilder` class is used to create transactions in a MongoDB client session. It offers options
to configure transaction settings and create transaction instances.

`TxBuilder` methods:

- `of(ClientSessionBuilder sessionBuilder)`: Creates a new `TxBuilder` instance with the provided
  session builder.
- `withTxOptions(TransactionOptions transactionOptions)`: Sets the transaction options for the
  transactions created with this builder.
- `build(MongoLambda<I, O> mongoLambda)`: Builds a transaction `Tx` with the specified MongoDB
  Lambda function and transaction options.

The `Tx` class represents a MongoDB transaction that can be applied within a MongoDB client session.
This class ensures that the transaction is executed consistently and provides methods for defining
and applying the transaction.

MongoDB sessions are not multi-threaded. Only one thread should operate within a MongoDB session at
a time to avoid errors like "Only servers in a sharded cluster can start a new transaction at the
active transaction number."

The following example demonstrates how to use jio-mongodb to insert a list of JSON documents within
a MongoDB transaction:

```code


public class TestTx {

    @RegisterExtension
    static Debugger debugger = Debugger.of(Duration.ofSeconds(2));

    String connection = "mongodb://localhost:27017,localhost:27018,localhost:27019/?replicaSet=rs0";

    MongoClient mongoClient =
         MongoClientBuilder.DEFAULT
                           .build(connection);

    ClientSessionBuilder session = ClientSessionBuilder.of(mongoClient);

    DatabaseBuilder testDb = DatabaseBuilder.of(mongoClient, "test");

    CollectionBuilder personCol = CollectionBuilder.of(testDb, "Person");



    @Test
    public void test() {
        MongoLambda<JsObj, String> insertOne = InsertOne.of(personCol)
                                                        .map(Converters::toHexId);

        MongoLambda<List<JsObj>, List<String>> insertAll =
                (session, jsons) ->
                        jsons.stream()
                             .map(json -> insertOne.apply(session, json))
                             .collect(ListExp.seqCollector());

        Tx tx = TxBuilder.of(session).build(insertAll);

        List<String> ids = tx.apply(List.of(JsObj.of("a", JsInt.of(0)),
                                            JsObj.of("b", JsInt.of(1))
                                           )
                                    )
                             .result();

        Assertions.assertEquals(2, ids.size());
    }

}
```

It's worth highlighting the advantages of using the `jio-exp` API to create a `MongoLambda`. In the
previous example, we used a `ListExp.seq` to insert all the JSON documents sequentially. As
mentioned earlier, using the `ListExp.parCollector()` to insert all the JSON documents in parallel
is not feasible due to MongoDB sessions not being multi-threaded.

On the other hand, when using `TxBuilder` and `Tx`, you don't have to worry about the intricacies of
committing or rolling back the transaction in the event of an error, or explicitly closing the
session. All of these essential operations are automatically handled for you, making your code more
robust and convenient.

### Common Exceptions<a name="common-exceptions"></a>

The `MongoExceptions` utility class provides predicates to handle common exceptions:

1. `READ_TIMEOUT`:

   - **Description**: This predicate checks if the given Throwable is an instance of
     `MongoSocketReadTimeoutException`. It returns true if the exception is a read timeout exception
     and false otherwise. Read timeout exceptions typically occur when a read operation (e.g.,
     reading data from the database) takes longer than the specified timeout.

2. `CONNECTION_TIMEOUT`:
   - **Description**: This predicate checks if the given Throwable is an instance of
     `MongoTimeoutException`. It returns true if the exception is a connection timeout exception and
     false otherwise. Connection timeout exceptions usually happen when there's a timeout while
     attempting to establish a connection to the MongoDB server.

Here's an example of how to use these predicates for resilient applications:

```code
JsObj query = ???;

var builder = FindBuilder.of(query);

IO<JsObj> io = FindOne.of(collection)
                      .apply(builder)
                      .retry(MongoExceptions.CONNECTION_TIMEOUT,
                             RetryPolicies.limitRetries(3)
                             );
```

### JFR Integration<a name="mongodb-jfr-integration"></a>

By default, all operations create an event when finished and send it to the Java Flight Recorder
(JFR) system. You can disable this behavior using the `withoutRecordedEvents` method.

Register the Junit Debugger extension from `jio-test` in your tests:

```code

@RegisterExtension
static Debugger debugger = Debugger.of(Duration.ofSeconds(2));

```

This extension enables you to see printed-out events like the following:

```plaintext

event: mongodb, op: INSERT_ONE, duration: 37,800 ms, result: SUCCESS
thread: ForkJoinPool.commonPool-worker-23, event-start-time: 2023-10-17T19:10:30.86140575+02:00

event: mongodb, op: INSERT_ONE, duration: 37,416 ms, result: SUCCESS
thread: ForkJoinPool.commonPool-worker-27, event-start-time: 2023-10-17T19:10:30.861923625+02:00

event: mongodb, op: FIND, duration: 1,362 ms, result: SUCCESS
thread: ForkJoinPool.commonPool-worker-18, event-start-time: 2023-10-17T19:10:30.899902583+02:00

```

## <a name="mongo-installation"><a/> Installation

It requires Java 21 or greater

```code

<dependency>
    <groupId>com.github.imrafaelmerino</groupId>
    <artifactId>jio-mongodb</artifactId>
    <version>4.0.0</version>
</dependency>

```

## <a name="jio-console"><a/> jio-console

[![Maven](https://img.shields.io/maven-central/v/com.github.imrafaelmerino/jio-console/4.0.0)](https://search.maven.org/artifact/com.github.imrafaelmerino/jio-console/4.0.0/jar
"jio-console")

## <a name="jio-jdbc"><a/> jio-jdbc

[![Maven](https://img.shields.io/maven-central/v/com.github.imrafaelmerino/jio-jdbc/1.0.0-RC1)](https://search.maven.org/artifact/com.github.imrafaelmerino/jio-jdbc/1.0.0-RC1/jar
"jio-jdbc")

documentation is on progress
