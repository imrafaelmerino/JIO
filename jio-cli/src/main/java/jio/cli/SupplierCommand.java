package jio.cli;

import java.util.function.Function;
import java.util.function.Supplier;
import jio.IO;
import jsonvalues.JsObj;

public class SupplierCommand extends Command {

  final Supplier<String> supplier;

  public SupplierCommand(String name,
                         String description,
                         Supplier<String> supplier
                        ) {
    super(name,
          description);
    this.supplier = supplier;
  }

  @Override
  public Function<String[], IO<String>> apply(JsObj conf,
                                              State state
                                             ) {
    return _ -> IO.lazy(supplier);
  }
}
