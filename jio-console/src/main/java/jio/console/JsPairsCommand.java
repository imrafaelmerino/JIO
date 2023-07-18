package jio.console;

import jio.IO;
import jsonvalues.JsObj;

import java.util.function.Function;
import java.util.stream.Collectors;

class JsPairsCommand extends Command {

    private static final String COMMAND_NAME = "json-pairs";

    public JsPairsCommand() {
        super(COMMAND_NAME,
              """
                 Returns the list of the path/value pairs of the json placed at the output variable.
                 It's possible to filter out the list of pairs passing in a substring
                 jio.chatgpt.Examples:
                     $command
                     $command email""".replace("$command", COMMAND_NAME)
             );
    }

    @Override
    public Function<String[], IO<String>> apply(final JsObj conf,
                                                final State state
                                               ) {

        return tokens -> {
            int nArgs = tokens.length - 1;

            return nArgs > 0 ?
                    IO.fromSupplier(
                            () -> Functions.toJson.apply(state.stringVariables.get("output"))
                                                  .stream()
                                                  .filter(it -> it.path()
                                                                  .toString()
                                                                  .contains(Functions.joinTail(tokens)))
                                                  .map(it -> String.format("%s -> %s",
                                                                           it.path(),
                                                                           it.value()
                                                                          ))
                                                  .collect(Collectors.joining("\n"))
                                   ) :
                    IO.fromSupplier(
                            () -> Functions.toJson.apply(state.stringVariables.get("output"))
                                                  .stream()
                                                  .map(it -> String.format("%s -> %s",
                                                                           it.path(),
                                                                           it.value()
                                                                          )
                                                      )
                                                  .collect(Collectors.joining("\n"))
                                   );
        };
    }
}

