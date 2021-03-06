package jio.api;

import fun.tuple.Pair;
import jio.IO;
import jio.PairExp;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TestPairExp {


    @Test
    public void sequential_constructor() {

        PairExp<String, String> pair = PairExp.seq(IO.fromValue("a"),
                                                   IO.fromValue("b")
                                                  );

        Assertions.assertEquals(Pair.of("a",
                                        "b"
                                       ),
                                pair.join()
                               );
    }



}
