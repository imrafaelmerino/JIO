package jio.api;

import java.util.List;
import jio.IO;
import jio.Result;
import jio.SwitchExp;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class SwitchExpTest {

  @Test
  public void test_object_constructors() throws Exception {

    IO<String> a = SwitchExp.<String, String>eval(IO.succeed("a"))
                            .match(new Result.Success<>("a"),
                                   _ -> Constants.A,
                                   new Result.Success<>("b"),
                                   _ -> Constants.B,
                                   _ -> Constants.C
                                  )
                            .debugEach("1")
                            .map(String::toUpperCase);

    Assertions.assertEquals("A",
                            a.result()
                             .call());

    Assertions.assertNull(SwitchExp.<String, String>eval(IO.succeed("c"))
                                   .match(new Result.Success<>("a"),
                                          _ -> Constants.A,
                                          new Result.Success<>("b"),
                                          _ -> Constants.B
                                         )
                                   .result()
                                   .call()
                         );

    IO<String> b = SwitchExp.<String, String>eval("b")
                            .match(new Result.Success<>("a"),
                                   _ -> Constants.A,
                                   new Result.Success<>("b"),
                                   _ -> Constants.B,
                                   new Result.Success<>("c"),
                                   _ -> Constants.C,
                                   _ -> Constants.C
                                  )
                            .debugEach("2")
                            .map(String::toUpperCase);

    Assertions.assertEquals("B",
                            b.result()
                             .call());

    IO<String> c = SwitchExp.<String, String>eval("c")
                            .match(new Result.Success<>("a"),
                                   _ -> Constants.A,
                                   new Result.Success<>("b"),
                                   _ -> Constants.B,
                                   new Result.Success<>("c"),
                                   _ -> Constants.C,
                                   new Result.Success<>("d"),
                                   _ -> Constants.D,
                                   _ -> Constants.C
                                  )
                            .debugEach("3")
                            .map(String::toUpperCase);

    Assertions.assertEquals("C",
                            c.result()
                             .call());

    IO<String> d = SwitchExp.<String, String>eval("d")
                            .match(new Result.Success<>("a"),
                                   _ -> Constants.A,
                                   new Result.Success<>("b"),
                                   _ -> Constants.B,
                                   new Result.Success<>("c"),
                                   _ -> Constants.C,
                                   new Result.Success<>("d"),
                                   _ -> Constants.D,
                                   new Result.Success<>("e"),
                                   _ -> Constants.A,
                                   _ -> Constants.C
                                  )
                            .debugEach("4")
                            .map(String::toUpperCase);

    Assertions.assertEquals("D",
                            d.result()
                             .call());

    SwitchExp<String, String> patterns = SwitchExp.<String, String>eval("e")
                                                  .match(new Result.Success<>("a"),
                                                         _ -> Constants.A,
                                                         new Result.Success<>("b"),
                                                         _ -> Constants.B,
                                                         new Result.Success<>("c"),
                                                         _ -> Constants.C,
                                                         new Result.Success<>("d"),
                                                         _ -> Constants.D,
                                                         new Result.Success<>("e"),
                                                         _ -> Constants.A,
                                                         new Result.Success<>("f"),
                                                         _ -> Constants.B,
                                                         _ -> Constants.C
                                                        );
    IO<String> e = patterns.debugEach("5")
                           .map(String::toUpperCase);

    Assertions.assertEquals("A",
                            e.result()
                             .call());

    IO<String> f = SwitchExp.<String, String>eval("h")
                            .match(new Result.Success<>("a"),
                                   _ -> Constants.A,
                                   new Result.Success<>("b"),
                                   _ -> Constants.B,
                                   new Result.Success<>("c"),
                                   _ -> Constants.C,
                                   new Result.Success<>("d"),
                                   _ -> Constants.D,
                                   new Result.Success<>("e"),
                                   _ -> Constants.A,
                                   new Result.Success<>("f"),
                                   _ -> Constants.B,
                                   IO::task
                                  )
                            .debugEach("6")
                            .map(String::toUpperCase);

    Assertions.assertEquals("H",
                            f.result()
                             .call());

  }

  @Test
  public void test_list_constructors() throws Exception {

    IO<String> a = SwitchExp.<String, String>eval("a")
                            .matchList(List.of(new Result.Success<>("a"),
                                               new Result.Success<>("c")),
                                       _ -> Constants.A,
                                       List.of(new Result.Success<>("b")),
                                       _ -> Constants.B,
                                       _ -> Constants.C
                                      )
                            .map(String::toUpperCase);

    Assertions.assertEquals("A",
                            a.result()
                             .call());

    Assertions.assertNull(SwitchExp.<String, String>eval("d")
                                   .matchList(List.of(new Result.Success<>("a"),
                                                      new Result.Success<>("c")),
                                              _ -> Constants.A,
                                              List.of(new Result.Success<>("b")),
                                              _ -> Constants.B
                                             )
                                   .result()
                                   .call()
                         );

    IO<String> b = SwitchExp.<String, String>eval("b")
                            .matchList(List.of(new Result.Success<>("a")),
                                       _ -> Constants.A,
                                       List.of(new Result.Success<>("b")),
                                       _ -> Constants.B,
                                       List.of(new Result.Success<>("c")),
                                       _ -> Constants.C,
                                       _ -> Constants.C
                                      )
                            .map(String::toUpperCase);

    Assertions.assertEquals("B",
                            b.result()
                             .call());

    IO<String> c = SwitchExp.<String, String>eval("c")
                            .matchList(List.of(new Result.Success<>("a")),
                                       _ -> Constants.A,
                                       List.of(new Result.Success<>("b")),
                                       _ -> Constants.B,
                                       List.of(new Result.Success<>("c")),
                                       _ -> Constants.C,
                                       List.of(new Result.Success<>("d")),
                                       _ -> Constants.D,
                                       _ -> Constants.C
                                      )
                            .map(String::toUpperCase);

    Assertions.assertEquals("C",
                            c.result()
                             .call());

    IO<String> d = SwitchExp.<String, String>eval("d")
                            .matchList(List.of(new Result.Success<>("a")),
                                       _ -> Constants.A,
                                       List.of(new Result.Success<>("b")),
                                       _ -> Constants.B,
                                       List.of(new Result.Success<>("c")),
                                       _ -> Constants.C,
                                       List.of(new Result.Success<>("d")),
                                       _ -> Constants.D,
                                       List.of(new Result.Success<>("e")),
                                       _ -> Constants.A,
                                       _ -> Constants.C
                                      )
                            .map(String::toUpperCase);

    Assertions.assertEquals("D",
                            d.result()
                             .call());

    IO<String> e = SwitchExp.<String, String>eval("e")
                            .matchList(List.of(new Result.Success<>("a"),
                                               new Result.Success<>("1")),
                                       _ -> Constants.A,
                                       List.of(new Result.Success<>("b"),
                                               new Result.Success<>("2")),
                                       _ -> Constants.B,
                                       List.of(new Result.Success<>("c")),
                                       _ -> Constants.C,
                                       List.of(new Result.Success<>("d")),
                                       _ -> Constants.D,
                                       List.of(new Result.Success<>("e"),
                                               new Result.Success<>("j")),
                                       _ -> Constants.A,
                                       List.of(new Result.Success<>("f"),
                                               new Result.Success<>("g")),
                                       _ -> Constants.B,
                                       _ -> Constants.C
                                      )
                            .map(String::toUpperCase);

    Assertions.assertEquals("A",
                            e.result()
                             .call());

    IO<String> f = SwitchExp.<String, String>eval("h")
                            .matchList(List.of(new Result.Success<>("a")),
                                       _ -> Constants.A,
                                       List.of(new Result.Success<>("b")),
                                       _ -> Constants.B,
                                       List.of(new Result.Success<>("c")),
                                       _ -> Constants.C,
                                       List.of(new Result.Success<>("d")),
                                       _ -> Constants.D,
                                       List.of(new Result.Success<>("e")),
                                       _ -> Constants.A,
                                       List.of(new Result.Success<>("f")),
                                       _ -> Constants.B,
                                       IO::task
                                      )
                            .map(String::toUpperCase);

    Assertions.assertEquals("H",
                            f.result()
                             .call());

  }

  @Test
  public void test_predicate_constructors() throws Exception {

    IO<String> a = SwitchExp.<String, String>eval("a")
                            .matchPredicate(x -> x.equals(new Result.Success<>("a")),
                                            _ -> Constants.A,
                                            x -> x.equals(new Result.Success<>("b")),
                                            _ -> Constants.B,
                                            _ -> Constants.C
                                           )
                            .map(String::toUpperCase);

    Assertions.assertEquals("A",
                            a.result()
                             .call());

    Assertions.assertNull(SwitchExp.<String, String>eval("c")
                                   .matchPredicate(x -> x.equals(new Result.Success<>("a")),
                                                   _ -> Constants.A,
                                                   x -> x.equals(new Result.Success<>("b")),
                                                   _ -> Constants.B
                                                  )
                                   .result()
                                   .call());

    IO<String> b = SwitchExp.<String, String>eval("b")
                            .matchPredicate(x -> x.equals(new Result.Success<>("a")),
                                            _ -> Constants.A,
                                            x -> x.equals(new Result.Success<>("b")),
                                            _ -> Constants.B,
                                            x -> x.equals(new Result.Success<>("c")),
                                            _ -> Constants.C,
                                            _ -> Constants.C
                                           )
                            .map(String::toUpperCase);

    Assertions.assertEquals("B",
                            b.result()
                             .call());

    IO<String> c = SwitchExp.<String, String>eval("c")
                            .matchPredicate(x -> x.equals(new Result.Success<>("a")),
                                            _ -> Constants.A,
                                            x -> x.equals(new Result.Success<>("b")),
                                            _ -> Constants.B,
                                            x -> x.equals(new Result.Success<>("c")),
                                            _ -> Constants.C,
                                            x -> x.equals(new Result.Success<>("d")),
                                            _ -> Constants.D,
                                            _ -> Constants.C
                                           )
                            .map(String::toUpperCase);

    Assertions.assertEquals("C",
                            c.result()
                             .call());

    IO<String> d = SwitchExp.<String, String>eval("d")
                            .matchPredicate(x -> x.equals(new Result.Success<>("a")),
                                            _ -> Constants.A,
                                            x -> x.equals(new Result.Success<>("b")),
                                            _ -> Constants.B,
                                            x -> x.equals(new Result.Success<>("c")),
                                            _ -> Constants.C,
                                            x -> x.equals(new Result.Success<>("d")),
                                            _ -> Constants.D,
                                            x -> x.equals(new Result.Success<>("e")),
                                            _ -> Constants.A,
                                            _ -> Constants.C
                                           )
                            .map(String::toUpperCase);

    Assertions.assertEquals("D",
                            d.result()
                             .call());

    IO<String> e = SwitchExp.<String, String>eval("e")
                            .matchPredicate(x -> x.equals(new Result.Success<>("a")),
                                            _ -> Constants.A,
                                            x -> x.equals(new Result.Success<>("b")),
                                            _ -> Constants.B,
                                            x -> x.equals(new Result.Success<>("c")),
                                            _ -> Constants.C,
                                            x -> x.equals(new Result.Success<>("d")),
                                            _ -> Constants.D,
                                            x -> x.equals(new Result.Success<>("e")),
                                            _ -> Constants.A,
                                            x -> x.equals(new Result.Success<>("f")),
                                            _ -> Constants.B,
                                            _ -> Constants.C
                                           )
                            .map(String::toUpperCase);

    Assertions.assertEquals("A",
                            e.result()
                             .call());

    IO<String> f = SwitchExp.<String, String>eval("h")
                            .matchPredicate(x -> x.equals(new Result.Success<>("a")),
                                            _ -> Constants.A,
                                            x -> x.equals(new Result.Success<>("b")),
                                            _ -> Constants.B,
                                            x -> x.equals(new Result.Success<>("c")),
                                            _ -> Constants.C,
                                            x -> x.equals(new Result.Success<>("d")),
                                            _ -> Constants.D,
                                            x -> x.equals(new Result.Success<>("e")),
                                            _ -> Constants.A,
                                            x -> x.equals(new Result.Success<>("f")),
                                            _ -> Constants.B,
                                            IO::task
                                           )
                            .map(String::toUpperCase);

    Assertions.assertEquals("H",
                            f.result()
                             .call());

  }

  @Test
  public void test_debug_each() throws Exception {
    var exp = SwitchExp.<Integer, String>eval(IO.succeed(2))
                       .match(new Result.Success<>(1),
                              _ -> IO.succeed("one"),
                              new Result.Success<>(2),
                              _ -> IO.succeed("two"),
                              _ -> IO.succeed("default")
                             )
                       .debugEach("context")
                       .result()
                       .call();

    Assertions.assertEquals("two",
                            exp
                           );

  }
}
