package jio.api;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.Instant;
import java.util.stream.Collectors;
import jio.AllExp;
import jio.IO;
import jio.IfElseExp;
import jio.JsArrayExp;
import jio.JsObjExp;
import jio.Result;
import jio.Result.Success;
import jsonvalues.JsArray;
import jsonvalues.JsBool;
import jsonvalues.JsInt;
import jsonvalues.JsObj;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ConstructorsTest {

  @Test
  public void succeed_constructor() throws Exception {

    IO<String> foo = IO.succeed("foo");

    Assertions.assertEquals(new Success<>("foo"),
                            foo.compute());

    Instant before = Instant.now();
    IO<Instant> now = IO.lazy(Instant::now);

    Assertions.assertTrue(before.isBefore(now.compute()
                                             .getOutputOrThrow()));

  }

  @Test
  public void testAll() {

    IO<Boolean> par = AllExp.par(IO.FALSE,
                                 IO.TRUE,
                                 IO.FALSE)
                            .debugEach("my-op");

    Assertions.assertEquals(Result.FALSE,
                            par.compute());

    IO<Boolean> seq = AllExp.seq(IO.FALSE,
                                 IO.TRUE,
                                 IO.FALSE)
                            .debugEach("my-op");

    Assertions.assertEquals(Result.FALSE,
                            seq.compute());

  }

  @Test
  public void testIfElse() {
    Assertions.assertEquals(new Success<>("alternative"),
                            IfElseExp.<String>predicate(IO.FALSE)
                                     .consequence(() -> IO.succeed("consequence"))
                                     .alternative(() -> IO.succeed("alternative"))
                                     .debugEach("my-op")
                                     .compute()
                           );
  }

  @Test
  public void testJsObj() {
    Assertions.assertEquals(
        new Success<>(JsObj.of("a",
                               JsInt.of(1),
                               "b",
                               JsInt.of(2),
                               "c",
                               JsInt.of(3),
                               "d",
                               JsObj.of("e",
                                        JsInt.of(4),
                                        "f",
                                        JsInt.of(5),
                                        "g",
                                        JsArray.of(true,
                                                   false)
                                       )
                              )),
        JsObjExp.par("a",
                     IO.succeed(1)
                       .map(JsInt::of),
                     "b",
                     IO.succeed(2)
                       .map(JsInt::of),
                     "c",
                     IO.succeed(3)
                       .map(JsInt::of),
                     "d",
                     JsObjExp.seq("e",
                                  IO.succeed(4)
                                    .map(JsInt::of),
                                  "f",
                                  IO.succeed(5)
                                    .map(JsInt::of),
                                  "g",
                                  JsArrayExp.seq(IO.TRUE.map(JsBool::of),
                                                 IO.FALSE.map(JsBool::of)
                                                )
                                 )
                    )
                .debugEach("my-op")
                .compute());
  }

  @Test
  public void testResource() {

    Result<String> a = IO.resource(() -> {
                                     File file = File.createTempFile("example",
                                                                     "text");
                                     Files.writeString(file.toPath(),
                                                       "hi");
                                     return new BufferedReader(
                                         new FileReader(file,
                                                        StandardCharsets.UTF_8));
                                   },
                                   it -> IO.succeed(it.lines()
                                                      .collect(Collectors.joining())))
                         .compute();

    Assertions.assertEquals(new Success<>("hi"),
                            a);
  }

  @Test
  public void testOn() {

    try {
      IO.task(() -> {
          throw new IllegalArgumentException("hi");
        })
        .debug()
        .compute()
        .getOutputOrThrow();
    } catch (Exception e) {
      Assertions.assertEquals("hi",
                              e.getMessage());
    }

    try {
      IO.task(() -> {
                throw new IllegalArgumentException("hi");
              }
             )
        .debug()
        .compute()
        .getOutputOrThrow();
    } catch (Exception e) {
      Assertions.assertEquals("hi",
                              e.getMessage());
    }
  }

}
