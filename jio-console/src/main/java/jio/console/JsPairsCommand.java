package jio.console;

import java.util.function.Function;
import java.util.stream.Collectors;
import jio.IO;
import jsonvalues.JsObj;

class JsPairsCommand extends Command {

  private static final String COMMAND_NAME = "json-pairs";

  public JsPairsCommand() {
    super(COMMAND_NAME,
          """
              Returns the list of the path/value pairs of the json placed at the output variable.
              It's possible to filter out the list of pairs passing in a substring
              Examples:
                  $command
                  $command email""".replace("$command",
                                            COMMAND_NAME)
         );
  }

  @Override
  public Function<String[], IO<String>> apply(final JsObj conf,
                                              final State state
                                             ) {

    return tokens -> {
      int nArgs = tokens.length - 1;

      return nArgs > 0 ? IO.lazy(
          () -> Functions.toJson.apply(state.variables.get("output"))
                                .stream()
                                .filter(it ->
                                        {
                                          String search = Functions.joinTail(tokens);
                                          return it.path()
                                                   .toString()
                                                   .contains(search) || it.value().toString().contains(search);
                                        })
                                .map(it -> String.format("%s -> %s",
                                                         it.path(),
                                                         it.value()
                                                        ))
                                .collect(Collectors.joining("\n"))
                                ) : IO.lazy(
          () -> Functions.toJson.apply(state.variables.get("output"))
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
