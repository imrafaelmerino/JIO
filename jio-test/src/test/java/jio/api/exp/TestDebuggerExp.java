package jio.api.exp;

import fun.tuple.Pair;
import fun.tuple.Triple;
import jio.*;
import jio.test.junit.JioDebugger;
import jio.test.junit.DebuggerDuration;
import jio.test.stub.value.IOStub;
import jsonvalues.JsArray;
import jsonvalues.JsInt;
import jsonvalues.JsObj;
import jsonvalues.JsStr;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.List;

@ExtendWith(JioDebugger.class)
@DebuggerDuration(millis = 1000)
public class TestDebuggerExp {


    @Test
    public void testAllExp() {

        Assertions.assertTrue(AllExp.seq(IO.TRUE,
                                         IO.TRUE
                                        )
                                    .debugEach("test")
                                    .join()
                             );
        Assertions.assertFalse(AllExp.seq(IO.FALSE,
                                          IO.TRUE
                                         )
                                     .debugEach("test1")
                                     .join()
                              );
        Assertions.assertFalse(AllExp.par(IO.FALSE,
                                          IO.TRUE
                                         )
                                     .debugEach("test2")
                                     .join()
                              );
        Assertions.assertFalse(AllExp.par(IO.FALSE,
                                          IO.TRUE
                                         )
                                     .debugEach("test3")
                                     .join()
                              );


    }

    @Test
    public void testAllExpSeqRetries() {

        IOStub<Boolean> trueAfterFailure =
                IOStub.failThenSucceed(i -> i <= 1 ? new RuntimeException(Integer.toString(i)) : null,
                                       true
                                      );

        Assertions.assertTrue(AllExp.seq(trueAfterFailure.get(),
                                         trueAfterFailure.get()
                                        )
                                    .debugEach("test")
                                    .retryEach(RetryPolicies.limitRetries(1))
                                    .join()
                             );


        IOStub<Boolean> falseAfterFailure =
                IOStub.failThenSucceed(i -> i <= 1 ? new RuntimeException(Integer.toString(i)) : null,
                                       false
                                      );

        // second effect is not evaluated since the first one is false
        Assertions.assertFalse(AllExp.seq(falseAfterFailure.get(),
                                          falseAfterFailure.get()
                                         )
                                     .debugEach("test1")
                                     .retryEach(RetryPolicies.limitRetries(1))
                                     .join()
                              );

    }


    @Test
    public void testAllExpParRetries() {
        IOStub<Boolean> trueAfterFailure =
                IOStub.failThenSucceed(i -> i <= 1 ? new RuntimeException(Integer.toString(i)) : null,
                                       true
                                      );

        Assertions.assertTrue(AllExp.par(trueAfterFailure.get(),
                                         trueAfterFailure.get()
                                        )
                                    .debugEach("test")
                                    .retryEach(RetryPolicies.limitRetries(1))
                                    .join()
                             );


        IOStub<Boolean> falseAfterFailure =
                IOStub.failThenSucceed(i -> i <= 1 ? new RuntimeException(Integer.toString(i)) : null,
                                       false
                                      );

        // all effects are evaluated even the first one is false,not like with the seq constructor
        Assertions.assertFalse(AllExp.par(falseAfterFailure.get(),
                                          falseAfterFailure.get()
                                         )
                                     .debugEach("test1")
                                     .retryEach(RetryPolicies.limitRetries(1))
                                     .join()
                              );
    }

    @Test
    public void testAnyExp() {

        Assertions.assertTrue(AnyExp.seq(IO.TRUE, IO.TRUE).debugEach("test").join());
        Assertions.assertTrue(AnyExp.seq(IO.FALSE, IO.TRUE).debugEach("test1").join());

        Assertions.assertFalse(AnyExp.par(IO.FALSE, IO.FALSE).debugEach("test2").join());
        Assertions.assertFalse(AnyExp.par(IO.FALSE, IO.FALSE).debugEach("test3").join());

    }

    @Test
    public void testCondExp() {


        Assertions.assertEquals("b",
                                CondExp.seq(IO.FALSE, () -> IO.fromValue("a"),
                                            IO.TRUE, () -> IO.fromValue("b"),
                                            () -> IO.fromValue("default")
                                           )
                                       .debugEach("test").join()
                               );

        Assertions.assertEquals("b",
                                CondExp.par(IO.FALSE, () -> IO.fromValue("a"),
                                            IO.TRUE, () -> IO.fromValue("b"),
                                            () -> IO.fromValue("default")
                                           )
                                       .debugEach("test").join()
                               );

        Assertions.assertEquals("a",
                                CondExp.seq(IO.TRUE, () -> IO.fromValue("a"),
                                            IO.TRUE, () -> IO.fromValue("b"),
                                            () -> IO.fromValue("default")
                                           )
                                       .debugEach("test1").join()
                               );

        Assertions.assertEquals("a",
                                CondExp.par(IO.TRUE, () -> IO.fromValue("a"),
                                            IO.TRUE, () -> IO.fromValue("b"),
                                            () -> IO.fromValue("default")
                                           )
                                       .debugEach("test2").join()
                               );

    }

