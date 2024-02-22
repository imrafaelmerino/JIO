package jio;

import java.util.List;
import java.util.function.Supplier;
import java.util.stream.IntStream;

final class DebuggerHelper {

  static <Output> Supplier<IO<Output>> debugSupplier(final Supplier<IO<Output>> supplier,
                                                     final String expName,
                                                     final String context
                                                    ) {
    return () -> debugIO(supplier.get(),
                         expName,
                         context
                        );

  }

  static <Input, Output> List<Lambda<Input, Output>> debugLambdas(List<Lambda<Input, Output>> lambdas,
                                                                  String expName,
                                                                  String context
                                                                 ) {
    return IntStream.range(0,
                           lambdas.size())
                    .mapToObj(i -> debugLambda(lambdas.get(i),
                                               String.format("%s[%s]",
                                                             expName,
                                                             i
                                                            ),
                                               context
                                              ))
                    .toList();
  }

  static <Input, Output> Lambda<Input, Output> debugLambda(Lambda<Input, Output> lambda,
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

  static <Output> List<Supplier<IO<Output>>> debugSuppliers(List<Supplier<IO<Output>>> suppliers,
                                                            String expName,
                                                            String context
                                                           ) {
    return IntStream.range(0,
                           suppliers.size())
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
                                           final EventBuilder<Boolean> eventBuilder
                                          ) {
    return IntStream.range(0,
                           exps.size())
                    .mapToObj(i -> debugIO(exps.get(i),
                                           "%s[%d]".formatted(eventBuilder.exp,
                                                              i),
                                           eventBuilder.context
                                          ))
                    .toList();
  }

  static <Output> IO<Output> debugIO(final IO<Output> io,
                                     final String expName,
                                     final String context
                                    ) {
    return debugExp(io,
                    EventBuilder.of(expName,
                                    context));
  }

  static <Output> IO<Output> debugExp(IO<Output> o,
                                      EventBuilder<Output> builder
                                     ) {
    return o instanceof Exp<Output> exp ? exp.debugEach(builder) : o.debug(builder);

  }

  static <Output> List<IO<Output>> debugList(List<IO<Output>> list,
                                             String expName,
                                             String context
                                            ) {
    return IntStream.range(0,
                           list.size())
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
