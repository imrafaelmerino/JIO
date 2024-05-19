package jio.cli;

import jio.IO;
import jio.cli.ConsolePrograms.AskForInputParams;
import jsonvalues.JsObj;

import java.util.Arrays;
import java.util.function.Function;

final class SetVarCommand extends Command {

  private static final String COMMAND_NAME = "var-set";

  public SetVarCommand() {
    super(COMMAND_NAME,
          """
              Stores a value into the specified variable.
              
              Usage:
                  $command                  (will ask you interactively for the variable name and value)
                  $command <name> <value>
                  $command <name> $<existing_variable>
              
              Examples:
                  var-set            (prompts you to enter the variable name and value interactively)
                  var-set age 40     (sets the variable 'age' to the value '40')
                  var-set counter $var  (sets the variable 'counter' to the value stored in 'var')
              
              Parameters:
                  <name>   - The name of the variable to set.
                  <value>  - The value to assign to the variable.
                  $<existing_variable> - The value of an existing variable to assign to <name>.
              
              Notes:
                  - Variable names are case-sensitive.
              """
         );
  }


  private static IO<String> setVarValue(State state,
                                        String varName,
                                        String newValue
  ) {

    return IO.lazy(() -> {
      String oldValue = state.variables.get(varName);
      state.variables.put(varName,
                          newValue);
      return String.format("Variable `%s` updated. From `%s` to `%s`",
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

      if (nTokens == 1) {
        return ConsolePrograms.ASK_FOR_PAIR(new AskForInputParams("Type the name of the variable"),
                                            new AskForInputParams("Type the value")
                                           )
                              .then(pair -> setVarValue(state,
                                                 pair.first(),
                                                 pair.second()));
      }

      if (nTokens == 2) {
        return ConsolePrograms
                       .ASK_FOR_INPUT(new AskForInputParams("Type the variable"))
                       .then(value -> setVarValue(state,
                                                  tokens[1],
                                                  value));
      }

      return setVarValue(state,
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