    @Test
    public void testIfElse() {
        Assertions.assertEquals("a",
                                IfElseExp.predicate(IO.FALSE)
                                         .consequence(() -> IO.fromValue("b"))
                                         .alternative(() -> IO.fromValue("a"))
                                         .debugEach("test1")
                                         .join()
                               );
        Assertions.assertEquals("b",
                                IfElseExp.predicate(IO.TRUE)
                                         .consequence(() -> IO.fromValue("b"))
                                         .alternative(() -> IO.fromValue("a"))
                                         .debugEach("test2")
                                         .join()
                               );

    }

    @Test
    public void testJsArrayExp() {

        Assertions.assertEquals(JsArray.of("a", "b"),
                                JsArrayExp.seq(IO.fromValue("a").map(JsStr::of),
                                               IO.fromValue("b").map(JsStr::of)
                                              )
                                          .debugEach("test")
                                          .join()
                               );

        Assertions.assertEquals(JsArray.of("a", "b"),
                                JsArrayExp.par(IO.fromValue("a").map(JsStr::of),
                                               IO.fromValue("b").map(JsStr::of)
                                              )
                                          .debugEach("test")
                                          .join()
                               );
    }

    @Test
    public void testJsObjExp() {

        Assertions.assertEquals(JsObj.of("a", JsObj.of("a", JsInt.of(1),
                                                       "b", JsInt.of(2)
                                                      ),
                                         "b", JsArray.of("a", "b")
                                        ),
                                JsObjExp.seq("a", JsObjExp.seq("a", IO.fromValue(1).map(JsInt::of),
                                                               "b", IO.fromValue(2).map(JsInt::of)
                                                              ),
                                             "b", JsArrayExp.seq(IO.fromValue("a").map(JsStr::of),
                                                                 IO.fromValue("b").map(JsStr::of)
                                                                )

                                            ).debugEach("test")
                                        .join()
                               );

        Assertions.assertEquals(JsObj.of("a", JsObj.of("a", JsInt.of(1),
                                                       "b", JsInt.of(2)
                                                      ),
                                         "b", JsArray.of("a", "b")
                                        ),
                                JsObjExp.par("a", JsObjExp.par("a", IO.fromValue(1).map(JsInt::of),
                                                               "b", IO.fromValue(2).map(JsInt::of)
                                                              ),
                                             "b", JsArrayExp.par(IO.fromValue("a").map(JsStr::of),
                                                                 IO.fromValue("b").map(JsStr::of)
                                                                )

                                            ).debugEach("test")
                                        .join()
                               );

    }

    @Test
    public void testListExp() {

        Assertions.assertEquals(List.of(1, 2, 3),
                                ListExp.seq(IO.fromValue(1),
                                            IO.fromValue(2),
                                            IO.fromValue(3)
                                           )
                                       .debugEach("test")
                                       .join()
                               );

        Assertions.assertEquals(List.of(1, 2, 3),
                                ListExp.par(IO.fromValue(1),
                                            IO.fromValue(2),
                                            IO.fromValue(3)
                                           )
                                       .debugEach("test1")
                                       .join()
                               );

    }

    @Test
    public void testPairExp() {

        Assertions.assertEquals(Pair.of(1, 2),
                                PairExp.seq(IO.fromValue(1),
                                            IO.fromValue(2)
                                           )
                                       .debugEach("test1")
                                       .join()
                               );

        Assertions.assertEquals(Pair.of(1, 2),
                                PairExp.par(IO.fromValue(1),
                                            IO.fromValue(2)
                                           )
                                       .debugEach("test2")
                                       .join()
                               );

    }

    @Test
    public void testTripleExp() {
        Assertions.assertEquals(Triple.of(1, 2, 3),
                                TripleExp.seq(IO.fromValue(1),
                                              IO.fromValue(2),
                                              IO.fromValue(3)
                                             )
                                         .debugEach("context")
                                         .join()
                               );

        Assertions.assertEquals(Triple.of(1, 2, 3),
                                TripleExp.par(IO.fromValue(1),
                                              IO.fromValue(2),
                                              IO.fromValue(3)
                                             )
                                         .debugEach("test2")
                                         .join()
                               );

    }

    @Test
    public void testSwitchExp() {

        Assertions.assertEquals("two",
                                SwitchExp.<Integer, String>eval(IO.fromValue(2))
                                         .match(1, i -> IO.fromValue("one"),
                                                2, i -> IO.fromValue("two"),
                                                i -> IO.fromValue("default")
                                               )
                                         .debugEach("testSwitchExp")
                                         .join()
                               );

    }
}
