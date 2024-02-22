package jio.api;

import jio.IO;
import jio.IfElseExp;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TestIfElseExp {

  @Test
  @SuppressWarnings({"divzero", "ConstantOverflow"})
  public void test_if_else() {

    // the consequence is never executed!
    IfElseExp<Integer> a = IfElseExp.<Integer>predicate(IO.FALSE)
                                    .consequence(() -> IO.succeed(1 / 0))
                                    .alternative(() -> Constants.ONE);

    int r = a.debugEach("ifelse")
             .join();
    Assertions.assertEquals(1,
                            r
                           );

    r = a.join();
    Assertions.assertEquals(1,
                            r);

  }

}
