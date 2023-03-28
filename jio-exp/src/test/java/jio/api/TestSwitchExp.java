package jio.api;

import jio.IO;
import jio.SwitchExp;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

public class TestSwitchExp {

    @Test
    public void test_object_constructors() {


        IO<String> a =
                SwitchExp.<String, String>eval(IO.succeed("a"))
                         .match("a", x -> Constants.A,
                                "b", x -> Constants.B,
                                x -> Constants.C
                               )
                         .debugEach("1")
                         .map(String::toUpperCase);

        Assertions.assertEquals("A", a.join());


        IO<String> b =
                SwitchExp.<String, String>eval("b")
                         .match("a", x -> Constants.A,
                                "b", x -> Constants.B,
                                "c", x -> Constants.C,
                                x -> Constants.C
                               )
                         .debugEach("2")
                         .map(String::toUpperCase);

        Assertions.assertEquals("B", b.join());

        IO<String> c =
                SwitchExp.<String, String>eval("c")
                         .match("a", x -> Constants.A,
                                "b", x -> Constants.B,
                                "c", x -> Constants.C,
                                "d", x -> Constants.D,
                                x -> Constants.C
                               )
                         .debugEach("3")
                         .map(String::toUpperCase);

        Assertions.assertEquals("C", c.join());

        IO<String> d =
                SwitchExp.<String, String>eval("d")
                         .match("a", x -> Constants.A,
                                "b", x -> Constants.B,
                                "c", x -> Constants.C,
                                "d", x -> Constants.D,
                                "e", x -> Constants.A,
                                x -> Constants.C
                               )
                         .debugEach("4")
                         .map(String::toUpperCase);

        Assertions.assertEquals("D", d.join());


        SwitchExp<String, String> patterns = SwitchExp.<String, String>eval("e")
                                                      .match("a", x -> Constants.A,
                                                             "b", x -> Constants.B,
                                                             "c", x -> Constants.C,
                                                             "d", x -> Constants.D,
                                                             "e", x -> Constants.A,
                                                             "f", x -> Constants.B,
                                                             x -> Constants.C
                                                            );
        IO<String> e =
                patterns.debugEach("5")
                        .map(String::toUpperCase);

        Assertions.assertEquals("A", e.join());

        IO<String> f =
                SwitchExp.<String, String>eval("h")
                         .match("a", x -> Constants.A,
                                "b", x -> Constants.B,
                                "c", x -> Constants.C,
                                "d", x -> Constants.D,
                                "e", x -> Constants.A,
                                "f", x -> Constants.B,
                                IO::succeed
                               )
                         .debugEach("6")
                         .map(String::toUpperCase);

        Assertions.assertEquals("H", f.join());

    }

    @Test
    public void test_list_constructors() {

        IO<String> a =
                SwitchExp.<String, String>eval("a")
                         .match(List.of("a", "c"), x -> Constants.A,
                                List.of("b"), x -> Constants.B,
                                x -> Constants.C
                               )
                         .map(String::toUpperCase);

        Assertions.assertEquals("A", a.join());


        IO<String> b =
                SwitchExp.<String, String>eval("b")
                         .match(List.of("a"), x -> Constants.A,
                                List.of("b"), x -> Constants.B,
                                List.of("c"), x -> Constants.C,
                                x -> Constants.C
                               )
                         .map(String::toUpperCase);

        Assertions.assertEquals("B", b.join());

        IO<String> c =
                SwitchExp.<String, String>eval("c")
                         .match(List.of("a"), x -> Constants.A,
                                List.of("b"), x -> Constants.B,
                                List.of("c"), x -> Constants.C,
                                List.of("d"), x -> Constants.D,
                                x -> Constants.C
                               )
                         .map(String::toUpperCase);

        Assertions.assertEquals("C", c.join());

        IO<String> d =
                SwitchExp.<String, String>eval("d")
                         .match(List.of("a"), x -> Constants.A,
                                List.of("b"), x -> Constants.B,
                                List.of("c"), x -> Constants.C,
                                List.of("d"), x -> Constants.D,
                                List.of("e"), x -> Constants.A,
                                x -> Constants.C
                               )
                         .map(String::toUpperCase);

        Assertions.assertEquals("D", d.join());


        IO<String> e =
                SwitchExp.<String, String>eval("e")
                         .match(List.of("a", "1"), x -> Constants.A,
                                List.of("b", "2"), x -> Constants.B,
                                List.of("c"), x -> Constants.C,
                                List.of("d"), x -> Constants.D,
                                List.of("e", "j"), x -> Constants.A,
                                List.of("f", "g"), x -> Constants.B,
                                x -> Constants.C
                               )
                         .map(String::toUpperCase);

        Assertions.assertEquals("A", e.join());

        IO<String> f =
                SwitchExp.<String, String>eval("h")
                         .match(List.of("a"), x -> Constants.A,
                                List.of("b"), x -> Constants.B,
                                List.of("c"), x -> Constants.C,
                                List.of("d"), x -> Constants.D,
                                List.of("e"), x -> Constants.A,
                                List.of("f"), x -> Constants.B,
                                IO::succeed
                               )
                         .map(String::toUpperCase);

        Assertions.assertEquals("H", f.join());

    }

