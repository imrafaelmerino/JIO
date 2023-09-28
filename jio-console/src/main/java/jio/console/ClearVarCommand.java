package jio.console;

import jio.IO;
import jio.Lambda;
import jio.RetryPolicies;
import jio.console.Programs.AskForInputParams;
import jsonvalues.JsObj;

import java.util.function.Function;
import java.util.stream.Collectors;

class ClearVarCommand extends Command {

    private static final String COMMAND_NAME = "var-clear";

    public ClearVarCommand() {
        super(COMMAND_NAME,
              """
                      Remove the specified variable.
                      Usage: $command {name}
                      Examples:
                          $command age
                          $command $var""".replace("$command", COMMAND_NAME)
             );
    }

    @Override
    public Function<String[], IO<String>> apply(final JsObj conf,
                                                final State state
                                               ) {
        Lambda<String, String> program = name -> IO.fromSupplier(() -> {
            state.stringVariables.remove(name);
            state.listsVariables.remove(name);
            state.mapVariables.remove(name);
            return "var removed!";
        });
        return tokens -> {
            int nTokens = tokens.length;

            if (nTokens == 1)
                return Programs.ASK_FOR_INPUT(new AskForInputParams("Type the name of the variable",
                                                                    name -> state.stringVariables.containsKey(name) ||
                                                                            state.listsVariables.containsKey(name) ||
                                                                            state.mapVariables.containsKey(name),
                                                                    "The variable doesn't exist",
                                                                    RetryPolicies.limitRetries(3)
                                              )
                                             )
                               .then(program);

            return program.apply(tokens[1]);


        };
    }

}
