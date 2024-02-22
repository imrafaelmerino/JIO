package jio.console;

import java.util.function.Function;
import jio.IO;
import jio.RetryPolicies;
import jsonvalues.JsObj;
import jsonvalues.JsPath;
import jsonvalues.UserError;

class JsGetValueCommand extends Command {

  private static final String COMMAND_NAME = "json-get";

  public JsGetValueCommand() {
    super(COMMAND_NAME,
          """
              Returns the value of the specified path from the json placed at the output variable.
              jio.chatgpt.Examples:
                       $command /phones/0/number""".replace("$command",
                                                            COMMAND_NAME
                                                           )
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
        return Programs.ASK_FOR_INPUT(new Programs.AskForInputParams("Type the path of the value",
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
