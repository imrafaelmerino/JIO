package jio.console;

import java.util.function.Function;
import jio.IO;
import jsonvalues.JsObj;

class JsPrettyCommand extends Command {

  private static final String COMMAND_NAME = "json-pretty";

  public JsPrettyCommand() {
    super(COMMAND_NAME,
          "Returns the json placed at the output variable in a pretty format"
         );
  }

  @Override
  public Function<String[], IO<String>> apply(final JsObj conf,
                                              final State state
                                             ) {
    return tokens -> IO.lazy(() -> Functions.toJson
                                 .apply(state.variables.get("output"))
                                 .toPrettyString()
                            );
  }
}
