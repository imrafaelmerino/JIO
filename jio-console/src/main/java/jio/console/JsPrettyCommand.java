package jio.console;

import jio.IO;
import jsonvalues.JsObj;

import java.util.function.Function;

class JsPrettyCommand extends Command {

    private static final String COMMAND_NAME = "prettyjs";

    public JsPrettyCommand() {
        super(COMMAND_NAME,
              "Returns the json placed at the output variable in a pretty format"
             );
    }

    @Override
    public Function<String[], IO<String>> apply(final JsObj conf,
                                                final State state
                                               ) {
        return tokens -> IO.supply(() ->
                                           Functions.toJson
                                                   .apply(state.variables.get("output"))
                                                   .toPrettyString()
                                  );
    }
}
