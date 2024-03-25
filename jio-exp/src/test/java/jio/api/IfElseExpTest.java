package jio.api;

import jio.IO;
import jio.IfElseExp;
import jio.Result.Success;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class IfElseExpTest {

  @Test
  @SuppressWarnings({"divzero", "ConstantOverflow"})
  public void test_if_else() {

    // the consequence is never executed!
    IfElseExp<Integer> a = IfElseExp.<Integer>predicate(IO.FALSE)
                                    .consequence(() -> IO.succeed(1 / 0))
                                    .alternative(() -> Constants.ONE);

    Assertions.assertEquals(new Success<>(1),
                            a.debugEach("ifelse")
                             .compute()
                           );

    Assertions.assertEquals(new Success<>(1),
                            a.compute());

  }

}
