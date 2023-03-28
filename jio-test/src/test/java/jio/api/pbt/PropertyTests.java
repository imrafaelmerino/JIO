package jio.api.pbt;

import fun.gen.ListGen;
import fun.gen.StrGen;
import jio.pbt.*;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class PropertyTests {


    @Test
    public void test() {

        Function<List<String>, List<String>> reverse = l -> {
            List<String> result = new ArrayList<>();
            for (String s : l) result.add(0, s);
            return result;
        };

        Property<List<String>> prop =
                Property.<List<String>>ofFunction("property",
                                                  list -> reverse.apply(reverse.apply(list)).equals(list) ?
                                                          TestResult.SUCCESS :
                                                          TestFailure.reason("reverse(reverse(list)) != list")
                                                 )
                        .withDescription("description")
                        .times(1000)
                        .withGen(ListGen.biased(StrGen.alphabetic(0, 10), 10, 10));

        prop.repeatPar(2)
            .assertAllSuccess();

        Group.par("group",
                  List.of(prop,
                          prop
                         )
                 )
             .apply()
             .forEach(Report::assertAllSuccess);


    }
}
