package jio.cli;

import jio.IO;
import jsonvalues.JsObj;

import java.util.function.Function;

/**
 * Represents a command that shuts down the console. Usage: {@code exit}
 * <p>
 * This command terminates the console application.
 */
class ExitCommand extends Command {

  private static final String COMMAND_NAME = "exit";

  public ExitCommand() {
    super(COMMAND_NAME,
          "Shuts down the console");
  }

  @Override
  public Function<String[], IO<String>> apply(final JsObj conf,
                                              final State state
  ) {
    ConsolePrinter.printlnResult("bye bye!");
    System.exit(0);
    return null;
  }
}
