package jio.api;

import java.util.List;
import jio.IO;
import jio.SwitchExp;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TestSwitchExp {

  @Test
  public void test_object_constructors() throws Exception {

    IO<String> a = SwitchExp.<String, String>eval(IO.succeed("a"))
                            .match("a",
                                   x -> Constants.A,
                                   "b",
                                   x -> Constants.B,
                                   x -> Constants.C
                            )
                            .debugEach("1")
                            .map(String::toUpperCase);

    Assertions.assertEquals("A",
                            a.get()
                             .call());

    Assertions.assertNull(SwitchExp.<String, String>eval(IO.succeed("c"))
                                   .match("a",
                                          x -> Constants.A,
                                          "b",
                                          x -> Constants.B
                                   )
                                   .get()
                                   .call()
    );

    IO<String> b = SwitchExp.<String, String>eval("b")
                            .match("a",
                                   x -> Constants.A,
                                   "b",
                                   x -> Constants.B,
                                   "c",
                                   x -> Constants.C,
                                   x -> Constants.C
                            )
                            .debugEach("2")
                            .map(String::toUpperCase);

    Assertions.assertEquals("B",
                            b.get()
                             .call());

    IO<String> c = SwitchExp.<String, String>eval("c")
                            .match("a",
                                   x -> Constants.A,
                                   "b",
                                   x -> Constants.B,
                                   "c",
                                   x -> Constants.C,
                                   "d",
                                   x -> Constants.D,
                                   x -> Constants.C
                            )
                            .debugEach("3")
                            .map(String::toUpperCase);

    Assertions.assertEquals("C",
                            c.get()
                             .call());

    IO<String> d = SwitchExp.<String, String>eval("d")
                            .match("a",
                                   x -> Constants.A,
                                   "b",
                                   x -> Constants.B,
                                   "c",
                                   x -> Constants.C,
                                   "d",
                                   x -> Constants.D,
                                   "e",
                                   x -> Constants.A,
                                   x -> Constants.C
                            )
                            .debugEach("4")
                            .map(String::toUpperCase);

    Assertions.assertEquals("D",
                            d.get()
                             .call());

    SwitchExp<String, String> patterns = SwitchExp.<String, String>eval("e")
                                                  .match("a",
                                                         x -> Constants.A,
                                                         "b",
                                                         x -> Constants.B,
                                                         "c",
                                                         x -> Constants.C,
                                                         "d",
                                                         x -> Constants.D,
                                                         "e",
                                                         x -> Constants.A,
                                                         "f",
                                                         x -> Constants.B,
                                                         x -> Constants.C
                                                  );
    IO<String> e = patterns.debugEach("5")
                           .map(String::toUpperCase);

    Assertions.assertEquals("A",
                            e.get()
                             .call());

    IO<String> f = SwitchExp.<String, String>eval("h")
                            .match("a",
                                   x -> Constants.A,
                                   "b",
                                   x -> Constants.B,
                                   "c",
                                   x -> Constants.C,
                                   "d",
                                   x -> Constants.D,
                                   "e",
                                   x -> Constants.A,
                                   "f",
                                   x -> Constants.B,
                                   IO::succeed
                            )
                            .debugEach("6")
                            .map(String::toUpperCase);

    Assertions.assertEquals("H",
                            f.get()
                             .call());

  }

  @Test
  public void test_list_constructors() throws Exception {

    IO<String> a = SwitchExp.<String, String>eval("a")
                            .match(List.of("a",
                                           "c"),
                                   x -> Constants.A,
                                   List.of("b"),
                                   x -> Constants.B,
                                   x -> Constants.C
                            )
                            .map(String::toUpperCase);

    Assertions.assertEquals("A",
                            a.get()
                             .call());

    Assertions.assertNull(SwitchExp.<String, String>eval("d")
                                   .match(List.of("a",
                                                  "c"),
                                          x -> Constants.A,
                                          List.of("b"),
                                          x -> Constants.B
                                   )
                                   .get()
                                   .call()
    );

    IO<String> b = SwitchExp.<String, String>eval("b")
                            .match(List.of("a"),
                                   x -> Constants.A,
                                   List.of("b"),
                                   x -> Constants.B,
                                   List.of("c"),
                                   x -> Constants.C,
                                   x -> Constants.C
                            )
                            .map(String::toUpperCase);

    Assertions.assertEquals("B",
                            b.get()
                             .call());

    IO<String> c = SwitchExp.<String, String>eval("c")
                            .match(List.of("a"),
                                   x -> Constants.A,
                                   List.of("b"),
                                   x -> Constants.B,
                                   List.of("c"),
                                   x -> Constants.C,
                                   List.of("d"),
                                   x -> Constants.D,
                                   x -> Constants.C
                            )
                            .map(String::toUpperCase);

    Assertions.assertEquals("C",
                            c.get()
                             .call());

    IO<String> d = SwitchExp.<String, String>eval("d")
                            .match(List.of("a"),
                                   x -> Constants.A,
                                   List.of("b"),
                                   x -> Constants.B,
                                   List.of("c"),
                                   x -> Constants.C,
                                   List.of("d"),
                                   x -> Constants.D,
                                   List.of("e"),
                                   x -> Constants.A,
                                   x -> Constants.C
                            )
                            .map(String::toUpperCase);

    Assertions.assertEquals("D",
                            d.get()
                             .call());

    IO<String> e = SwitchExp.<String, String>eval("e")
                            .match(List.of("a",
                                           "1"),
                                   x -> Constants.A,
                                   List.of("b",
                                           "2"),
                                   x -> Constants.B,
                                   List.of("c"),
                                   x -> Constants.C,
                                   List.of("d"),
                                   x -> Constants.D,
                                   List.of("e",
                                           "j"),
                                   x -> Constants.A,
                                   List.of("f",
                                           "g"),
                                   x -> Constants.B,
                                   x -> Constants.C
                            )
                            .map(String::toUpperCase);

    Assertions.assertEquals("A",
                            e.get()
                             .call());

    IO<String> f = SwitchExp.<String, String>eval("h")
                            .match(List.of("a"),
                                   x -> Constants.A,
                                   List.of("b"),
                                   x -> Constants.B,
                                   List.of("c"),
                                   x -> Constants.C,
                                   List.of("d"),
                                   x -> Constants.D,
                                   List.of("e"),
                                   x -> Constants.A,
                                   List.of("f"),
                                   x -> Constants.B,
                                   IO::succeed
                            )
                            .map(String::toUpperCase);

    Assertions.assertEquals("H",
                            f.get()
                             .call());

  }

  @Test
  public void test_predicate_constructors() throws Exception {

    IO<String> a = SwitchExp.<String, String>eval("a")
                            .match(x -> x.equals("a"),
                                   x -> Constants.A,
                                   x -> x.equals("b"),
                                   x -> Constants.B,
                                   x -> Constants.C
                            )
                            .map(String::toUpperCase);

    Assertions.assertEquals("A",
                            a.get()
                             .call());

    Assertions.assertNull(SwitchExp.<String, String>eval("c")
                                   .match(x -> x.equals("a"),
                                          x -> Constants.A,
                                          x -> x.equals("b"),
                                          x -> Constants.B
                                   )
                                   .get()
                                   .call());

    IO<String> b = SwitchExp.<String, String>eval("b")
                            .match(x -> x.equals("a"),
                                   x -> Constants.A,
                                   x -> x.equals("b"),
                                   x -> Constants.B,
                                   x -> x.equals("c"),
                                   x -> Constants.C,
                                   x -> Constants.C
                            )
                            .map(String::toUpperCase);

    Assertions.assertEquals("B",
                            b.get()
                             .call());

    IO<String> c = SwitchExp.<String, String>eval("c")
                            .match(x -> x.equals("a"),
                                   x -> Constants.A,
                                   x -> x.equals("b"),
                                   x -> Constants.B,
                                   x -> x.equals("c"),
                                   x -> Constants.C,
                                   x -> x.equals("d"),
                                   x -> Constants.D,
                                   x -> Constants.C
                            )
                            .map(String::toUpperCase);

    Assertions.assertEquals("C",
                            c.get()
                             .call());

    IO<String> d = SwitchExp.<String, String>eval("d")
                            .match(x -> x.equals("a"),
                                   x -> Constants.A,
                                   x -> x.equals("b"),
                                   x -> Constants.B,
                                   x -> x.equals("c"),
                                   x -> Constants.C,
                                   x -> x.equals("d"),
                                   x -> Constants.D,
                                   x -> x.equals("e"),
                                   x -> Constants.A,
                                   x -> Constants.C
                            )
                            .map(String::toUpperCase);

    Assertions.assertEquals("D",
                            d.get()
                             .call());

    IO<String> e = SwitchExp.<String, String>eval("e")
                            .match(x -> x.equals("a"),
                                   x -> Constants.A,
                                   x -> x.equals("b"),
                                   x -> Constants.B,
                                   x -> x.equals("c"),
                                   x -> Constants.C,
                                   x -> x.equals("d"),
                                   x -> Constants.D,
                                   x -> x.equals("e"),
                                   x -> Constants.A,
                                   x -> x.equals("f"),
                                   x -> Constants.B,
                                   x -> Constants.C
                            )
                            .map(String::toUpperCase);

    Assertions.assertEquals("A",
                            e.get()
                             .call());

    IO<String> f = SwitchExp.<String, String>eval("h")
                            .match(x -> x.equals("a"),
                                   x -> Constants.A,
                                   x -> x.equals("b"),
                                   x -> Constants.B,
                                   x -> x.equals("c"),
                                   x -> Constants.C,
                                   x -> x.equals("d"),
                                   x -> Constants.D,
                                   x -> x.equals("e"),
                                   x -> Constants.A,
                                   x -> x.equals("f"),
                                   x -> Constants.B,
                                   IO::succeed
                            )
                            .map(String::toUpperCase);

    Assertions.assertEquals("H",
                            f.get()
                             .call());

  }

  @Test
  public void test_debug_each() throws Exception {
    var exp = SwitchExp.<Integer, String>eval(IO.succeed(2))
                       .match(1,
                              i -> IO.succeed("one"),
                              2,
                              i -> IO.succeed("two"),
                              i -> IO.succeed("default")
                       )
                       .debugEach("context")
                       .get()
                       .call();

    Assertions.assertEquals("two",
                            exp
    );

  }
}
