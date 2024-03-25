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
import jio.Result;
import jio.Result.Failure;
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

public class DebuggerExpTests {

  @RegisterExtension
  static Debugger debugger = Debugger.of(Duration.ofSeconds(2));

  @Test
  public void testAllExp() throws Exception {

    Assertions.assertTrue(AllExp.seq(IO.TRUE,
                                     IO.TRUE
                                    )
                                .debugEach("test")
                                .compute()
                                .isSuccess(isAllTrue -> isAllTrue)
                         );
    Assertions.assertFalse(AllExp.seq(IO.FALSE,
                                      IO.TRUE
                                     )
                                 .debugEach("test1")
                                 .compute()
                                 .isSuccess(isAllTrue -> isAllTrue)
                          );
    Assertions.assertFalse(AllExp.par(IO.FALSE,
                                      IO.TRUE
                                     )
                                 .debugEach("test2")
                                 .compute()
                                 .isSuccess(isAllTrue -> isAllTrue)
                          );
    Assertions.assertFalse(AllExp.par(IO.FALSE,
                                      IO.TRUE
                                     )
                                 .debugEach("test3")
                                 .compute()
                                 .isSuccess(isAllTrue -> isAllTrue)
                          );

  }

  @Test
  public void testAllExpSeqRetries() throws Exception {

    StubBuilder<Boolean> trueAfterFailure =
        StubBuilder.ofGen(Gen.seq(n -> n <= 1
                                       ? new Failure<>(new RuntimeException(Integer.toString(n)))
                                       : Result.TRUE));

    Assertions.assertTrue(AllExp.seq(trueAfterFailure.get(),
                                     trueAfterFailure.get()
                                    )
                                .debugEach("test")
                                .retryEach(RetryPolicies.limitRetries(1))
                                .call()
                                .getOutputOrThrow()
                         );

    StubBuilder<Boolean> falseAfterFailure =
        StubBuilder.ofGen(Gen.seq(n -> n <= 1
                                       ? new Failure<>(new RuntimeException(Integer.toString(n)))
                                       : Result.FALSE));

    // second effect is not evaluated since the first one is false
    Assertions.assertFalse(AllExp.seq(falseAfterFailure.get(),
                                      falseAfterFailure.get()
                                     )
                                 .debugEach("test1")
                                 .retryEach(RetryPolicies.limitRetries(1))
                                 .call()
                                 .getOutputOrThrow()
                          );

  }

  @Test
  public void testAllExpParRetries() throws Exception {
    StubBuilder<Boolean> trueAfterFailure =
        StubBuilder.ofGen(Gen.seq(n -> n <= 1
                                       ? new Failure<>(new RuntimeException(Integer.toString(n)))
                                       : Result.TRUE));

    Assertions.assertTrue(AllExp.par(trueAfterFailure.get(),
                                     trueAfterFailure.get()
                                    )
                                .debugEach("test")
                                .retryEach(RetryPolicies.limitRetries(1))
                                .call()
                                .getOutputOrThrow()
                         );

    StubBuilder<Boolean> falseAfterFailure =
        StubBuilder.ofGen(Gen.seq(n -> n <= 1
                                       ? new Failure<>(new RuntimeException(Integer.toString(n)))
                                       : Result.FALSE));

    // all effects are evaluated even the first one is false,not like with the seq constructor
    Assertions.assertFalse(AllExp.par(falseAfterFailure.get(),
                                      falseAfterFailure.get()
                                     )
                                 .debugEach("test1")
                                 .retryEach(RetryPolicies.limitRetries(1))
                                 .call()
                                 .getOutputOrThrow()
                          );
  }

  @Test
  public void testAnyExp() throws Exception {

    Assertions.assertTrue(AnyExp.seq(IO.TRUE,
                                     IO.TRUE)
                                .debugEach("test")
                                .call()
                                .getOutputOrThrow());
    Assertions.assertTrue(AnyExp.seq(IO.FALSE,
                                     IO.TRUE)
                                .debugEach("test1")
                                .call()
                                .getOutputOrThrow());

    Assertions.assertFalse(AnyExp.par(IO.FALSE,
                                      IO.FALSE)
                                 .debugEach("test2")
                                 .call()
                                 .getOutputOrThrow());
    Assertions.assertFalse(AnyExp.par(IO.FALSE,
                                      IO.FALSE)
                                 .debugEach("test3")
                                 .call()
                                 .getOutputOrThrow());

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
                                   .getOutputOrThrow()
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
                                   .getOutputOrThrow()
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
                                   .getOutputOrThrow()
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
                                   .getOutputOrThrow()
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
                                     .getOutputOrThrow()
                           );
    Assertions.assertEquals("b",
                            IfElseExp.predicate(IO.TRUE)
                                     .consequence(() -> IO.succeed("b"))
                                     .alternative(() -> IO.succeed("a"))
                                     .debugEach("test2")
                                     .call()
                                     .getOutputOrThrow()
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
                                      .getOutputOrThrow()
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
                                      .getOutputOrThrow()
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
                                    .getOutputOrThrow()
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
                                    .getOutputOrThrow()
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
                                   .getOutputOrThrow()
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
                                   .getOutputOrThrow()
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
                                   .getOutputOrThrow()
                           );

    Assertions.assertEquals(Pair.of(1,
                                    2),
                            PairExp.par(IO.succeed(1),
                                        IO.succeed(2)
                                       )
                                   .debugEach("test2")
                                   .call()
                                   .getOutputOrThrow()
                           );

  }

  @Test
  public void testTripleExp() throws Exception {
    Assertions.assertEquals(new Success<>(Triple.of(1,
                                                    2,
                                                    3)),
                            TripleExp.seq(IO.succeed(1),
                                          IO.succeed(2),
                                          IO.succeed(3)
                                         )
                                     .debugEach("context")
                                     .compute()
                           );

    Assertions.assertEquals(new Success<>(Triple.of(1,
                                                    2,
                                                    3)),
                            TripleExp.par(IO.succeed(1),
                                          IO.succeed(2),
                                          IO.succeed(3)
                                         )
                                     .debugEach("test2")
                                     .compute()
                           );

  }

  @Test
  public void testSwitchExp() {

    Assertions.assertEquals(new Success<>("two"),
                            SwitchExp.<Integer, String>eval(IO.succeed(2))
                                     .match(1,
                                            _ -> IO.succeed("one"),
                                            2,
                                            _ -> IO.succeed("two"),
                                            _ -> IO.succeed("default")
                                           )
                                     .debugEach("testSwitchExp")
                                     .compute()
                           );

  }

}
