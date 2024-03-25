package jio.api;

import java.util.concurrent.atomic.AtomicLong;
import jio.IO;
import jio.RetryPolicies;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class FunctionsTest {

  @Test
  public void mapped_effect_must_be_repeated_n_times() throws Exception {
    AtomicLong counter = new AtomicLong(0);

    long a = IO.lazy(counter::incrementAndGet)
               .map(x -> x * 10)
               .repeat(_ -> true,
                       RetryPolicies.limitRetries(2)
                      )
               .compute()
               .getOutputOrThrow();

    Assertions.assertEquals(30,
                            a);
  }

  @Test
  public void flat_mapped_effect_must_be_repeated_n_times() throws Exception {
    AtomicLong counter = new AtomicLong(0);

    long a = IO.lazy(
                   counter::incrementAndGet)
               .then(x -> IO.succeed(x * 10))
               .repeat(_ -> true,
                       RetryPolicies.limitRetries(2)
                      )
               .compute()
               .getOutputOrThrow();

    Assertions.assertEquals(30,
                            a);
  }
}
