package jio.cli;

import jio.IO;
import jio.Lambda;
import jio.RetryPolicies;
import jio.cli.ConsolePrograms.AskForInputParams;
import jsonvalues.JsObj;

import java.util.function.Function;

import static jio.cli.ConsolePrograms.ASK_FOR_INPUT;

final class ClearVarCommand extends Command {

    private static final String COMMAND_NAME = "var-clear";

    public ClearVarCommand() {
        super(COMMAND_NAME,
              """
                      Removes the specified variable from the current state.
                      Usage: $command {variable_name}
                      Examples:
                          $command (prompts for the name of the variable to remove)
                          $command age
                          $command $var""".replace("$command",
                                                   COMMAND_NAME)
             );
    }

    @Override
    public Function<String[], IO<String>> apply(final JsObj conf,
                                                final State state
                                               ) {
        Lambda<String, String> program = name -> IO.lazy(() -> {
            state.variables.remove(name);
            return "var removed!";
        });
        return tokens -> {
            int nTokens = tokens.length;

            if (nTokens == 1) {
                return ASK_FOR_INPUT(new AskForInputParams("Type the name of the variable",
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
