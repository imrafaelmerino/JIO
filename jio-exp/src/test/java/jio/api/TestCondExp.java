package jio.api;

import jio.CondExp;
import jio.IO;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TestCondExp {

    @Test
    public void sequential_constructors() {

        Assertions.assertEquals(
                "B",
                CondExp.seq(IO.FALSE, () -> Constants.A,
                            IO.TRUE, () -> Constants.B,
                            () -> Constants.C
                           )
                       .map(String::toUpperCase)
                       .join());


        Assertions.assertEquals(
                "C",
                CondExp.seq(IO.FALSE, () -> Constants.A,
                            IO.FALSE, () -> Constants.B,
                            IO.TRUE, () -> Constants.C,
                            () -> Constants.C
                           )
                       .map(String::toUpperCase)
                       .join());

        Assertions.assertEquals(
                "D",
                CondExp.seq(IO.FALSE, () -> Constants.A,
                            IO.FALSE, () -> Constants.B,
                            IO.FALSE, () -> Constants.C,
                            IO.TRUE, () -> Constants.D,
                            () -> Constants.C
                           )
                       .map(String::toUpperCase)
                       .join());

        Assertions.assertEquals(
                "A",
                CondExp.seq(IO.FALSE, () -> Constants.A,
                            IO.FALSE, () -> Constants.B,
                            IO.FALSE, () -> Constants.C,
                            IO.FALSE, () -> Constants.D,
                            IO.TRUE, () -> Constants.A,
                            () -> Constants.C
                           )
                       .map(String::toUpperCase)
                       .join());

        Assertions.assertEquals(
                "B",
                CondExp.seq(IO.FALSE, () -> Constants.A,
                            IO.FALSE, () -> Constants.B,
                            IO.FALSE, () -> Constants.C,
                            IO.FALSE, () -> Constants.D,
                            IO.FALSE, () -> Constants.A,
                            IO.TRUE, () -> Constants.B,
                            () -> Constants.C
                           )
                       .map(String::toUpperCase)
                       .join());

        Assertions.assertEquals(
                "C",
                CondExp.seq(IO.FALSE, () -> Constants.A,
                            IO.FALSE, () -> Constants.B,
                            IO.FALSE, () -> Constants.C,
                            IO.FALSE, () -> Constants.D,
                            IO.FALSE, () -> Constants.A,
                            IO.FALSE, () -> Constants.B,
                            () -> Constants.C
                           )
                       .map(String::toUpperCase)
                       .join());
    }

    @Test
    public void parallel_constructors() {

        Assertions.assertEquals(
                "B",
                CondExp.par(IO.FALSE, () -> Constants.A,
                            IO.TRUE, () -> Constants.B,
                            () -> Constants.C
                           )
                       .map(String::toUpperCase)
                       .join());


        Assertions.assertEquals(
                "C",
                CondExp.par(IO.FALSE, () -> Constants.A,
                            IO.FALSE, () -> Constants.B,
                            IO.TRUE, () -> Constants.C,
                            () -> Constants.C
                           )
                       .map(String::toUpperCase)
                       .join());

        Assertions.assertEquals(
                "D",
                CondExp.par(IO.FALSE, () -> Constants.A,
                            IO.FALSE, () -> Constants.B,
                            IO.FALSE, () -> Constants.C,
                            IO.TRUE, () -> Constants.D,
                            () -> Constants.C
                           )
                       .map(String::toUpperCase)
                       .join());

        Assertions.assertEquals(
                "A",
                CondExp.par(IO.FALSE, () -> Constants.A,
                            IO.FALSE, () -> Constants.B,
                            IO.FALSE, () -> Constants.C,
                            IO.FALSE, () -> Constants.D,
                            IO.TRUE, () -> Constants.A,
                            () -> Constants.C
                           )
                       .map(String::toUpperCase)
                       .join());

        Assertions.assertEquals(
                "B",
                CondExp.par(IO.FALSE, () -> Constants.A,
                            IO.FALSE, () -> Constants.B,
                            IO.FALSE, () -> Constants.C,
                            IO.FALSE, () -> Constants.D,
                            IO.FALSE, () -> Constants.A,
                            IO.TRUE, () -> Constants.B,
                            () -> Constants.C
                           )
                       .map(String::toUpperCase)
                       .join());

        Assertions.assertEquals(
                "C",
                CondExp.par(IO.FALSE, () -> Constants.A,
                            IO.FALSE, () -> Constants.B,
                            IO.FALSE, () -> Constants.C,
                            IO.FALSE, () -> Constants.D,
                            IO.FALSE, () -> Constants.A,
                            IO.FALSE, () -> Constants.B,
                            () -> Constants.C
                           )
                       .map(String::toUpperCase)
                       .join());
    }

    @Test
    public void test_debugeach() {
        var exp = CondExp.par(IO.FALSE, () -> IO.succeed("a"),
                              IO.FALSE, () -> IO.succeed("b"),
                              () -> IO.succeed("default")
                             )
                         .debugEach("context")
                         .join();

        Assertions.assertEquals("default",
                                exp
                               );

    }


}
