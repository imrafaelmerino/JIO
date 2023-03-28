package jio.api;

import jio.AnyExp;
import jio.IO;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TestAnyExp {

    @Test
    public void sequential_constructor() {

        var a = AnyExp.seq(AnyExp.seq(IO.FALSE, IO.FALSE), IO.TRUE);

        Assertions.assertTrue(a.join());

        var b = AnyExp.seq(AnyExp.seq(IO.FALSE, IO.FALSE), IO.FALSE);

        Assertions.assertFalse(b.join());

    }

    @Test
    public void parallel_constructor() {

        var a = AnyExp.par(AnyExp.par(IO.TRUE, IO.FALSE), IO.FALSE);

        Assertions.assertTrue(a.join());

        var b = AnyExp.seq(AnyExp.par(IO.FALSE, IO.FALSE), IO.FALSE);

        Assertions.assertFalse(b.join());
    }

    @Test
    public void test_debugeach() {
        var exp = AnyExp.par(IO.FALSE,
                             IO.TRUE
                            )
                        .debugEach("context")
                        .join();

        Assertions.assertEquals(true,
                                exp
                               );

    }
}
