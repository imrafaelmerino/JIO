package jio.api;

import jio.IO;
import jio.JsArrayExp;
import jsonvalues.JsArray;
import jsonvalues.JsStr;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

public class TestJsArrayExp {


    @Test
    public void test_parallel_constructors() {
        Assertions.assertEquals(JsArray.of(JsStr.of("a"),
                                           JsStr.of("b")
                                          ),
                                JsArrayExp.par(IO.succeed("a").map(JsStr::of),
                                               IO.succeed("b").map(JsStr::of)
                                              )
                                          .debugEach("array")
                                          .join()
                               );
    }


}
