package jio.api;

import jio.IO;
import jio.JsArrayExp;
import jio.Result.Success;
import jsonvalues.JsArray;
import jsonvalues.JsStr;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class JsArrayExpTest {

  @Test
  public void test_parallel_constructors() {
    Assertions.assertEquals(new Success<>(JsArray.of(JsStr.of("a"),
                                                     JsStr.of("b")
                                                    )),
                            JsArrayExp.par(IO.succeed("a")
                                             .map(JsStr::of),
                                           IO.succeed("b")
                                             .map(JsStr::of)
                                          )
                                      .debugEach("array")
                                      .compute()
                           );
  }

}
