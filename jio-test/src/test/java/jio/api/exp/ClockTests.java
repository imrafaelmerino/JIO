package jio.api.exp;

import java.time.Instant;
import jio.test.stub.ClockStub;
import jio.time.Clock;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ClockTests {

  @Test
  public void testMock() throws InterruptedException {

    Instant base = Instant.parse("1982-03-13T00:00:00.000000Z");
    Clock clock = ClockStub.fromReference(base);

    long tick = clock.get();
    long epochMilli = base.toEpochMilli();
    Assertions.assertEquals(tick,
                            epochMilli
                           );

    System.out.println(base);

    Thread.sleep(1000);

    Thread.sleep(1000);

    tick = clock.get();
    System.out.println(Instant.ofEpochMilli(tick)
                              .toString());

  }

}
