package jio.api.exp;

import fun.tuple.Pair;
import fun.tuple.Triple;
import jio.*;
import jio.test.junit.JioDebugger;
import jio.test.junit.DebuggerDuration;
import jsonvalues.JsArray;
import jsonvalues.JsObj;
import jsonvalues.JsStr;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static jio.api.exp.Stubs.*;

@ExtendWith(JioDebugger.class)
@DebuggerDuration(millis = 1000)
public class StubTests {

    @Test
    public void ifelse_exp_measuring_time() {
        long start = System.nanoTime();
        var x = IfElseExp.<String>predicate(IO.FALSE)
                         .consequence(A_AFTER_1_SEC::get)
                         .alternative(B_AFTER_1_SEC::get)
                         .debugEach("context")
                         .join();


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
    public void triple_exp_sequential_measuring_time() {
        long start = System.nanoTime();

        Triple<String, String, String> triple =
                TripleExp.seq(A_AFTER_1_SEC.get(),
                              B_AFTER_1_SEC.get(),
                              C_AFTER_1_SEC.get()
                             )
                         .join();


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
    public void triple_exp_parallel_measuring_time() {
        long start = System.nanoTime();
        Triple<String, String, String> triple =
                TripleExp.par(A_AFTER_1_SEC.get(),
                              B_AFTER_1_SEC.get(),
                              C_AFTER_1_SEC.get()
                             )
                         .debugEach("context")
                         .join();


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
        Assertions.assertTrue(duration < 3);


    }

    @Test
    public void jobj_exp_parallel_measuring_time() {
        long start = System.nanoTime();
        var obj = JsObjExp.par("a", JsObjExp.par("a", A_AFTER_1_SEC.get().map(JsStr::of),
                                                 "b", B_AFTER_1_SEC.get().map(JsStr::of)
                                                ),
                               "b", JsArrayExp.par(A_AFTER_1_SEC.get().map(JsStr::of),
                                                   B_AFTER_1_SEC.get().map(JsStr::of)
                                                  )
                              )
                          .debugEach("context")
                          .join();


        long duration = Duration.of(System.nanoTime() - start,
                                    ChronoUnit.NANOS
                                   )
                                .toSeconds();

        Assertions.assertEquals(JsObj.of("a", JsObj.of("a", JsStr.of("a"),
                                                       "b", JsStr.of("b")
                                                      ),
                                         "b", JsArray.of(JsStr.of("a"),
                                                         JsStr.of("b")
                                                        )
                                        ),
                                obj
                               );
        Assertions.assertTrue(duration < 2);


    }
    @Test
    public void pair_exp_sequential_measuring_time() {
        long start = System.nanoTime();

        Pair<String, String> pair =
                PairExp.seq(A_AFTER_1_SEC.get(),
                            B_AFTER_1_SEC.get())
                       .join();


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
    public void pair_exp_parallel_measuring_time() {
        long start = System.nanoTime();
        Pair<String, String> pair =
                PairExp.par(A_AFTER_1_SEC.get(),
                            B_AFTER_1_SEC.get())
                       .debugEach("context")
                       .join();


        long duration = Duration.of(System.nanoTime() - start,
                                    ChronoUnit.NANOS
                                   )
                                .toSeconds();

        Assertions.assertEquals(Pair.of("a",
                                        "b"
                                       ),
                                pair
                               );
        System.out.println(duration);
        Assertions.assertTrue(duration < 2);


    }

    @Test
    public void array_exp_seq_time() {
        long start = System.nanoTime();
        var arr = JsArrayExp.seq(A_AFTER_1_SEC.get().map(JsStr::of),
                                 B_AFTER_1_SEC.get().map(JsStr::of)
                                )
                            .debugEach("context")
                            .join();


        long duration = Duration.of(System.nanoTime() - start,
                                    ChronoUnit.NANOS
                                   )
                                .toSeconds();

        Assertions.assertEquals(JsArray.of("a",
                                           "b"
                                          ),
                                arr
                               );
        System.out.println(duration);
        Assertions.assertTrue(duration >= 2);


    }

    @Test
    public void list_exp_parallel_measuring_time() {
        long start = System.nanoTime();
        List<String> list =
                ListExp.par(A_AFTER_1_SEC.get(),
                            B_AFTER_1_SEC.get(),
                            C_AFTER_1_SEC.get()
                           )
                       .debugEach("context")
                       .join();


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
