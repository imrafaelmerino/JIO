package jio.console;

import jio.IO;
import jsonvalues.JsObj;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.function.Function;

class Base64EncodeCommand extends Command {

    private final static Base64.Encoder encoder = Base64.getEncoder();

    private static final String COMMAND_NAME = "encode64";

    public Base64EncodeCommand() {
        super("encode64",
              """
                      Encodes the specified string into a new string using the Base64 encoding scheme.
                      Examples:
                          $command hi! i'll be encoded into base 64
                          $command $var""".replace("$command", COMMAND_NAME)
             );
    }


    @Override
    public Function<String[], IO<String>> apply(final JsObj conf,
                                                final State state
                                               ) {
        return tokens -> {
            int nTokens = tokens.length;
            if (nTokens == 1)
                return Programs.ASK_FOR_INPUT(new Programs.AskForInputParams("Type the text"))
                               .then(text -> encode(text));
            return encode(Functions.joinTail(tokens));
        };
    }

    private static IO<String> encode(String text) {
        return IO.fromValue(encoder.encodeToString(text.getBytes(StandardCharsets.UTF_8)));
    }
}
