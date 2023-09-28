package jio.test.pbt;

import fun.gen.Gen;
import jio.IO;
import jio.Lambda;
import jio.ListExp;
import jsonvalues.JsObj;

import java.util.function.Function;
import java.util.function.Supplier;

import static java.util.Objects.requireNonNull;

/**
 * Represents a property of a piece of code or program that should always
 * be held and never fails. It's modeled with a supplier that returns
 * a JIO effect that generates pseudorandom data to feed a
 * test executed the specified number of times with {@link #times(int)} ({@link #DEFAULT_TESTS} by default) and
 * generates a {@link Report}.
 * <p>
 * The tests will be executed by a thread from the common FromJoinPool since
 * the JIO effect is created with the {@link IO#managedLazy(Supplier)} constructor.
 * <p>
 * A generator has to be specified with the method {@link #withGen(Gen)}, otherwise
 * the value null is always generated
 * <p>
 * Properties can be created with the static factory methods {@link #ofFunction(String, Function)} and
 * {@link #ofLambda(String, Lambda)}
 * <p>
 *
 * @param <O> the type of the data generated to feed the property tests
 */
public class ParProperty<O> implements Testable {

    int n;

    Property<O> prop;

    ParProperty(int n, Property<O> prop) {
        this.n = n;
        this.prop = prop;
    }

    @Override
    public IO<Report> check(JsObj conf) {
        if (n < 1) throw new IllegalArgumentException("n < 1");
        final IO<Report> test = prop.check(conf);
        var result = ListExp.par(test);
        for (int i = 1; i < n; i++) result = result.append(test);
        return result.map(it -> it.stream().reduce(Report::aggregatePar).get());
    }
}
