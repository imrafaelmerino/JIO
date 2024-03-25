package jio.api.exp;

import static jio.api.exp.Stubs.A_AFTER_1_SEC;
import static jio.api.exp.Stubs.B_AFTER_1_SEC;
import static jio.api.exp.Stubs.C_AFTER_1_SEC;

import fun.tuple.Pair;
import fun.tuple.Triple;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.List;
import jio.IO;
import jio.IfElseExp;
import jio.JsArrayExp;
import jio.JsObjExp;
import jio.ListExp;
import jio.PairExp;
import jio.TripleExp;
import jio.test.junit.Debugger;
import jsonvalues.JsArray;
import jsonvalues.JsObj;
import jsonvalues.JsStr;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

public class StubSupplierTests {

  @RegisterExtension
  static Debugger debugger = Debugger.of(Duration.ofSeconds(5));

  @Test
  public void if_else_exp_measuring_time() throws Exception {
    long start = System.nanoTime();
    var x = IfElseExp.<String>predicate(IO.FALSE)
                     .consequence(A_AFTER_1_SEC)
                     .alternative(B_AFTER_1_SEC)
                     .debugEach("context")
                     .call()
                     .getOutputOrThrow();

    long duration = Duration.of(System.nanoTime() - start,
                                ChronoUnit.NANOS
    )
                            .toSeconds();

    Assertions.assertEquals("b",
                            x
    );
    Assertions.assertTrue(duration < 3);

  }

  @Test
  public void triple_exp_sequential_measuring_time() throws Exception {
    long start = System.nanoTime();

    Triple<String, String, String> triple = TripleExp.seq(A_AFTER_1_SEC.get(),
                                                          B_AFTER_1_SEC.get(),
                                                          C_AFTER_1_SEC.get()
    )
                                                     .call()
                                                     .getOutputOrThrow();

    long duration = Duration.of(System.nanoTime() - start,
                                ChronoUnit.NANOS
    )
                            .toSeconds();

    Assertions.assertEquals(Triple.of("a",
                                      "b",
                                      "c"
    ),
                            triple
    );
    Assertions.assertTrue(duration >= 3);

  }

  @Test
  public void triple_exp_parallel_measuring_time() throws Exception {
    long start = System.nanoTime();
    Triple<String, String, String> triple = TripleExp.par(A_AFTER_1_SEC.get(),
                                                          B_AFTER_1_SEC.get(),
                                                          C_AFTER_1_SEC.get()
    )
                                                     .debugEach("context")
                                                     .call()
                                                     .getOutputOrThrow();

    long duration = Duration.of(System.nanoTime() - start,
                                ChronoUnit.NANOS
    )
                            .toSeconds();

    Assertions.assertEquals(Triple.of("a",
                                      "b",
                                      "c"
    ),
                            triple
    );
    System.out.println(duration);
    Assertions.assertTrue(duration < 3);

  }

  @Test
  public void jsobj_exp_parallel_measuring_time() throws Exception {
    long start = System.nanoTime();
    var obj = JsObjExp.par("a",
                           JsObjExp.par("a",
                                        A_AFTER_1_SEC.get()
                                                     .map(JsStr::of),
                                        "b",
                                        B_AFTER_1_SEC.get()
                                                     .map(JsStr::of)
                           ),
                           "b",
                           JsArrayExp.par(A_AFTER_1_SEC.get()
                                                       .map(JsStr::of),
                                          B_AFTER_1_SEC.get()
                                                       .map(JsStr::of)
                           )
    )
                      .debugEach("context")
                      .call()
                      .getOutputOrThrow();

    long duration = Duration.of(System.nanoTime() - start,
                                ChronoUnit.NANOS
    )
                            .toSeconds();

    Assertions.assertEquals(JsObj.of("a",
                                     JsObj.of("a",
                                              JsStr.of("a"),
                                              "b",
                                              JsStr.of("b")
                                     ),
                                     "b",
                                     JsArray.of(JsStr.of("a"),
                                                JsStr.of("b")
                                     )
    ),
                            obj
    );
    Assertions.assertTrue(duration < 2);

  }

  @Test
  public void pair_exp_sequential_measuring_time() throws Exception {
    long start = System.nanoTime();

    Pair<String, String> pair = PairExp.seq(A_AFTER_1_SEC.get(),
                                            B_AFTER_1_SEC.get())
                                       .call()
                                       .getOutputOrThrow();

    long duration = Duration.of(System.nanoTime() - start,
                                ChronoUnit.NANOS
    )
                            .toSeconds();

    Assertions.assertEquals(Pair.of("a",
                                    "b"
    ),
                            pair
    );
    Assertions.assertTrue(duration >= 2);

  }

  @Test
  public void pair_exp_parallel_measuring_time() throws Exception {
    long start = System.nanoTime();
    Pair<String, String> pair = PairExp.par(A_AFTER_1_SEC.get(),
                                            B_AFTER_1_SEC.get())
                                       .debugEach("context")
                                       .call()
                                       .getOutputOrThrow();

    long duration = Duration.of(System.nanoTime() - start,
                                ChronoUnit.NANOS
    )
                            .toSeconds();

    Assertions.assertEquals(Pair.of("a",
                                    "b"
    ),
                            pair
    );
    Assertions.assertTrue(duration < 2,
                          "%s is not lower than two sg".formatted(duration));

  }

  @Test
  public void array_exp_seq_time() throws Exception {
    long start = System.nanoTime();
    var arr = JsArrayExp.seq(A_AFTER_1_SEC.get()
                                          .map(JsStr::of),
                             B_AFTER_1_SEC.get()
                                          .map(JsStr::of)
    )
                        .debugEach("context")
                        .call()
                        .getOutputOrThrow();

    long duration = Duration.of(System.nanoTime() - start,
                                ChronoUnit.NANOS
    )
                            .toSeconds();

    Assertions.assertEquals(JsArray.of("a",
                                       "b"
    ),
                            arr
    );
    Assertions.assertTrue(duration >= 2);

  }

  @Test
  public void list_exp_parallel_measuring_time() throws Exception {
    long start = System.nanoTime();
    List<String> list = ListExp.par(A_AFTER_1_SEC.get(),
                                    B_AFTER_1_SEC.get(),
                                    C_AFTER_1_SEC.get()
    )
                               .debugEach("context")
                               .call()
                               .getOutputOrThrow();

    long duration = Duration.of(System.nanoTime() - start,
                                ChronoUnit.NANOS
    )
                            .toSeconds();

    Assertions.assertEquals(List.of("a",
                                    "b",
                                    "c"
    ),
                            list
    );
    Assertions.assertTrue(duration < 3);
  }

}
