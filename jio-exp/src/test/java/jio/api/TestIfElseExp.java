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
                                        .consequence(() -> IO.value(1 / 0))
                                        .alternative(() -> Constants.ONE);

        Assertions.assertEquals(1,
                                a.debugEach("ifelse").join()
                               );

        // the alternative is never executed!
        IfElseExp<Integer> unused =
                IfElseExp.<Integer>predicate(IO.TRUE)
                         .alternative(() -> IO.value(1 / 0))
                         .consequence(() -> Constants.ONE);

        Assertions.assertEquals(1, a.join());
    }


}
