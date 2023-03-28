package jio;


import java.util.List;
import java.util.function.Supplier;
import java.util.stream.IntStream;

final class LoggerHelper {


    static <O> Supplier<IO<O>> debugSupplier(final Supplier<IO<O>> supplier,
                                             final String expName,
                                             final String context
                                            ) {
        return () -> debugIO(supplier.get(),
                             expName,
                             context
                            );

    }

    static <I, O> List<Lambda<I, O>> debugLambdas(List<Lambda<I, O>> lambdas,
                                                  String expName,
                                                  String context
                                                 ) {
        return IntStream.range(0, lambdas.size())
                        .mapToObj(i -> debugLambda(lambdas.get(i),
                                                   String.format("%s[%s]",
                                                                 expName, i
                                                                ),
                                                   context
                                                  ))
                        .toList();
    }

    static <I, O> Lambda<I, O> debugLambda(Lambda<I, O> lambda,
                                           String expName,
                                           String context
                                          ) {
        return lambda
                .map(it -> debugIO(it,
                                   expName,
                                   context
                                  )
                    );
    }


    static <O> List<Supplier<IO<O>>> debugSuppliers(List<Supplier<IO<O>>> suppliers,
                                                    String expName,
                                                    String context
                                                   ) {
        return IntStream.range(0, suppliers.size())
                        .mapToObj(i -> debugSupplier(suppliers.get(i),
                                                     String.format("%s[%s]",
                                                                   expName,
                                                                   i
                                                                  ),
                                                     context
                                                    )
                                 )
                        .toList();
    }

    static List<IO<Boolean>> debugConditions(final List<IO<Boolean>> exps,
                                             final String expName,
                                             final String context
                                            ) {
        return IntStream.range(0, exps.size())
                        .mapToObj(i -> debugIO(exps.get(i),
                                               expName + "[" + i + "]",
                                               context
                                              ))
                        .toList();
    }

    static <O> IO<O> debugIO(final IO<O> io,
                             final String expName,
                             final String context
                            ) {
        return
                debugExp(io,
                         EventBuilder.<O>ofExp(expName)
                                     .setContext(context)
                        );
    }


    static <O> IO<O> debugExp(IO<O> o,
                              EventBuilder<O> builder
                             ) {
        return switch (o) {
            case Exp<O> exp -> exp.debugEach(builder);
            case IO<O> val -> val.debug(builder);
        };
    }

    static <O> List<IO<O>> debugList(List<IO<O>> list,
                                     String expName,
                                     String context
                                    ) {
        return IntStream.range(0, list.size())
                        .mapToObj(i -> debugIO(list.get(i),
                                               String.format("%s[%s]",
                                                             expName,
                                                             i
                                                            ),
                                               context
                                              )
                                 )
                        .toList();
    }
}
