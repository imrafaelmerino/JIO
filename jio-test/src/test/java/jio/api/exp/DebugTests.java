package jio.api.exp;

import fun.gen.BoolGen;
import fun.gen.Combinators;
import java.time.Duration;
import java.util.List;
import java.util.Locale;
import java.util.function.Supplier;
import java.util.stream.Stream;
import jio.EventBuilder;
import jio.IO;
import jio.IfElseExp;
import jio.Result;
import jio.SwitchExp;
import jio.test.junit.Debugger;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

public class DebugTests {

  @RegisterExtension
  static Debugger debugger = Debugger.of(Duration.ofSeconds(2));

  @Test
  public void test() {

    Supplier<Boolean> isLowerCase = BoolGen.arbitrary()
                                           .sample();
    Supplier<String> loserCase = Combinators.oneOf("a",
                                                   "e",
                                                   "i",
                                                   "o",
                                                   "u")
                                            .sample();
    Supplier<String> upperCase = Combinators.oneOf("A",
                                                   "E",
                                                   "I",
                                                   "O",
                                                   "U")
                                            .sample();

    List<String> xs = Stream.of("a",
                                "e",
                                "i",
                                "o",
                                "u")
                            .toList();
    List<String> ys = Stream.of("A",
                                "E",
                                "I",
                                "O",
                                "U")
                            .toList();
    SwitchExp<String, String> match =
        SwitchExp.<String, String>eval(IfElseExp.<String>predicate(IO.lazy(isLowerCase))
                                                .consequence(() -> IO.lazy(loserCase))
                                                .alternative(() -> IO.lazy(upperCase))
                                      )
                 .matchList(xs,
                            s ->
                                IO.succeed("%s %s".formatted(s,
                                                             s.toUpperCase(Locale.ENGLISH))
                                          ),
                            ys,
                            s -> IO.succeed("%s %s".formatted(s,
                                                              s.toLowerCase(Locale.ENGLISH))
                                           ),
                            s -> IO.NULL()
                           )
                 .debugEach("context");

    System.out.println(match.call());

  }

  @Test
  public void testMain(){
    Supplier<Boolean> isLowerCase = BoolGen.arbitrary().sample();
    Supplier<String> lowerCase = Combinators.oneOf("a", "e", "i", "o", "u").sample();
    Supplier<String> upperCase = Combinators.oneOf("A", "E", "I", "O", "U").sample();

    SwitchExp<String, String> letters =
        SwitchExp.<String, String>eval(IfElseExp.<String>predicate(IO.lazy(isLowerCase))
                                                .consequence(() -> IO.lazy(lowerCase))
                                                .alternative(() -> IO.lazy(upperCase))
                                      )
                 .matchList(List.of("a", "e", "i", "o", "u"),
                            letter -> IO.succeed("%s %s".formatted(letter, letter.toUpperCase(Locale.ENGLISH))),
                            List.of("A", "E", "I", "O", "U"),
                            letter -> IO.succeed("%s %s".formatted(letter, letter.toLowerCase(Locale.ENGLISH)))
                           )
                 .debugEach("context");

    Assertions.assertTrue(letters.compute().isSuccess());


  }
}
