package jio.api;

import jio.AllExp;
import jio.IO;
import jio.Result;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class AllExpTest {

  @Test
  public void sequential_constructor() {

    var allIsTrue = AllExp.seq(AllExp.seq(IO.TRUE,
                                          IO.TRUE),
                               IO.TRUE)
                          .debug();

    Assertions.assertEquals(Result.TRUE,
                            allIsTrue.compute());

    var allIsFalse = AllExp.seq(AllExp.seq(IO.TRUE,
                                           IO.TRUE),
                                IO.FALSE);

    Assertions.assertEquals(Result.FALSE,
                            allIsFalse.call());

  }

  @Test
  public void parallel_constructor() {

    var allIsTrue = AllExp.par(AllExp.par(IO.TRUE,
                                          IO.TRUE),
                               IO.TRUE);

    Assertions.assertEquals(Result.TRUE,
                            allIsTrue.call());

    var allIsFalse = AllExp.par(AllExp.par(IO.TRUE,
                                           IO.TRUE),
                                IO.FALSE);

    Assertions.assertEquals(Result.FALSE,
                            allIsFalse.call());
  }

  @Test
  public void test_debug_each() {
    var exp = AllExp.par(IO.TRUE,
                         IO.TRUE
                        )
                    .debugEach("context")
                    .compute();

    Assertions.assertEquals(Result.TRUE,
                            exp
                           );

  }

}
