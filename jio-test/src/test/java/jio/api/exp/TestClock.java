package jio.api.exp;

import jio.test.stub.ClockStubSupplier;
import jio.time.Clock;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Instant;

public class TestClock {


    @Test
    public void testMock() throws InterruptedException {

        Instant base = Instant.parse("1982-03-13T00:00:00.000000Z");
        Clock clock = ClockStubSupplier.fromReference(base).get();

        Assertions.assertEquals(clock.get(),
                                base.toEpochMilli()
                               );

        System.out.println(base);

        Thread.sleep(1000);

        System.out.println(Instant.ofEpochMilli(clock.get())
                                  .toString());

        Thread.sleep(1000);

        System.out.println(Instant.ofEpochMilli(clock.get())
                                  .toString());


    }

}
