package jio.console;

import jio.IO;
import jsonvalues.JsObj;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.function.Function;

class EncodeURLCommand extends Command {

    private static final String COMMAND_NAME = "url-encode";

    public EncodeURLCommand() {
        super(COMMAND_NAME,
              """
                      Translates a string into application/x-www-form-urlencoded format using.
                      Usage: $command {text}
                      Examples:
                          $command hi, how are you doing
                          $command $var""".replace("$command", COMMAND_NAME)
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
                               .then(text -> IO.fromValue(URLEncoder.encode(text, StandardCharsets.UTF_8)));
            return IO.fromValue(URLEncoder.encode(Functions.joinTail(tokens), StandardCharsets.UTF_8));
        };
    }
}
