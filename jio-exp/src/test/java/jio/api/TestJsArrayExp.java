package jio.api;

import jio.IO;
import jio.JsArrayExp;
import jsonvalues.JsArray;
import jsonvalues.JsStr;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TestJsArrayExp {


    @Test
    public void test_parallel_constructors() {
        Assertions.assertEquals(JsArray.of(JsStr.of("a"),
                                           JsStr.of("b")
                                          ),
                                JsArrayExp.par(IO.value("a").map(JsStr::of),
                                               IO.value("b").map(JsStr::of)
                                              )
                                          .debugEach("array")
                                          .join()
                               );
    }


}
