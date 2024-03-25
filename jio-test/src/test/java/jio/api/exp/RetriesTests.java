package jio.api.exp;

import static jio.RetryPolicies.incrementalDelay;

import fun.gen.Gen;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import jio.IO;
import jio.Result;
import jio.Result.Failure;
import jio.Result.Success;
import jio.RetryPolicies;
import jio.RetryPolicy;
import jio.test.junit.Debugger;
import jio.test.stub.StubBuilder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

public class RetriesTests {

  @RegisterExtension
  static Debugger debugger = Debugger.of(Duration.ofSeconds(10));

  @Test
  @Disabled
  @SuppressWarnings("CatchAndPrintStackTrace")
  //throws stackoverflowexception
  public void testRetryLimits() {

    Gen<Result<Integer>> gen =
        Gen.seq(n -> n < 3500 ?
                     new Failure<>(new RuntimeException()) :
                     new Success<>(1));

    StubBuilder<Integer> stub = StubBuilder.ofGen(gen);

    Result<Integer> result = stub.get()
                                 .retry(RetryPolicies.limitRetries(3500))
                                 .compute();

    try {
      System.out.println(result);
    } catch (Exception e) {
      e.getCause()
       .printStackTrace();
    }

  }

  @Test
  public void test_retry_success() throws Exception {
    Gen<Result<String>> gen =
        Gen.seq(n -> n <= 3 ?
                     new Result.Failure<>(new RuntimeException()) :
                     new Result.Success<>("a"));

    StubBuilder<String> val = StubBuilder.ofGen(gen);
    Assertions.assertEquals("a",
                            val.get()
                               .retry(RetryPolicies.limitRetries(3))
                               .debug()
                               .compute()
                               .getOutputOrThrow()
                           );

    Assertions.assertEquals("a",
                            val.get()
                               .retry(e -> e instanceof RuntimeException,
                                      RetryPolicies.limitRetries(3)
                                     )
                               .debug()
                               .compute()
                               .getOutputOrThrow()
                           );
  }

  @Test
  public void test_retry_failure() {

    Gen<Result<String>> gen = Gen.seq(n -> n <= 3 ?
                                           new Result.Failure<>(new RuntimeException()) :
                                           new Result.Success<>("a"));

    StubBuilder<String> val = StubBuilder.ofGen(gen);

    Assertions.assertThrows(RuntimeException.class,
                            () -> val.get()
                                     .retry(RetryPolicies.limitRetries(2))
                                     .compute()
                                     .getOutputOrThrow()
                           );
  }

  @Test
  public void test_retry_with_failure_policy_success() throws Exception {
    long start = System.nanoTime();
    Gen<Result<String>> gen = Gen.seq(n ->
                                          n <= 3 ?
                                          new Result.Failure<>(new RuntimeException()) :
                                          new Result.Success<>("b"));

    IO<String> val = StubBuilder.ofGen(gen)
                                .get()
                                .debug();

    RetryPolicy retryPolicy = RetryPolicies.limitRetries(3)
                                           .append(incrementalDelay(Duration.ofSeconds(1)));

    String result = val.retry(retryPolicy)
                       .compute()
                       .getOutputOrThrow();
    long duration = Duration.of(System.nanoTime() - start,
                                ChronoUnit.NANOS
                               )
                            .toSeconds();

    System.out.println(duration);
    Assertions.assertTrue(duration >= 6,
                          STR."duration was \{duration}");

    Assertions.assertEquals("b",
                            result);
  }

  @Test
  public void test_retry_with_failure_policy_failure() {
    long start = System.nanoTime();
    Gen<Result<String>> gen = Gen.seq(n -> n <= 3 ?
                                           new Failure<>(new RuntimeException()) :
                                           new Success<>("b"));

    IO<String> val = StubBuilder.ofGen(gen)
                                .get();

    RetryPolicy retryPolicy = RetryPolicies.limitRetries(2)
                                           .append(incrementalDelay(Duration.ofSeconds(1)));

    Assertions.assertThrows(RuntimeException.class,
                            () -> val.retry(retryPolicy)
                                     .compute()
                                     .getOutputOrThrow()
                           );
    long duration = Duration.of(System.nanoTime() - start,
                                ChronoUnit.NANOS
                               )
                            .toSeconds();
    System.out.println(duration);
    Assertions.assertTrue(duration >= 3,
                          STR."duration was \{duration}");

  }

}
