package jio.api.exp;

import fun.gen.Gen;
import jio.IO;
import jio.RetryPolicies;
import jio.RetryPolicy;
import jio.test.junit.Debugger;
import jio.test.stub.Gens;
import jio.test.stub.StubBuilder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import static jio.RetryPolicies.incrementalDelay;

public class TestRetries {

    @RegisterExtension
    static Debugger debugger = Debugger.of(Duration.ofSeconds(2));

    @Test
    @Disabled
    //throws stackoverflowexception
    public void testRetryLimits() {

        Gen<IO<Integer>> gen = Gens.seq(n -> n < 3500 ? IO.fail(new RuntimeException()) : IO.succeed(1));
        StubBuilder<Integer> stub = StubBuilder.ofIOGen(gen);

        CompletableFuture<Integer> future = stub.build()
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

        StubBuilder<String> val = StubBuilder.ofIOGen(gen);
        Assertions.assertEquals("a",
                                val.build()
                                   .retry(RetryPolicies.limitRetries(3))
                                   .result()
                               );

        Assertions.assertEquals("a",
                                val.build()
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

        StubBuilder<String> val = StubBuilder.ofIOGen(gen);

        Assertions.assertThrows(CompletionException.class,
                                () -> val.build()
                                         .retry(RetryPolicies.limitRetries(2))
                                         .result()
                               );
    }


    @Test
    public void test_retry_with_failurePolicy_success() {
        long start = System.nanoTime();
        Gen<IO<String>> gen =
                Gens.seq(n -> n <= 3 ? IO.fail(new RuntimeException()) : IO.succeed("b"));

        IO<String> val = StubBuilder.ofIOGen(gen).build();

        RetryPolicy retryPolicy = RetryPolicies.limitRetries(3)
                                               .append(incrementalDelay(Duration.ofSeconds(1)));

        String result = val.retry(retryPolicy)
                           .result();
        long duration = Duration.of(System.nanoTime() - start,
                                    ChronoUnit.NANOS
                                   )
                                .toSeconds();

        Assertions.assertTrue(duration >= 6);

        Assertions.assertEquals("b", result);
    }

    @Test
    public void test_retry_with_failurePolicy_failure() {
        long start = System.nanoTime();
        Gen<IO<String>> gen =
                Gens.seq(n -> n <= 3 ? IO.fail(new RuntimeException()) : IO.succeed("b"));

        IO<String> val = StubBuilder.ofIOGen(gen).build();

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


    }


}