    @Test
    public void test_predicate_constructors() {

        IO<String> a =
                SwitchExp.<String, String>eval("a")
                         .match(x -> x.equals("a"), x -> Constants.A,
                                x -> x.equals("b"), x -> Constants.B,
                                x -> Constants.C
                               )
                         .map(String::toUpperCase);

        Assertions.assertEquals("A", a.join());


        IO<String> b =
                SwitchExp.<String, String>eval("b")
                         .match(x -> x.equals("a"), x -> Constants.A,
                                x -> x.equals("b"), x -> Constants.B,
                                x -> x.equals("c"), x -> Constants.C,
                                x -> Constants.C
                               )
                         .map(String::toUpperCase);

        Assertions.assertEquals("B", b.join());

        IO<String> c =
                SwitchExp.<String, String>eval("c")
                         .match(x -> x.equals("a"), x -> Constants.A,
                                x -> x.equals("b"), x -> Constants.B,
                                x -> x.equals("c"), x -> Constants.C,
                                x -> x.equals("d"), x -> Constants.D,
                                x -> Constants.C
                               )
                         .map(String::toUpperCase);

        Assertions.assertEquals("C", c.join());

        IO<String> d =
                SwitchExp.<String, String>eval("d")
                         .match(x -> x.equals("a"), x -> Constants.A,
                                x -> x.equals("b"), x -> Constants.B,
                                x -> x.equals("c"), x -> Constants.C,
                                x -> x.equals("d"), x -> Constants.D,
                                x -> x.equals("e"), x -> Constants.A,
                                x -> Constants.C
                               )
                         .map(String::toUpperCase);

        Assertions.assertEquals("D", d.join());


        IO<String> e =
                SwitchExp.<String, String>eval("e")
                         .match(x -> x.equals("a"), x -> Constants.A,
                                x -> x.equals("b"), x -> Constants.B,
                                x -> x.equals("c"), x -> Constants.C,
                                x -> x.equals("d"), x -> Constants.D,
                                x -> x.equals("e"), x -> Constants.A,
                                x -> x.equals("f"), x -> Constants.B,
                                x -> Constants.C
                               )
                         .map(String::toUpperCase);

        Assertions.assertEquals("A", e.join());

        IO<String> f =
                SwitchExp.<String, String>eval("h")
                         .match(x -> x.equals("a"), x -> Constants.A,
                                x -> x.equals("b"), x -> Constants.B,
                                x -> x.equals("c"), x -> Constants.C,
                                x -> x.equals("d"), x -> Constants.D,
                                x -> x.equals("e"), x -> Constants.A,
                                x -> x.equals("f"), x -> Constants.B,
                                x -> IO.succeed(x)
                               )
                         .map(String::toUpperCase);

        Assertions.assertEquals("H", f.join());




    }

    @Test
    public void test_debug_each() {
        var exp = SwitchExp.<Integer, String>eval(IO.succeed(2))
                           .match(1, i -> IO.succeed("one"),
                                  2, i -> IO.succeed("two"),
                                  i -> IO.succeed("default")
                                 )
                           .debugEach("context")
                           .join();

        Assertions.assertEquals("two",
                                exp
                               );

    }
}
