package jio.console;

import jio.IO;
import jsonvalues.JsObj;

import java.util.function.Function;

/**
 * Represents a command that prints out a message into the console. Usage: {@code echo {text}}
 * <p>
 * Example: {@code echo hi, how are you doing?} {@code echo $var}
 */
class EchoCommand extends Command {

  private static final String COMMAND_NAME = "echo";

  public EchoCommand() {
    super(COMMAND_NAME,
          """
              Prints out the message into the console.
              Usage: echo {text}
              Example:
                  $command hi, how are you doing?""".replace("$command",
                                                             COMMAND_NAME)
         );
  }

  @Override
  public Function<String[], IO<String>> apply(final JsObj conf,
                                              final State state
                                             ) {
    return tokens -> {
      int nArgs = tokens.length - 1;
      return nArgs == 0 ? IO.succeed("") : IO.succeed(Functions.joinTail(tokens));
    };
  }
}
