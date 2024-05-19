package jio.cli;

import java.util.function.Function;
import jio.IO;
import jio.Lambda;
import jio.RetryPolicies;
import jio.cli.ConsolePrograms.AskForInputParams;
import jsonvalues.JsObj;

/**
 * Command to read the content of a specified variable with the command:
 * <pre>
 * var-get {name}
 * </pre>
 * <p>
 * Users can specify the name of the variable they want to read, and the command will return the variable's content as a
 * string. If the variable doesn't exist, the command allows for multiple retries.
 * <p>
 * Examples:
 * <pre>
 * var-get age
 * var-get $var
 * </pre>
 *
 * @see Command
 */
final class ReadVarCommand extends Command {

  private static final String COMMAND_NAME = "var-get";

  public ReadVarCommand() {
    super(COMMAND_NAME,
          """
                  Reads the content of the specified variable.
              
                  Usage:
                      var-get <variable_name>
              
                  Parameters:
                      <variable_name>  - The name of the variable to read.
              
                  Examples:
                      var-get           (will ask you for the name of the variable interactively)
                      var-get age       (retrieves the value of the 'age' variable)
                      var-get $var      (retrieves the value of the 'var' variable)
              
                  Description:
                      The 'var-get' command reads and returns the value of the specified variable. If the variable
                      does not exist, it will prompt the user to try again up to three times.
              
                  Notes:
                      - If the variable name is not provided as an argument, you will be prompted to enter it interactively.
                      - If the variable does not exist, an appropriate message will be displayed.
              """
         );
  }

  @Override
  public Function<String[], IO<String>> apply(final JsObj conf,
                                              final State state
                                             ) {
    Lambda<String, String> program = var -> IO.lazy(() -> {
      var value = state.variables.get(var);
      if (value != null) {
        return value;
      }
      return "";
    });

    return tokens -> {
      int nTokens = tokens.length;

      if (nTokens == 1) {
        return ConsolePrograms.ASK_FOR_INPUT(new AskForInputParams("Type the name of the variable",
                                                                   state.variables::containsKey,
                                                                   "The variable doesn't exist",
                                                                   RetryPolicies.limitRetries(3)
                                             )
                                            )
                              .then(program);
      }

      return program.apply(tokens[1]);

    };
  }

}
