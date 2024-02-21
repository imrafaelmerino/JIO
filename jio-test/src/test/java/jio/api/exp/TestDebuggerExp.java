package jio.api.exp;

import fun.gen.Gen;
import fun.tuple.Pair;
import fun.tuple.Triple;
import java.time.Duration;
import java.util.List;
import jio.AllExp;
import jio.AnyExp;
import jio.CondExp;
import jio.IO;
import jio.IfElseExp;
import jio.JsArrayExp;
import jio.JsObjExp;
import jio.ListExp;
import jio.PairExp;
import jio.Result.Success;
import jio.RetryPolicies;
import jio.SwitchExp;
import jio.TripleExp;
import jio.test.junit.Debugger;
import jio.test.stub.StubBuilder;
import jsonvalues.JsArray;
import jsonvalues.JsInt;
import jsonvalues.JsObj;
import jsonvalues.JsStr;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

public class TestDebuggerExp {

  @RegisterExtension
  static Debugger debugger = Debugger.of(Duration.ofSeconds(2));

  @Test
  public void testAllExp() throws Exception {

    Assertions.assertTrue(AllExp.seq(IO.TRUE,
                                     IO.TRUE
    )
                                .debugEach("test")
                                .call()
                                .call()
    );
    Assertions.assertFalse(AllExp.seq(IO.FALSE,
                                      IO.TRUE
    )
                                 .debugEach("test1")
                                 .call()
                                 .call()
    );
    Assertions.assertFalse(AllExp.par(IO.FALSE,
                                      IO.TRUE
    )
                                 .debugEach("test2")
                                 .call()
                                 .call()
    );
    Assertions.assertFalse(AllExp.par(IO.FALSE,
                                      IO.TRUE
    )
                                 .debugEach("test3")
                                 .call()
                                 .call()
    );

  }

  @Test
  public void testAllExpSeqRetries() throws Exception {

    StubBuilder<Boolean> trueAfterFailure = StubBuilder.ofGen(Gen.seq(n -> n <= 1
        ? IO.fail(new RuntimeException(Integer.toString(n)))
        : IO.TRUE));

    Assertions.assertTrue(AllExp.seq(trueAfterFailure.get(),
                                     trueAfterFailure.get()
    )
                                .debugEach("test")
                                .retryEach(RetryPolicies.limitRetries(1))
                                .call()
                                .call()
    );

    StubBuilder<Boolean> falseAfterFailure = StubBuilder.ofGen(Gen.seq(n -> n <= 1
        ? IO.fail(new RuntimeException(Integer.toString(n)))
        : IO.FALSE));

    // second effect is not evaluated since the first one is false
    Assertions.assertFalse(AllExp.seq(falseAfterFailure.get(),
                                      falseAfterFailure.get()
    )
                                 .debugEach("test1")
                                 .retryEach(RetryPolicies.limitRetries(1))
                                 .call()
                                 .call()
    );

  }

  @Test
  public void testAllExpParRetries() throws Exception {
    StubBuilder<Boolean> trueAfterFailure = StubBuilder.ofGen(Gen.seq(n -> n <= 1
        ? IO.fail(new RuntimeException(Integer.toString(n)))
        : IO.TRUE));

    Assertions.assertTrue(AllExp.par(trueAfterFailure.get(),
                                     trueAfterFailure.get()
    )
                                .debugEach("test")
                                .retryEach(RetryPolicies.limitRetries(1))
                                .call()
                                .call()
    );

    StubBuilder<Boolean> falseAfterFailure = StubBuilder.ofGen(Gen.seq(n -> n <= 1
        ? IO.fail(new RuntimeException(Integer.toString(n)))
        : IO.FALSE));

    // all effects are evaluated even the first one is false,not like with the seq constructor
    Assertions.assertFalse(AllExp.par(falseAfterFailure.get(),
                                      falseAfterFailure.get()
    )
                                 .debugEach("test1")
                                 .retryEach(RetryPolicies.limitRetries(1))
                                 .call()
                                 .call()
    );
  }

  @Test
  public void testAnyExp() throws Exception {

    Assertions.assertTrue(AnyExp.seq(IO.TRUE,
                                     IO.TRUE)
                                .debugEach("test")
                                .call()
                                .call());
    Assertions.assertTrue(AnyExp.seq(IO.FALSE,
                                     IO.TRUE)
                                .debugEach("test1")
                                .call()
                                .call());

    Assertions.assertFalse(AnyExp.par(IO.FALSE,
                                      IO.FALSE)
                                 .debugEach("test2")
                                 .call()
                                 .call());
    Assertions.assertFalse(AnyExp.par(IO.FALSE,
                                      IO.FALSE)
                                 .debugEach("test3")
                                 .call()
                                 .call());

  }

  @Test
  public void testCondExp() throws Exception {

    Assertions.assertEquals("b",
                            CondExp.seq(IO.FALSE,
                                        () -> IO.succeed("a"),
                                        IO.TRUE,
                                        () -> IO.succeed("b"),
                                        () -> IO.succeed("default")
                            )
                                   .debugEach("test")
                                   .call()
                                   .call()
    );

    Assertions.assertEquals("b",
                            CondExp.par(IO.FALSE,
                                        () -> IO.succeed("a"),
                                        IO.TRUE,
                                        () -> IO.succeed("b"),
                                        () -> IO.succeed("default")
                            )
                                   .debugEach("test")
                                   .call()
                                   .call()
    );

    Assertions.assertEquals("a",
                            CondExp.seq(IO.TRUE,
                                        () -> IO.succeed("a"),
                                        IO.TRUE,
                                        () -> IO.succeed("b"),
                                        () -> IO.succeed("default")
                            )
                                   .debugEach("test1")
                                   .call()
                                   .call()
    );

    Assertions.assertEquals("a",
                            CondExp.par(IO.TRUE,
                                        () -> IO.succeed("a"),
                                        IO.TRUE,
                                        () -> IO.succeed("b"),
                                        () -> IO.succeed("default")
                            )
                                   .debugEach("test2")
                                   .call()
                                   .call()
    );

  }

