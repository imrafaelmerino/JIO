package jio.api;

import fun.tuple.Pair;
import jio.IO;
import jio.PairExp;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TestPairExp {


  @Test
  public void sequential_constructor() throws Exception {

    PairExp<String, String> pair = PairExp.seq(IO.succeed("a"),
                                               IO.succeed("b")
                                              );

    Assertions.assertEquals(Pair.of("a",
                                    "b"
                                   ),
                            pair.get()
                                .call()
                           );
  }


}
