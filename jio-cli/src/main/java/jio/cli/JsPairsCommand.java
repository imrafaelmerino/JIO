package jio.cli;

import java.util.function.Function;
import java.util.stream.Collectors;
import jio.IO;
import jsonvalues.JsObj;

final class JsPairsCommand extends Command {

  private static final String COMMAND_NAME = "json-pairs";

  public JsPairsCommand() {
    super(COMMAND_NAME,
          """
                  Returns the list of path/value pairs of the JSON stored in the 'output' variable.
                  It's possible to filter out the list of pairs by passing a substring.
              
                  Usage:
                      json-pairs [substring]
              
                  Parameters:
                      [substring] - Optional. A substring to filter the list of pairs by path or value.
              
                  Examples:
                      json-pairs          (returns all path/value pairs of the JSON stored in 'output')
                      json-pairs email    (returns path/value pairs containing 'email' in path or value)
              """
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
                                                   .contains(search) || it.value()
                                                                          .toString()
                                                                          .contains(search);
                                        })
                                .map(it -> String.format("%s -> %s",
                                                         it.path(),
                                                         "\\%s".formatted(it.value()) //to escapa in shell
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
