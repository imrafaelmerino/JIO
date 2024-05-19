package jio.cli;

import java.util.function.Function;
import jio.IO;
import jsonvalues.JsObj;

class JsPrettyCommand extends Command {

  private static final String COMMAND_NAME = "json-pretty";

  public JsPrettyCommand() {
    super(COMMAND_NAME,
          """
                  Pretty-prints the JSON stored in the 'output' variable.
              
                  Usage:
                      json-pretty
              
                  Description:
                      The 'json-pretty' command formats the JSON data stored in the 'output' variable into a human-readable form.
                      The 'output' variable typically contains the result of the last executed command, if that command is
                      configured to store its result in 'output'.
              
                  Examples:
                      json-pretty  (formats and prints the JSON stored in the 'output' variable)
              
                  Notes:
                      - Ensure that the 'output' variable contains valid JSON before running this command.
                      - If the 'output' variable is empty or contains non-JSON data, the command will fail.
              """
         );
  }

  @Override
  public Function<String[], IO<String>> apply(final JsObj conf,
                                              final State state
                                             ) {
    return _ -> IO.lazy(() -> Functions.toJson
                            .apply(state.variables.get("output"))
                            .toPrettyString()
                       );
  }
}
