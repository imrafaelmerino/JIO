package jio.cli;

import java.util.function.Function;
import jio.IO;
import jio.RetryPolicies;
import jio.cli.ConsolePrograms.AskForInputParams;
import jsonvalues.JsObj;
import jsonvalues.JsPath;
import jsonvalues.UserError;

final class JsGetValueCommand extends Command {

  private static final String COMMAND_NAME = "json-get";

  public JsGetValueCommand() {
    super(COMMAND_NAME,
          """
                  Retrieves the value at the specified path from the JSON stored in the 'output' variable.
              
                  Usage:
                      json-get [path]
              
                  Parameters:
                      [path]  - The path to retrieve the value from the JSON.
                                Must start with '/', indicating the root of the JSON.
              
                  Examples:
                      json-get /phones/0/number    (retrieves the value at the specified path)
              
                  Description:
                      The 'json-get' command fetches the value at the specified path from the JSON stored in the 'output' variable.
                      Users need to provide the path starting from '/', indicating the root of the JSON structure.
              
                  Notes:
                      - Ensure that the 'output' variable contains valid JSON data before using this command.
                      - If the provided path is invalid or does not exist in the JSON, the command will fail.
              """
         );
  }

  private static IO<String> getValue(State state,
                                     String path) {
    JsPath jsPath = JsPath.path(path);
    return IO.lazy(() -> Functions.toJson
        .apply(state.variables.get("output"))
        .get(jsPath)
        .toString());
  }

  @Override
  public Function<String[], IO<String>> apply(final JsObj conf,
                                              final State state
                                             ) {
    return tokens -> {
      int nArgs = tokens.length - 1;
      if (nArgs == 0) {
        return ConsolePrograms.ASK_FOR_INPUT(new AskForInputParams("Type the path of the output",
                                                                     path -> {
                                                                       try {
                                                                         JsPath.path(path);
                                                                         return true;
                                                                       } catch (UserError e) {
                                                                         return false;
                                                                       }
                                                                     },
                                                                   "Type a valid path (starting with /)",
                                                                   RetryPolicies.limitRetries(3)
                                      )
                                            )
                              .then(path -> getValue(state,
                                              path));
      }
      return getValue(state,
                      Functions.joinTail(tokens));
    };
  }
}
