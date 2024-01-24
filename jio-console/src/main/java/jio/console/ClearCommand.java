package jio.console;

import jio.IO;
import jsonvalues.JsObj;

import java.util.function.Function;

/**
 * Represents a command that clears the console screen. When executed, this command clears the console and returns an
 * empty string as a result.
 * <p>
 * Usage: {@code clear}
 * <p>
 * Example: {@code clear}
 */
class ClearCommand extends Command {

  private static final String COMMAND_NAME = "clear";

  public ClearCommand() {
    super(COMMAND_NAME,
          "Clear the console");
  }

  @Override
  public Function<String[], IO<String>> apply(final JsObj conf,
                                              final State state
                                             ) {
    return tokens -> {
      int nArgs = tokens.length - 1;
      if (nArgs > 1) {
        return Errors.TOO_MANY_ARGUMENTS.apply(nArgs,
                                               this);
      }
      return IO.succeed(ControlChars.CLEAR.code + ControlChars.RESET.code);
    };
  }
}
