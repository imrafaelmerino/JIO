package jio.api;

import jio.AllExp;
import jio.IO;
import jio.IfElseExp;
import jio.Lambda;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

public class TestIfElseExp {

    @Test
    @SuppressWarnings({"divzero", "ConstantOverflow"})
    public void test_if_else() {

        // the consequence is never executed!
        IfElseExp<Integer> a = IfElseExp.<Integer>predicate(IO.FALSE)
                                        .consequence(() -> IO.succeed(1 / 0))
                                        .alternative(() -> Constants.ONE);

        Assertions.assertEquals(1,
                                a.debugEach("ifelse").result()
                               );


        Assertions.assertEquals(1, a.result());


    }


}
