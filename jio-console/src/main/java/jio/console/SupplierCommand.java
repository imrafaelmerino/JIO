package jio.console;

import jio.IO;
import jsonvalues.JsObj;

import java.util.function.Function;
import java.util.function.Supplier;

public class SupplierCommand extends Command {

    Supplier<String> supplier;

    public SupplierCommand(String name,
                           String description,
                           Supplier<String> supplier
                          ) {
        super(name, description);
        this.supplier = supplier;
    }

    @Override
    public Function<String[], IO<String>> apply(JsObj conf,
                                                State state
                                               ) {
        return tokens -> IO.fromSupplier(supplier);
    }
}
