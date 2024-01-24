package jio.api.exp;

import fun.gen.BoolGen;
import fun.gen.Combinators;
import jio.IO;
import jio.IfElseExp;
import jio.SwitchExp;
import jio.test.junit.Debugger;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.time.Duration;
import java.util.List;
import java.util.function.Supplier;

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

    SwitchExp<String, String> match =
        SwitchExp.<String, String>eval(IfElseExp.<String>predicate(IO.lazy(isLowerCase))
                                                .consequence(() -> IO.lazy(loserCase))
                                                .alternative(() -> IO.lazy(upperCase))
                                      )
                 .match(List.of("a",
                                "e",
                                "i",
                                "o",
                                "u"),
                        s -> IO.succeed("%s %s".formatted(s,
                                                          s.toUpperCase())),
                        List.of("A",
                                "E",
                                "I",
                                "O",
                                "U"),
                        s -> IO.succeed("%s %s".formatted(s,
                                                          s.toLowerCase())),
                        s -> IO.NULL()
                       )
                 .debugEach("context");

    System.out.println(match.result());

  }
}
