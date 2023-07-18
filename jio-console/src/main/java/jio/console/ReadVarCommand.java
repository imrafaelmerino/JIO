package jio.console;

import jio.IO;
import jio.Lambda;
import jio.RetryPolicies;
import jio.console.Programs.AskForInputParams;
import jsonvalues.JsObj;

import java.util.function.Function;
import java.util.stream.Collectors;

class ReadVarCommand extends Command {

    private static final String COMMAND_NAME = "var-get";

    public ReadVarCommand() {
        super(COMMAND_NAME,
              """
                      Read the content of the specified variable.
                      var-get {name}
                      jio.chatgpt.Examples:
                          $command age
                          $command $var""".replace("$command", COMMAND_NAME)
             );
    }

    @Override
    public Function<String[], IO<String>> apply(final JsObj conf,
                                                final State state
                                               ) {
        Lambda<String, String> program = var -> IO.fromSupplier(() -> {
            var value = state.stringVariables.get(var);
            if (value != null) return value;
            var list = state.listsVariables.get(var);
            if (list != null) return String.join("\n", list);
            var map = state.mapVariables.get(var);
            if (map != null) return state.mapVariables.get(var).entrySet().stream()
                                                      .map(e -> String.format("%s -> %s",
                                                                              e.getKey(),
                                                                              e.getValue()
                                                                             )
                                                          )
                                                      .collect(Collectors.joining("\n"));
            return "";
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
