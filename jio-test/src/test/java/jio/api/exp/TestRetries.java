package jio.api.exp;

import jio.IO;
import jio.RetryPolicies;
import jio.RetryPolicy;
import jio.test.junit.JioDebugger;
import jio.test.junit.DebuggerDuration;
import jio.test.stub.value.IOStub;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import static jio.RetryPolicies.incrementalDelay;

@ExtendWith(JioDebugger.class)
@DebuggerDuration(millis = 1000)
public class TestRetries {


    @Test
    @Disabled
    //throws stackoverflowexception
    public void testRetryLimits() {

        IOStub<Integer> stub = IOStub.failThenSucceed(n -> n < 3500 ? new RuntimeException() : null, 1);

        CompletableFuture<Integer> future = stub.get()
                                                .retry(RetryPolicies.limitRetries(3500))
                                                .get();

        try {
            Integer integer = future.get();
            System.out.println(integer);
        } catch (Exception e) {
            System.out.println(e.getCause());
        }


    }


    @Test
    public void test_retry_success() {

        IOStub<String> val = IOStub.failThenSucceed(i -> i <= 3 ?
                                                            new RuntimeException() :
                                                            null,
                                                    "a"
                                                   );

        Assertions.assertEquals("a",
                                val.get()
                                   .retry(RetryPolicies.limitRetries(3))
                                   .join()
                               );

        Assertions.assertEquals("a",
                                val.get()
                                   .retry(e -> e instanceof RuntimeException,
                                          RetryPolicies.limitRetries(3)
                                         )
                                   .join()
                               );
    }

    @Test
    public void test_retry_failure() {

        IOStub<String> val = IOStub.failThenSucceed(
                i -> i <= 3 ? new RuntimeException() : null,
                "a"
                                                   );

        Assertions.assertThrows(CompletionException.class,
                                () -> val.get()
                                         .retry(RetryPolicies.limitRetries(2))
                                         .join()
                               );
    }


    @Test
    public void test_retry_with_failurePolicy_success() {
        long start = System.nanoTime();
        IO<String> val = IOStub.failThenSucceed(i -> i <= 3 ?
                                                        new RuntimeException() :
                                                        null,
                                                "b"
                                               ).get();

        RetryPolicy retryPolicy = RetryPolicies.limitRetries(3)
                                               .append(incrementalDelay(Duration.ofSeconds(1)));

        String result = val.retry(retryPolicy)
                           .join();
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
        var val = IOStub.failThenSucceed(i -> i <= 3 ?
                                                 new RuntimeException() :
                                                 null,
                                         "b"
                                        ).get();

        RetryPolicy retryPolicy = RetryPolicies.limitRetries(2)
                                               .append(incrementalDelay(Duration.ofSeconds(1)));

        Assertions.assertThrows(CompletionException.class,
                                () -> val.retry(retryPolicy)
                                         .join()
                               );
        long duration = Duration.of(System.nanoTime() - start,
                                    ChronoUnit.NANOS
                                   )
                                .toSeconds();

        Assertions.assertTrue(duration >= 3);

        System.out.println(duration);

    }


}
