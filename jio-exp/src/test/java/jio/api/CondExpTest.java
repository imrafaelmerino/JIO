package jio.api;

import jio.CondExp;
import jio.IO;
import jio.Result;
import jio.Result.Success;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class CondExpTest {

  @Test
  public void sequential_constructors() {

    Assertions.assertEquals(new Success<>("B"),
                            CondExp.seq(IO.FALSE,
                                        () -> Constants.A,
                                        IO.TRUE,
                                        () -> Constants.B,
                                        () -> Constants.C
                                       )
                                   .map(String::toUpperCase)
                                   .compute());

    Assertions.assertEquals(Result.NULL,
                            CondExp.seq(IO.FALSE,
                                        () -> Constants.A,
                                        IO.FALSE,
                                        () -> Constants.B
                                       )
                                   .call());

    Assertions.assertEquals(new Success<>("C"),
                            CondExp.seq(IO.FALSE,
                                        () -> Constants.A,
                                        IO.FALSE,
                                        () -> Constants.B,
                                        IO.TRUE,
                                        () -> Constants.C,
                                        () -> Constants.C
                                       )
                                   .map(String::toUpperCase)
                                   .compute());

    Assertions.assertEquals(new Success<>("D"),
                            CondExp.seq(IO.FALSE,
                                        () -> Constants.A,
                                        IO.FALSE,
                                        () -> Constants.B,
                                        IO.FALSE,
                                        () -> Constants.C,
                                        IO.TRUE,
                                        () -> Constants.D,
                                        () -> Constants.C
                                       )
                                   .map(String::toUpperCase)
                                   .compute());

    Assertions.assertEquals(new Success<>("A"),
                            CondExp.seq(IO.FALSE,
                                        () -> Constants.A,
                                        IO.FALSE,
                                        () -> Constants.B,
                                        IO.FALSE,
                                        () -> Constants.C,
                                        IO.FALSE,
                                        () -> Constants.D,
                                        IO.TRUE,
                                        () -> Constants.A,
                                        () -> Constants.C
                                       )
                                   .map(String::toUpperCase)
                                   .compute());

    Assertions.assertEquals(new Success<>("B"),
                            CondExp.seq(IO.FALSE,
                                        () -> Constants.A,
                                        IO.FALSE,
                                        () -> Constants.B,
                                        IO.FALSE,
                                        () -> Constants.C,
                                        IO.FALSE,
                                        () -> Constants.D,
                                        IO.FALSE,
                                        () -> Constants.A,
                                        IO.TRUE,
                                        () -> Constants.B,
                                        () -> Constants.C
                                       )
                                   .map(String::toUpperCase)
                                   .compute());

    Assertions.assertEquals(new Success<>("C"),
                            CondExp.seq(IO.FALSE,
                                        () -> Constants.A,
                                        IO.FALSE,
                                        () -> Constants.B,
                                        IO.FALSE,
                                        () -> Constants.C,
                                        IO.FALSE,
                                        () -> Constants.D,
                                        IO.FALSE,
                                        () -> Constants.A,
                                        IO.FALSE,
                                        () -> Constants.B,
                                        () -> Constants.C
                                       )
                                   .map(String::toUpperCase)
                                   .compute());
  }

  @Test
  public void parallel_constructors() {

    Assertions.assertEquals(
        new Success<>("B"),
        CondExp.par(IO.FALSE,
                    () -> Constants.A,
                    IO.TRUE,
                    () -> Constants.B,
                    () -> Constants.C
                   )
               .map(String::toUpperCase)
               .compute());

    Assertions.assertEquals(new Success<>("C"),
                            CondExp.par(IO.FALSE,
                                        () -> Constants.A,
                                        IO.FALSE,
                                        () -> Constants.B,
                                        IO.TRUE,
                                        () -> Constants.C,
                                        () -> Constants.C
                                       )
                                   .map(String::toUpperCase)
                                   .compute());

    Assertions.assertEquals(new Success<>("D"),
                            CondExp.par(IO.FALSE,
                                        () -> Constants.A,
                                        IO.FALSE,
                                        () -> Constants.B,
                                        IO.FALSE,
                                        () -> Constants.C,
                                        IO.TRUE,
                                        () -> Constants.D,
                                        () -> Constants.C
                                       )
                                   .map(String::toUpperCase)
                                   .compute());

    Assertions.assertEquals(new Success<>("A"),
                            CondExp.par(IO.FALSE,
                                        () -> Constants.A,
                                        IO.FALSE,
                                        () -> Constants.B,
                                        IO.FALSE,
                                        () -> Constants.C,
                                        IO.FALSE,
                                        () -> Constants.D,
                                        IO.TRUE,
                                        () -> Constants.A,
                                        () -> Constants.C
                                       )
                                   .map(String::toUpperCase)
                                   .compute());

    Assertions.assertEquals(new Success<>("B"),
                            CondExp.par(IO.FALSE,
                                        () -> Constants.A,
                                        IO.FALSE,
                                        () -> Constants.B,
                                        IO.FALSE,
                                        () -> Constants.C,
                                        IO.FALSE,
                                        () -> Constants.D,
                                        IO.FALSE,
                                        () -> Constants.A,
                                        IO.TRUE,
                                        () -> Constants.B,
                                        () -> Constants.C
                                       )
                                   .map(String::toUpperCase)
                                   .compute());

    Assertions.assertEquals(new Success<>("C"),
                            CondExp.par(IO.FALSE,
                                        () -> Constants.A,
                                        IO.FALSE,
                                        () -> Constants.B,
                                        IO.FALSE,
                                        () -> Constants.C,
                                        IO.FALSE,
                                        () -> Constants.D,
                                        IO.FALSE,
                                        () -> Constants.A,
                                        IO.FALSE,
                                        () -> Constants.B,
                                        () -> Constants.C
                                       )
                                   .map(String::toUpperCase)
                                   .compute());
  }

  @Test
  public void test_debug_each() {
    var exp = CondExp.par(IO.FALSE,
                          () -> IO.succeed("a"),
                          IO.FALSE,
                          () -> IO.succeed("b"),
                          () -> IO.succeed("default")
                         )
                     .debugEach("context")
                     .compute();

    Assertions.assertEquals(new Success<>("default"),
                            exp
                           );

  }

}
