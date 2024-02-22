package jio.api;

import java.time.Duration;
import java.time.Instant;
import jio.time.Clock;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ClockTest {

  @Test
  public void test_measure_time() throws InterruptedException {

    Clock monotonic = Clock.monotonic;

    long start = monotonic.get();

    Thread.sleep(10);

    long end = monotonic.get();

    long duration = Duration.ofNanos(end - start)
                            .toMillis();
    System.out.println(STR."duration(ms): \{duration}");
    Assertions.assertTrue(duration >= 10);
  }

  @Test
  public void test_real_time() {
    Clock realTime = Clock.realTime;

    Assertions.assertEquals(0,
                            Duration.between(Instant.ofEpochMilli(realTime.get()),
                                             Instant.now()
                                            )
                                    .toDays()
                           );
    Assertions.assertEquals(0,
                            Duration.between(Instant.ofEpochMilli(realTime.get()),
                                             Instant.now()
                                            )
                                    .toHours()
                           );

  }

}
