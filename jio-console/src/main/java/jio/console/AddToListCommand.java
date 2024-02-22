package jio.console;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.function.Function;
import jio.IO;
import jsonvalues.JsObj;

class AddToListCommand extends Command {

  private static final String COMMAND_NAME = "var-add";

  public AddToListCommand() {
    super(COMMAND_NAME,
          """
              Add the given value into the the specified list. You can read
              the content of the list with the command var-get {name}.
              Usage: var-add {name} {value}
              Examples:
                  $command names Rafa
                  $command numbers $counter""".replace("$command",
                                                       COMMAND_NAME)
         );
  }

  private static IO<String> addValueToList(State state,
                                           String varName,
                                           String newValue
                                          ) {

    return IO.lazy(() -> {
      if (!state.listsVariables.containsKey(varName)) {
        state.listsVariables.put(varName,
                                 new ArrayList<>());
      }

      state.listsVariables.get(varName)
                          .add(newValue);
      return "added! Size of " + varName + " is " + state.listsVariables.get(varName)
                                                                        .size();
    });
  }

  @Override
  public Function<String[], IO<String>> apply(final JsObj conf,
                                              final State state
                                             ) {
    return tokens -> {
      int nTokens = tokens.length;

      if (nTokens == 1) {
        return Programs.ASK_FOR_PAIR(new Programs.AskForInputParams("Type the name of the list"),
                                     new Programs.AskForInputParams("Type the value to be added")
                                    )
                       .then(pair -> addValueToList(state,
                                                    pair.first(),
                                                    pair.second()));
      }

      if (nTokens == 2) {
        return Programs
            .ASK_FOR_INPUT(new Programs.AskForInputParams("Type the value to be added"))
            .then(value -> addValueToList(state,
                                          tokens[1],
                                          value));
      }

      return addValueToList(state,
                            tokens[1],
                            String.join(" ",
                                        Arrays.stream(tokens)
                                              .toList()
                                              .subList(2,
                                                       tokens.length))
                           );

    };
  }
}
