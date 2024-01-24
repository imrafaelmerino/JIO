package jio.api.exp;

import jio.test.stub.ClockStub;
import jio.time.Clock;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Instant;

public class TestClock {


  @Test
  public void testMock() throws InterruptedException {

    Instant base = Instant.parse("1982-03-13T00:00:00.000000Z");
    Clock clock = ClockStub.fromReference(base);

    Assertions.assertEquals(clock.get(),
                            base.toEpochMilli()
                           );

    System.out.println(base);

    Thread.sleep(1000);

    Thread.sleep(1000);

    System.out.println(Instant.ofEpochMilli(clock.get())
                              .toString());


  }

}
