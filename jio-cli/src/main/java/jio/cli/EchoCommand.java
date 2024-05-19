package jio.cli;

import jio.IO;
import jsonvalues.JsObj;

import java.util.function.Function;

/**
 * Represents a command that prints out a message into the console. Usage: {@code echo {text}}
 * <p>
 * Example: {@code echo hi, how are you doing?} {@code echo $var}
 */
final class EchoCommand extends Command {

    private static final String COMMAND_NAME = "echo";

    public EchoCommand() {
        super(COMMAND_NAME,
              """
                      Prints a message to the console. This message can be literal text or the value of a variable.
                      Usage:
                        echo {text}
                      Examples:
                        echo hi, how are you doing?
                        echo $var
                        echo $output
                      Note: $output is a special variable that holds the result of the last executed command.
                      """
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
