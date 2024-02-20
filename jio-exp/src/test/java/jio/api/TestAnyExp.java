package jio.api;

import jio.AnyExp;
import jio.IO;
import jio.Result;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TestAnyExp {

  @Test
  public void sequential_constructor() {

    var anyIsTrue = AnyExp.seq(AnyExp.seq(IO.FALSE,
                                          IO.FALSE),
                               IO.TRUE);

    Assertions.assertEquals(Result.TRUE,
                            anyIsTrue.get());

    var anyIsFalse = AnyExp.seq(AnyExp.seq(IO.FALSE,
                                           IO.FALSE),
                                IO.FALSE);

    Assertions.assertEquals(Result.FALSE,
                            anyIsFalse.get());

  }

  @Test
  public void parallel_constructor() {

    var anyIsTrue = AnyExp.par(AnyExp.par(IO.TRUE,
                                          IO.FALSE),
                               IO.FALSE);

    Assertions.assertEquals(Result.TRUE,
                            anyIsTrue.get());

    var anyIsFalse = AnyExp.par(AnyExp.par(IO.FALSE,
                                           IO.FALSE),
                                IO.FALSE);

    Assertions.assertEquals(Result.FALSE,
                            anyIsFalse.get());
  }

  @Test
  public void test_debug_each() {
    var exp = AnyExp.par(IO.FALSE,
                         IO.TRUE
    )
                    .debugEach("context")
                    .get();

    Assertions.assertEquals(Result.TRUE,
                            exp
    );

  }
}
