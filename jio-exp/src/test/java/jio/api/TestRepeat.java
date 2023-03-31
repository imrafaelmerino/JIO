package jio.api;

import fun.tuple.Pair;
import jio.IO;
import jio.ListExp;
import jio.PairExp;
import jio.RetryPolicies;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.ForkJoinPool;

import static jio.api.TestManagedBlocker.random;

public class TestRepeat {
    static int maxActiveThreadCount = -1;

    public static int sleepRandom(int max) {
        int n = ForkJoinPool.commonPool().getActiveThreadCount();
        if (n > maxActiveThreadCount) {
            maxActiveThreadCount = n;
        }
        int r = random.nextInt(max);
        try {
            Thread.sleep(r);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return r;
    }

    @Test
    @Disabled
    public void testRepeatLimits() {

        int max = 10;
        IO<Pair<Integer, Integer>> pair = PairExp.par(IO.fromManagedSupplier(() -> sleepRandom(max)),
                                                      IO.fromManagedSupplier(() -> sleepRandom(max))
                                                     ).debugEach("pair").repeat(e -> true,
                                                                                RetryPolicies.constantDelay(Duration.ofMillis(100)).limitRetriesByCumulativeDelay(Duration.ofSeconds(1))
                                                                               );
        try {
            ListExp<Pair<Integer, Integer>> exp = ListExp.par();
            for (int i = 0; i < 500; i++) {
                exp = exp.append(pair);
            }

            System.out.println(exp.join());
        } finally {
            System.out.println(maxActiveThreadCount);
        }

    }
}
