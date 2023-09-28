package jio.api;

import fun.tuple.Triple;
import jio.IO;
import jio.TripleExp;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;



public class TestTripleExp {


    @Test
    public void sequential_constructor() {

        TripleExp<String, String, String> triple =
                TripleExp.seq(IO.succeed("a"),
                              IO.succeed("b"),
                              IO.succeed("c")
                             );

        Assertions.assertEquals(
                Triple.of("a",
                          "b",
                          "c"
                         ),
                triple.result());
    }



    @Test
    public void parallel_constructor() {

        TripleExp<String, String, String> triple =
                TripleExp.par(IO.succeed("a"),
                              IO.succeed("b"),
                              IO.succeed("c")
                             );

        Assertions.assertEquals(
                Triple.of("a",
                          "b",
                          "c"
                         ),
                triple.result()
                               );
    }



}