  @Test
  public void testIfElse() throws Exception {
    Assertions.assertEquals("a",
                            IfElseExp.predicate(IO.FALSE)
                                     .consequence(() -> IO.succeed("b"))
                                     .alternative(() -> IO.succeed("a"))
                                     .debugEach("test1")
                                     .call()
                                     .call()
    );
    Assertions.assertEquals("b",
                            IfElseExp.predicate(IO.TRUE)
                                     .consequence(() -> IO.succeed("b"))
                                     .alternative(() -> IO.succeed("a"))
                                     .debugEach("test2")
                                     .call()
                                     .call()
    );

  }

  @Test
  public void testJsArrayExp() throws Exception {

    Assertions.assertEquals(JsArray.of("a",
                                       "b"),
                            JsArrayExp.seq(IO.succeed("a")
                                             .map(JsStr::of),
                                           IO.succeed("b")
                                             .map(JsStr::of)
                            )
                                      .debugEach("test")
                                      .call()
                                      .call()
    );

    Assertions.assertEquals(JsArray.of("a",
                                       "b"),
                            JsArrayExp.par(IO.succeed("a")
                                             .map(JsStr::of),
                                           IO.succeed("b")
                                             .map(JsStr::of)
                            )
                                      .debugEach("test")
                                      .call()
                                      .call()
    );
  }

  @Test
  public void testJsObjExp() throws Exception {

    Assertions.assertEquals(JsObj.of("a",
                                     JsObj.of("a",
                                              JsInt.of(1),
                                              "b",
                                              JsInt.of(2)
                                     ),
                                     "b",
                                     JsArray.of("a",
                                                "b")
    ),
                            JsObjExp.seq("a",
                                         JsObjExp.seq("a",
                                                      IO.succeed(1)
                                                        .map(JsInt::of),
                                                      "b",
                                                      IO.succeed(2)
                                                        .map(JsInt::of)
                                         ),
                                         "b",
                                         JsArrayExp.seq(IO.succeed("a")
                                                          .map(JsStr::of),
                                                        IO.succeed("b")
                                                          .map(JsStr::of)
                                         )

                            )
                                    .debugEach("test")
                                    .call()
                                    .call()
    );

    Assertions.assertEquals(JsObj.of("a",
                                     JsObj.of("a",
                                              JsInt.of(1),
                                              "b",
                                              JsInt.of(2)
                                     ),
                                     "b",
                                     JsArray.of("a",
                                                "b")
    ),
                            JsObjExp.par("a",
                                         JsObjExp.par("a",
                                                      IO.succeed(1)
                                                        .map(JsInt::of),
                                                      "b",
                                                      IO.succeed(2)
                                                        .map(JsInt::of)
                                         ),
                                         "b",
                                         JsArrayExp.par(IO.succeed("a")
                                                          .map(JsStr::of),
                                                        IO.succeed("b")
                                                          .map(JsStr::of)
                                         )

                            )
                                    .debugEach("test")
                                    .call()
                                    .call()
    );

  }

  @Test
  public void testListExp() throws Exception {

    Assertions.assertEquals(List.of(1,
                                    2,
                                    3),
                            ListExp.seq(IO.succeed(1),
                                        IO.succeed(2),
                                        IO.succeed(3)
                            )
                                   .debugEach("test")
                                   .call()
                                   .call()
    );

    Assertions.assertEquals(List.of(1,
                                    2,
                                    3),
                            ListExp.par(IO.succeed(1),
                                        IO.succeed(2),
                                        IO.succeed(3)
                            )
                                   .debugEach("test1")
                                   .call()
                                   .call()
    );

  }

  @Test
  public void testPairExp() throws Exception {

    Assertions.assertEquals(Pair.of(1,
                                    2),
                            PairExp.seq(IO.succeed(1),
                                        IO.succeed(2)
                            )
                                   .debugEach("test1")
                                   .call()
                                   .call()
    );

    Assertions.assertEquals(Pair.of(1,
                                    2),
                            PairExp.par(IO.succeed(1),
                                        IO.succeed(2)
                            )
                                   .debugEach("test2")
                                   .call()
                                   .call()
    );

  }

  @Test
  public void testTripleExp() throws Exception {
    Assertions.assertEquals(Triple.of(1,
                                      2,
                                      3),
                            TripleExp.seq(IO.succeed(1),
                                          IO.succeed(2),
                                          IO.succeed(3)
                            )
                                     .debugEach("context")
                                     .call()
                                     .call()
    );

    Assertions.assertEquals(Triple.of(1,
                                      2,
                                      3),
                            TripleExp.par(IO.succeed(1),
                                          IO.succeed(2),
                                          IO.succeed(3)
                            )
                                     .debugEach("test2")
                                     .call()
                                     .call()
    );

  }

  @Test
  public void testSwitchExp() {

    Assertions.assertEquals(new Success<>("two"),
                            SwitchExp.<Integer, String>eval(IO.succeed(2))
                                     .match(new Success<>(1),
                                            _ -> IO.succeed("one"),
                                            new Success<>(2),
                                            _ -> IO.succeed("two"),
                                            _ -> IO.succeed("default")
                                     )
                                     .debugEach("testSwitchExp")
                                     .result()
    );

  }

}
