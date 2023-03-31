package jio.console;

import jio.IO;
import jio.RetryPolicies;
import jsonvalues.JsObj;

import java.util.Base64;
import java.util.function.Function;

class Base64DecodeCommand extends Command {

    private final static Base64.Decoder decoder = Base64.getDecoder();
    private static final String COMMAND_NAME = "decode64";

    public Base64DecodeCommand() {
        super(COMMAND_NAME,
              """
                      Decodes a base64 encoded string into a new string using the Base64 encoding scheme.
                      Examples:
                          $command aGkhIGknbGwgYmUgZW5jb2RlZCBpbnRvIGJhc2UgNjQ=
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
                return Programs.ASK_FOR_INPUT(new Programs.AskForInputParams("Type the string encoded in base64",
                                                                             e -> e.length() == 1,
                                                                             "Space blank is not a valid base64 scheme",
                                                                             RetryPolicies.limitRetries(3)
                )).then(encoded -> IO.fromValue(new String(decoder.decode(encoded))));

            if (nTokens == 2)
                return IO.fromValue(new String(decoder.decode(tokens[1])));

            return IO.fromFailure(new InvalidCommand(this, "Space blank is not a valid base64 character"));


        };
    }
}
