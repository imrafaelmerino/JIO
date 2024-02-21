package jio.api.exp;

import fun.gen.BoolGen;
import fun.gen.Combinators;
import java.time.Duration;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Stream;
import jio.IO;
import jio.IfElseExp;
import jio.Result.Success;
import jio.SwitchExp;
import jio.test.junit.Debugger;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

public class TestDebug {

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

    List<Success<String>> xs = Stream.of("a",
                                         "e",
                                         "i",
                                         "o",
                                         "u")
                                     .map(Success::new)
                                     .toList();
    List<Success<String>> ys = Stream.of("A",
                                         "E",
                                         "I",
                                         "O",
                                         "U")
                                     .map(Success::new)
                                     .toList();
    SwitchExp<String, String> match = SwitchExp.<String, String>eval(IfElseExp.<String>predicate(IO.lazy(isLowerCase))
                                                                              .consequence(() -> IO.lazy(loserCase))
                                                                              .alternative(() -> IO.lazy(upperCase))
    )
                                               .matchList(xs,
                                                          s -> IO.NULL(),
                                                          /*IO.succeed("%s %s".formatted(s.call(),
                                                                                        s.call()
                                                                                         .toUpperCase(Locale.ENGLISH))
                                                                     ),*/
                                                          ys,
                                                          s -> IO.NULL(),
                                                          /*       IO.succeed("%s %s".formatted(s.call(),
                                                                                               s.call().toLowerCase(Locale.ENGLISH))),*/
                                                          s -> IO.NULL()
                                               )
                                               .debugEach("context");

    System.out.println(match.call());

  }
}
