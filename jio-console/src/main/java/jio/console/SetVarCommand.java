package jio.console;

import jio.IO;
import jsonvalues.JsObj;

import java.util.Arrays;
import java.util.function.Function;

class SetVarCommand extends Command {

    private static final String COMMAND_NAME = "var-set";

    public SetVarCommand() {
        super(COMMAND_NAME,
              """
                      Stores the specified value into the the specified variable.
                      set var value
                      Examples:
                          $command age 40
                          $command counter $var""".replace("$command", COMMAND_NAME)
             );
    }

    private static IO<String> setVarValue(State state,
                                          String varName,
                                          String newValue
                                         ) {

        return IO.lazy(() -> {
                                   String oldValue = state.variables.get(varName);
                                   state.variables.put(varName, newValue);
                                   return String.format("%s from %s to %s",
                                                        varName,
                                                        oldValue,
                                                        newValue
                                                       );
                               }
                              );
    }

    @Override
    public Function<String[], IO<String>> apply(final JsObj conf,
                                                final State state
                                               ) {
        return tokens -> {
            int nTokens = tokens.length;

            if (nTokens == 1)
                return Programs.ASK_FOR_PAIR(new Programs.AskForInputParams("Type the name of the variable"),
                                             new Programs.AskForInputParams("Type the value")
                                            )
                               .then(pair -> setVarValue(state, pair.first(), pair.second()));

            if (nTokens == 2)
                return Programs
                        .ASK_FOR_INPUT(new Programs.AskForInputParams("Type the value"))
                        .then(value -> setVarValue(state, tokens[1], value));

            return setVarValue(state,
                               tokens[1],
                               String.join(" ", Arrays.stream(tokens)
                                                      .toList()
                                                      .subList(2, tokens.length))
                              );


        };
    }
}
