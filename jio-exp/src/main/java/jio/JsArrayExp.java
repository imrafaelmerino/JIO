package jio;


import jsonvalues.JsArray;
import jsonvalues.JsValue;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.util.Objects.requireNonNull;

/**
 * Represents an immutable sequence of effects of type {@link JsValue}. It's possible to evaluate
 * all the effects either in parallel or sequentially. If one fails, the whole
 * expression fails.
 */
public abstract sealed class JsArrayExp extends Exp<JsArray> permits JsArrayExpPar, JsArrayExpSeq {


    final List<IO<? extends JsValue>> list;

    JsArrayExp(List<IO<? extends JsValue>> list,
               Function<ExpEvent, BiConsumer<JsArray, Throwable>> logger
              ) {
        super(logger);
        this.list = list;
    }

    /**
     * Creates a JsArray expression evaluating the effects sequentially, one after
     * the other. If one fails, the whole expression fails and the subsequent effects
     * are not evaluated.
     *
     * @param effects the effects
     * @return a JsArrayExp
     */
    @SafeVarargs
    public static JsArrayExp seq(final IO<? extends JsValue>... effects) {
        var list = new ArrayList<IO<? extends JsValue>>();
        for (var other : requireNonNull(effects)) list.add(requireNonNull(other));
        return new JsArrayExpSeq(list, null);
    }


    /**
     * Creates a JsArray expression evaluating all the effects in parallel.
     * If one fails, the whole expression fails.
     *
     * @param effects the effects
     * @return a JsArrayExp
     */
    @SafeVarargs
    public static JsArrayExp par(final IO<? extends JsValue>... effects) {
        var list = new ArrayList<IO<? extends JsValue>>();
        for (var other : requireNonNull(effects)) list.add(requireNonNull(other));
        return new JsArrayExpPar(list, null);
    }

    List<IO<? extends JsValue>> debugJsArray(List<IO<? extends JsValue>> exps,
                                             String context
                                            ) {
        return IntStream.range(0, exps.size())
                        .mapToObj(i -> LoggerHelper.debugIO(exps.get(i),
                                                            String.format("%s[%s]",
                                                                          this.getClass().getSimpleName(),
                                                                          i
                                                                         ),
                                                            context

                                                           )
                                 )
                        .collect(Collectors.toList());
    }

    /**
     * Creates a new JsArrayExp expression where the given retry policy is applied recursively
     * to every subexpression when an exception is tested true against the specified predicate.
     *
     * @param predicate the predicate to test exceptions
     * @param policy    the retry policy
     * @return a new JsArrayExp
     */
    @Override
    public abstract JsArrayExp retryEach(final Predicate<Throwable> predicate,
                                         final RetryPolicy policy
                                        );

    /**
     * Creates a new JsArrayExp that will write to the given logger information about every
     * computation evaluated to reduce this expression (like {@link #debugEach(String)} does).
     * A final log message created with the specified messageBuilder is written after reducing
     * the whole expression
     *
     * @param messageBuilder the builder to create the log message from the result of the expression
     * @return a new JsArrayExp
     * @see #debugEach(String) debugEach
     */
    @Override
    public abstract JsArrayExp debugEach(final EventBuilder<JsArray> messageBuilder
                                        );

    /**
     * Creates a new JsArrayExp that will print out on the console information about every
     * computation evaluated to reduce this expression. The given context will be associated
     * to every subexpression and printed out to correlate all the evaluations (contextual
     * logging).
     * <p>
     * The line format is the following:
     * <p>
     * datetime thread logger [context] elapsed_time success|exception expression|subexpression result?
     * <p>
     * Find bellow an example:
     *
     * <pre>
     * {@code
     *
     *         JsArrayExp.seq(IO.succeed("a").map(JsStr::of),
     *                        IO.succeed("b").map(JsStr::of)
     *                       )
     *                    .debugEach("context")
     *                    .join()
     *
     *
     * }
     * </pre>
     * 2023-02-04T17:47:01.326156+01:00 pool-1-thread-1 DEBUGGER [context] 1028241042 success JsArrayExpSeq[0]
     * 2023-02-04T17:47:02.345274+01:00 pool-2-thread-1 DEBUGGER [context] 1006703875 success JsArrayExpSeq[1]
     * 2023-02-04T17:47:02.350689+01:00 pool-2-thread-1 DEBUGGER [context] 2095418584 success JsArrayExpSeq ["a","b"]
     *
     * @param context the context shared by all the subexpressions that will be printed out
     * @return a new JsArrayExp
     */
    @Override
    public abstract JsArrayExp debugEach(final String context);

    /**
     * Creates a new JsArrayExp expression where the given retry policy is applied recursively
     * to every subexpression when an exception happens.
     *
     * @param policy the retry policy
     * @return a new JsArrayExp
     */
    @Override
    public JsArrayExp retryEach(RetryPolicy policy) {
        return retryEach(e -> true, policy);
    }


}
