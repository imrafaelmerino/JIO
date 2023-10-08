package jio.api.exp;

import fun.gen.Gen;
import jio.IO;
import jio.RetryPolicies;
import jio.RetryPolicy;
import jio.test.junit.DebugExp;
import jio.test.junit.Debugger;
import jio.test.stub.Gens;
import jio.test.stub.StubSupplier;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import static jio.RetryPolicies.incrementalDelay;

@ExtendWith(Debugger.class)
@DebugExp(duration = 5000)
public class TestRetries {


    @Test
    @Disabled
    //throws stackoverflowexception
    public void testRetryLimits() {

        Gen<IO<Integer>> gen = Gens.seq(n -> n < 3500 ? IO.fail(new RuntimeException()) : IO.succeed(1));
        StubSupplier<Integer> stub = StubSupplier.ofIOGen(gen);

        CompletableFuture<Integer> future = stub.get()
                                                .retry(RetryPolicies.limitRetries(3500))
                                                .get();

        try {
            Integer integer = future.get();
            System.out.println(integer);
        } catch (Exception e) {
            e.getCause().printStackTrace();
        }


    }


    @Test
    public void test_retry_success() {
        Gen<IO<String>> gen =
                Gens.seq(n -> n <= 3 ? IO.fail(new RuntimeException()) : IO.succeed("a"));

        StubSupplier<String> val = StubSupplier.ofIOGen(gen);
        Assertions.assertEquals("a",
                                val.get()
                                   .retry(RetryPolicies.limitRetries(3))
                                   .result()
                               );

        Assertions.assertEquals("a",
                                val.get()
                                   .retry(e -> e instanceof RuntimeException,
                                          RetryPolicies.limitRetries(3)
                                         )
                                   .result()
                               );
    }

    @Test
    public void test_retry_failure() {

        Gen<IO<String>> gen =
                Gens.seq(n -> n <= 3 ? IO.fail(new RuntimeException()) : IO.succeed("a"));

        StubSupplier<String> val = StubSupplier.ofIOGen(gen);

        Assertions.assertThrows(CompletionException.class,
                                () -> val.get()
                                         .retry(RetryPolicies.limitRetries(2))
                                         .result()
                               );
    }


    @Test
    public void test_retry_with_failurePolicy_success() {
        long start = System.nanoTime();
        Gen<IO<String>> gen =
                Gens.seq(n -> n <= 3 ? IO.fail(new RuntimeException()) : IO.succeed("b"));

        IO<String> val = StubSupplier.ofIOGen(gen).get();

        RetryPolicy retryPolicy = RetryPolicies.limitRetries(3)
                                               .append(incrementalDelay(Duration.ofSeconds(1)));

        String result = val.retry(retryPolicy)
                           .result();
        long duration = Duration.of(System.nanoTime() - start,
                                    ChronoUnit.NANOS
                                   )
                                .toSeconds();

        Assertions.assertTrue(duration >= 6);

        System.out.println(duration);
        Assertions.assertEquals("b", result);
    }

    @Test
    public void test_retry_with_failurePolicy_failure() {
        long start = System.nanoTime();
        Gen<IO<String>> gen =
                Gens.seq(n -> n <= 3 ? IO.fail(new RuntimeException()) : IO.succeed("b"));

        IO<String> val = StubSupplier.ofIOGen(gen).get();

        RetryPolicy retryPolicy = RetryPolicies.limitRetries(2)
                                               .append(incrementalDelay(Duration.ofSeconds(1)));

        Assertions.assertThrows(CompletionException.class,
                                () -> val.retry(retryPolicy)
                                         .result()
                               );
        long duration = Duration.of(System.nanoTime() - start,
                                    ChronoUnit.NANOS
                                   )
                                .toSeconds();

        Assertions.assertTrue(duration >= 3);

        System.out.println(duration);

    }


}
