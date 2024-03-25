package jio.api.properties;

import fun.gen.Gen;
import fun.gen.IntGen;
import fun.gen.PairGen;
import fun.tuple.Pair;
import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import jio.Lambda;
import jio.Result.Failure;
import jio.Result.Success;
import jio.test.pbt.Command;
import jio.test.pbt.Property;
import jio.test.pbt.PropertyBuilder;
import jio.test.pbt.PropertyConsole;
import jio.test.pbt.TestFailure;
import jio.test.pbt.TestResult;
import jio.test.stub.StubBuilder;
import org.junit.jupiter.api.Test;

public class ExampleProperties {

  static BiFunction<Integer, Integer, Integer> medium = (a,
                                                         b) -> (a + b) >>> 1;
  @Command
  static Property<Pair<Integer, Integer>> mediumProperty =
      PropertyBuilder.of("medium",
                         PairGen.of(IntGen.biased(0),
                                    IntGen.biased(0)
                                   )
                                .suchThat(pair -> pair.first()
                                                  <= pair.second()),
                         pair -> {
                           var a = pair.first();
                           var b = pair.second();
                           var mean = medium.apply(a,
                                                   b);
                           if (mean < a) {
                             return TestFailure.reason("mean lower than a");
                           }
                           if (mean > b) {
                             return TestFailure.reason("mean greater than b");
                           }
                           return TestResult.SUCCESS;
                         }
                        )
                     .withClassifiers(Map.of("both",
                                             p -> p.first()
                                                  >
                                                  Integer.MAX_VALUE
                                                  / 2
                                                  && p.second()
                                                     >
                                                     Integer.MAX_VALUE
                                                     / 2,
                                             "none",
                                             p -> p.first()
                                                  <
                                                  Integer.MAX_VALUE
                                                  / 2
                                                  && p.second()
                                                     <
                                                     Integer.MAX_VALUE
                                                     / 2
                                            ),
                                      "one"
                                     )

                     .get();

  public static void main(String[] args) throws IOException {
    new PropertyConsole(List.of(ExampleProperties.class)).start(args);
  }

  @Test
  public void testMean() throws Exception {

    //let's change the delay of every stub to 1 sec, for the sake of clarity
    Gen<Duration> delayGen = Gen.cons(1)
                                .map(Duration::ofSeconds);

    Lambda<Void, Integer> unused = _ ->
        StubBuilder.ofGen(Gen.seq(n -> n <= 4 ? new Failure<>(new RuntimeException(STR."\{n}"))
                                              : new Success<>(n)
                                 )
                         )
                   .withDelays(delayGen)
                   .get();

    mediumProperty.repeatPar(3).check()
                  .assertAllSuccess();

  }

}
