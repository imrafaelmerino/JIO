package jio.api.properties;

import fun.gen.IntGen;
import fun.gen.PairGen;
import fun.tuple.Pair;
import jio.test.pbt.*;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;


public class TestProperties {

    static BiFunction<Integer, Integer, Integer> medium = (a, b) -> a + (b - a) / 2;
    @Command
    static Property<Pair<Integer, Integer>> mediumProperty =
            PropBuilder.of("medium",
                           PairGen.of(IntGen.biased(0),
                                              IntGen.biased(0)
                                             )
                                          .suchThat(pair -> pair.first() <= pair.second()),
                                   pair -> {
                                       var a = pair.first();
                                       var b = pair.second();
                                       var mean = medium.apply(a, b);
                                       if (mean < a)
                                           return TestFailure.reason("mean lower than a");
                                       if (mean > b)
                                           return TestFailure.reason("mean greater than b");
                                       return TestResult.SUCCESS;
                                   }
                          )

                       .withClassifiers(Map.of("both",
                                               p -> p.first() > Integer.MAX_VALUE / 2
                                                    && p.second() > Integer.MAX_VALUE / 2,
                                               "none",
                                               p -> p.first() < Integer.MAX_VALUE / 2
                                                    && p.second() < Integer.MAX_VALUE / 2
                                              ),
                                        "one"
                                       )
                       .build();

    public static void main(String[] args) throws IOException {
        new PropertyConsole(List.of(TestProperties.class)).start(args);
    }

    @Test
    public void testMean() {


        mediumProperty.check().assertAllSuccess();

    }

}
