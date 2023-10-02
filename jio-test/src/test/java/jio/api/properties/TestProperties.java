package jio.api.properties;

import fun.gen.IntGen;
import fun.gen.PairGen;
import jio.test.pbt.Property;
import jio.test.pbt.TestFailure;
import jio.test.pbt.TestResult;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.function.BiFunction;

public class TestProperties {


    @Test
    public void testMean() {
        BiFunction<Integer, Integer, Integer> fn = (a, b) -> (a + b) / 2;
        Property.ofFunction("medium",
                            PairGen.of(IntGen.arbitrary(0, Integer.MAX_VALUE),
                                       IntGen.arbitrary()
                                      )
                                   .suchThat(pair -> pair.first() <= pair.second()),
                            pair -> {
                                var a = pair.first();
                                var b = pair.second();
                                var mean = fn.apply(a, b);
                                if (mean < a) return TestFailure.reason("mean lower than a");
                                if (mean > b) return TestFailure.reason("mean greater than b");
                                return TestResult.SUCCESS;
                            }
                           )
                .withClassifiers(Map.of("both",
                                        p -> p.first() > Integer.MAX_VALUE / 2 && p.second() > Integer.MAX_VALUE / 2,
                                        "none",
                                        p -> p.first() < Integer.MAX_VALUE / 2 && p.second() < Integer.MAX_VALUE / 2),
                                 "one"
                                )
                .check()
                .result()
                .assertAllSuccess();

    }
}
