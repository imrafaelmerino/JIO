package jio.api;

import jio.AllExp;
import jio.IO;
import jsonvalues.JsInt;
import jsonvalues.JsObj;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TestAllExp {

    @Test
    public void sequential_constructor() {

        var a = AllExp.seq(AllExp.seq(IO.TRUE, IO.TRUE), IO.TRUE).debug();

        Assertions.assertTrue(a.result());

        var b = AllExp.seq(AllExp.seq(IO.TRUE, IO.TRUE), IO.FALSE);

        Assertions.assertFalse(b.result());


    }

    @Test
    public void parallel_constructor() {

        var a = AllExp.par(AllExp.par(IO.TRUE, IO.TRUE), IO.TRUE);

        Assertions.assertTrue(a.result());

        var b = AllExp.seq(AllExp.par(IO.TRUE, IO.TRUE), IO.FALSE);

        Assertions.assertFalse(b.result());
    }

    @Test
    public void test_debugeach() {
        var exp = AllExp.par(IO.TRUE,
                             IO.TRUE
                            )
                        .debugEach("context")
                        .result();

        Assertions.assertEquals(true,
                                exp
                               );

    }

}
