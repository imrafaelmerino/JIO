package jio.console;

import jio.IO;
import jsonvalues.JsObj;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.function.Function;

/**
 * Represents a command that translates a string into application/x-www-form-urlencoded format.
 * Usage: {@code url-encode {text}}
 *
 * Examples:
 * {@code url-encode hi, how are you doing}
 * {@code url-encode $var}
 */
class EncodeURLCommand extends Command {

    private static final String COMMAND_NAME = "url-encode";

    public EncodeURLCommand() {
        super(COMMAND_NAME,
              """
                      Translates a string into application/x-www-form-urlencoded format using.
                      Usage: $command {text}
                      Examples:
                          $command hi, how are you doing""".replace("$command", COMMAND_NAME)
             );
    }

    @Override
    public Function<String[], IO<String>> apply(final JsObj conf,
                                                final State state
                                               ) {
        return tokens -> {
            int nArgs = tokens.length - 1;
            if (nArgs == 0)
                return Programs.ASK_FOR_INPUT(new Programs.AskForInputParams("Type some text"))
                               .then(text -> IO.succeed(URLEncoder.encode(text, StandardCharsets.UTF_8)));
            return IO.succeed(URLEncoder.encode(Functions.joinTail(tokens), StandardCharsets.UTF_8));
        };
    }
}
