package jio.api;

import fun.tuple.Triple;
import jio.IO;
import jio.Result.Success;
import jio.TripleExp;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TripleExpTest {

  @Test
  public void sequential_constructor() {

    TripleExp<String, String, String> triple = TripleExp.seq(IO.succeed("a"),
                                                             IO.succeed("b"),
                                                             IO.succeed("c")
                                                            );

    Assertions.assertEquals(new Success<>(Triple.of("a",
                                                    "b",
                                                    "c"
                                                   )),
                            triple.compute());
  }

  @Test
  public void parallel_constructor() {

    TripleExp<String, String, String> triple = TripleExp.par(IO.succeed("a"),
                                                             IO.succeed("b"),
                                                             IO.succeed("c")
                                                            );

    Assertions.assertEquals(new Success<>(Triple.of("a",
                                                    "b",
                                                    "c"
                                                   )),
                            triple.compute()
                           );
  }

}
