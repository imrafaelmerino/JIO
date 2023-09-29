package jio.console;

import jio.IO;
import jio.Lambda;
import jio.RetryPolicies;
import jio.console.Programs.AskForInputParams;
import jsonvalues.JsObj;

import java.util.function.Function;

/**
 * Command to read the content of a specified variable with the command:
 * <pre>
 *     var-get {name}
 * </pre>
 * <p>
 * Users can specify the name of the variable they want to read, and the command will return the variable's content as a string.
 * If the variable doesn't exist, the command allows for multiple retries.
 * <p>
 * Examples:
 * <pre>
 *     var-get age
 *     var-get $var
 * </pre>
 *
 * @see Command
 */
class ReadVarCommand extends Command {

    private static final String COMMAND_NAME = "var-get";

    public ReadVarCommand() {
        super(COMMAND_NAME,
              """
                      Read the content of the specified variable.
                      var-get {name}
                      Examples:
                          $command age
                          $command $var""".replace("$command", COMMAND_NAME)
             );
    }

    @Override
    public Function<String[], IO<String>> apply(final JsObj conf,
                                                final State state
                                               ) {
        Lambda<String, String> program = var -> IO.lazy(() -> {
            var value = state.variables.get(var);
            if (value != null) return value;
            var list = state.listsVariables.get(var);
            if (list != null) return String.join("\n", list);
            return "";
        });

        return tokens -> {
            int nTokens = tokens.length;

            if (nTokens == 1)
                return Programs.ASK_FOR_INPUT(new AskForInputParams("Type the name of the variable",
                                                                    name -> state.variables.containsKey(name) ||
                                                                            state.listsVariables.containsKey(name),
                                                                    "The variable doesn't exist",
                                                                    RetryPolicies.limitRetries(3)
                                              )
                                             )
                               .then(program);

            return program.apply(tokens[1]);


        };
    }

}
