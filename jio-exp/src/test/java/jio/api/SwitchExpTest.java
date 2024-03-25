package jio.api;

import java.util.List;
import jio.IO;
import jio.Result;
import jio.Result.Success;
import jio.SwitchExp;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class SwitchExpTest {

  @Test
  public void test_object_constructors() {

    IO<String> a = SwitchExp.<String, String>eval(IO.succeed("a"))
                            .match("a",
                                   _ -> Constants.A,
                                   "b",
                                   _ -> Constants.B,
                                   _ -> Constants.C
                                  )
                            .debugEach("1")
                            .map(String::toUpperCase);

    Assertions.assertEquals(new Success<>("A"),
                            a.compute());

    Assertions.assertEquals(Result.NULL,
                            SwitchExp.<String, String>eval(IO.succeed("c"))
                                     .match("a",
                                            _ -> Constants.A,
                                            "b",
                                            _ -> Constants.B
                                           )
                                     .compute()
                           );

    IO<String> b = SwitchExp.<String, String>eval("b")
                            .match("a",
                                   _ -> Constants.A,
                                   "b",
                                   _ -> Constants.B,
                                   "c",
                                   _ -> Constants.C,
                                   _ -> Constants.C
                                  )
                            .debugEach("2")
                            .map(String::toUpperCase);

    Assertions.assertEquals(new Success<>("B"),
                            b.compute());

    IO<String> c = SwitchExp.<String, String>eval("c")
                            .match("a",
                                   _ -> Constants.A,
                                   "b",
                                   _ -> Constants.B,
                                   "c",
                                   _ -> Constants.C,
                                   "d",
                                   _ -> Constants.D,
                                   _ -> Constants.C
                                  )
                            .debugEach("3")
                            .map(String::toUpperCase);

    Assertions.assertEquals(new Success<>("C"),
                            c.compute());

    IO<String> d = SwitchExp.<String, String>eval("d")
                            .match("a",
                                   _ -> Constants.A,
                                   "b",
                                   _ -> Constants.B,
                                   "c",
                                   _ -> Constants.C,
                                   "d",
                                   _ -> Constants.D,
                                   "e",
                                   _ -> Constants.A,
                                   _ -> Constants.C
                                  )
                            .debugEach("4")
                            .map(String::toUpperCase);

    Assertions.assertEquals(new Success<>("D"),
                            d.compute());

    SwitchExp<String, String> patterns = SwitchExp.<String, String>eval("e")
                                                  .match("a",
                                                         _ -> Constants.A,
                                                         "b",
                                                         _ -> Constants.B,
                                                         "c",
                                                         _ -> Constants.C,
                                                         "d",
                                                         _ -> Constants.D,
                                                         "e",
                                                         _ -> Constants.A,
                                                         "f",
                                                         _ -> Constants.B,
                                                         _ -> Constants.C
                                                        );
    IO<String> e = patterns.debugEach("5")
                           .map(String::toUpperCase);

    Assertions.assertEquals(new Success<>("A"),
                            e.compute());

    IO<String> f = SwitchExp.<String, String>eval("h")
                            .match("a",
                                   _ -> Constants.A,
                                   "b",
                                   _ -> Constants.B,
                                   "c",
                                   _ -> Constants.C,
                                   "d",
                                   _ -> Constants.D,
                                   "e",
                                   _ -> Constants.A,
                                   "f",
                                   _ -> Constants.B,
                                   IO::succeed
                                  )
                            .debugEach("6")
                            .map(String::toUpperCase);

    Assertions.assertEquals(new Success<>("H"),
                            f.compute());

  }

  @Test
  public void test_list_constructors()  {

    IO<String> a = SwitchExp.<String, String>eval("a")
                            .matchList(List.of("a",
                                               "c"),
                                       _ -> Constants.A,
                                       List.of("b"),
                                       _ -> Constants.B,
                                       _ -> Constants.C
                                      )
                            .map(String::toUpperCase);

    Assertions.assertEquals(new Success<>("A"),
                            a.compute());

    Assertions.assertEquals(Result.NULL,
                            SwitchExp.<String, String>eval("d")
                                     .matchList(List.of("a",
                                                        "c"),
                                                _ -> Constants.A,
                                                List.of("b"),
                                                _ -> Constants.B
                                               )
                                     .compute()
                           );

    IO<String> b = SwitchExp.<String, String>eval("b")
                            .matchList(List.of("a"),
                                       _ -> Constants.A,
                                       List.of("b"),
                                       _ -> Constants.B,
                                       List.of("c"),
                                       _ -> Constants.C,
                                       _ -> Constants.C
                                      )
                            .map(String::toUpperCase);

    Assertions.assertEquals(new Success<>("B"),
                            b.compute());

    IO<String> c = SwitchExp.<String, String>eval("c")
                            .matchList(List.of("a"),
                                       _ -> Constants.A,
                                       List.of("b"),
                                       _ -> Constants.B,
                                       List.of("c"),
                                       _ -> Constants.C,
                                       List.of("d"),
                                       _ -> Constants.D,
                                       _ -> Constants.C
                                      )
                            .map(String::toUpperCase);

    Assertions.assertEquals(new Success<>("C"),
                            c.compute());

    IO<String> d = SwitchExp.<String, String>eval("d")
                            .matchList(List.of("a"),
                                       _ -> Constants.A,
                                       List.of("b"),
                                       _ -> Constants.B,
                                       List.of("c"),
                                       _ -> Constants.C,
                                       List.of("d"),
                                       _ -> Constants.D,
                                       List.of("e"),
                                       _ -> Constants.A,
                                       _ -> Constants.C
                                      )
                            .map(String::toUpperCase);

    Assertions.assertEquals(new Success<>("D"),
                            d.compute());

    IO<String> e = SwitchExp.<String, String>eval("e")
                            .matchList(List.of("a",
                                               "1"),
                                       _ -> Constants.A,
                                       List.of("b",
                                               "2"),
                                       _ -> Constants.B,
                                       List.of("c"),
                                       _ -> Constants.C,
                                       List.of("d"),
                                       _ -> Constants.D,
                                       List.of("e",
                                               "j"),
                                       _ -> Constants.A,
                                       List.of("f",
                                               "g"),
                                       _ -> Constants.B,
                                       _ -> Constants.C
                                      )
                            .map(String::toUpperCase);

    Assertions.assertEquals(new Success<>("A"),
                            e.compute());

    IO<String> f = SwitchExp.<String, String>eval("h")
                            .matchList(List.of("a"),
                                       _ -> Constants.A,
                                       List.of("b"),
                                       _ -> Constants.B,
                                       List.of("c"),
                                       _ -> Constants.C,
                                       List.of("d"),
                                       _ -> Constants.D,
                                       List.of("e"),
                                       _ -> Constants.A,
                                       List.of("f"),
                                       _ -> Constants.B,
                                       IO::succeed
                                      )
                            .map(String::toUpperCase);

    Assertions.assertEquals(new Success<>("H"),
                            f.compute());

  }

  @Test
  public void test_predicate_constructors() {

    IO<String> a = SwitchExp.<String, String>eval("a")
                            .matchPredicate(x -> x.equals("a"),
                                            _ -> Constants.A,
                                            x -> x.equals("b"),
                                            _ -> Constants.B,
                                            _ -> Constants.C
                                           )
                            .map(String::toUpperCase);

    Assertions.assertEquals(new Success<>("A"),
                            a.compute());

    Assertions.assertEquals(Result.NULL,
                            SwitchExp.<String, String>eval("c")
                                     .matchPredicate(x -> x.equals("a"),
                                                     _ -> Constants.A,
                                                     x -> x.equals("b"),
                                                     _ -> Constants.B
                                                    )
                                     .compute());

    IO<String> b = SwitchExp.<String, String>eval("b")
                            .matchPredicate(x -> x.equals("a"),
                                            _ -> Constants.A,
                                            x -> x.equals("b"),
                                            _ -> Constants.B,
                                            x -> x.equals("c"),
                                            _ -> Constants.C,
                                            _ -> Constants.C
                                           )
                            .map(String::toUpperCase);

    Assertions.assertEquals(new Success<>("B"),
                            b.compute());

    IO<String> c = SwitchExp.<String, String>eval("c")
                            .matchPredicate(x -> x.equals("a"),
                                            _ -> Constants.A,
                                            x -> x.equals("b"),
                                            _ -> Constants.B,
                                            x -> x.equals("c"),
                                            _ -> Constants.C,
                                            x -> x.equals("d"),
                                            _ -> Constants.D,
                                            _ -> Constants.C
                                           )
                            .map(String::toUpperCase);

    Assertions.assertEquals(new Success<>("C"),
                            c.compute());

    IO<String> d = SwitchExp.<String, String>eval("d")
                            .matchPredicate(x -> x.equals("a"),
                                            _ -> Constants.A,
                                            x -> x.equals("b"),
                                            _ -> Constants.B,
                                            x -> x.equals("c"),
                                            _ -> Constants.C,
                                            x -> x.equals("d"),
                                            _ -> Constants.D,
                                            x -> x.equals("e"),
                                            _ -> Constants.A,
                                            _ -> Constants.C
                                           )
                            .map(String::toUpperCase);

    Assertions.assertEquals(new Success<>("D"),
                            d.compute());

    IO<String> e = SwitchExp.<String, String>eval("e")
                            .matchPredicate(x -> x.equals("a"),
                                            _ -> Constants.A,
                                            x -> x.equals("b"),
                                            _ -> Constants.B,
                                            x -> x.equals("c"),
                                            _ -> Constants.C,
                                            x -> x.equals("d"),
                                            _ -> Constants.D,
                                            x -> x.equals("e"),
                                            _ -> Constants.A,
                                            x -> x.equals("f"),
                                            _ -> Constants.B,
                                            _ -> Constants.C
                                           )
                            .map(String::toUpperCase);

    Assertions.assertEquals(new Success<>("A"),
                            e.compute());

    IO<String> f = SwitchExp.<String, String>eval("h")
                            .matchPredicate(x -> x.equals("a"),
                                            _ -> Constants.A,
                                            x -> x.equals("b"),
                                            _ -> Constants.B,
                                            x -> x.equals("c"),
                                            _ -> Constants.C,
                                            x -> x.equals("d"),
                                            _ -> Constants.D,
                                            x -> x.equals("e"),
                                            _ -> Constants.A,
                                            x -> x.equals("f"),
                                            _ -> Constants.B,
                                            IO::succeed
                                           )
                            .map(String::toUpperCase);

    Assertions.assertEquals(new Success<>("H"),
                            f.compute());

  }

  @Test
  public void test_debug_each()  {
    var exp = SwitchExp.<Integer, String>eval(IO.succeed(2))
                       .match(1,
                              _ -> IO.succeed("one"),
                              2,
                              _ -> IO.succeed("two"),
                              _ -> IO.succeed("default")
                             )
                       .debugEach("context")
                       .compute();

    Assertions.assertEquals(new Success<>("two"),
                            exp
                           );

  }
}
