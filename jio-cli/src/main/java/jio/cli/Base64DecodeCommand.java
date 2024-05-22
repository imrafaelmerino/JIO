package jio.cli;

import jio.IO;
import jio.RetryPolicies;
import jio.cli.ConsolePrograms.AskForInputParams;
import jsonvalues.JsObj;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.function.Function;

import static jio.cli.ConsolePrograms.ASK_FOR_INPUT;

/**
 * Represents a command that decodes a base64 encoded string into a new string using the Base64 encoding scheme. The
 * decoded string is returned as the result of this command.
 * <p>
 * Usage: {@code base64-decode {encoded}}
 * <p>
 * Examples: - {@code base64-decode aGkhIGknbGwgYmUgZW5jb2RlZCBpbnRvIGJhc2UgNjQ=} - {@code base64-decode $var}
 */
final class Base64DecodeCommand extends Command {

    private final static Base64.Decoder decoder = Base64.getDecoder();
    private static final String COMMAND_NAME = "base64-decode";

    public Base64DecodeCommand() {
        super(
                COMMAND_NAME,
                """
                        Decodes a base64 encoded string into its original form using the Base64 encoding scheme.
                        Usage:
                          base64-decode (prompts the user to input the base64-encoded text)
                          base64-decode {encoded_string}
                        Examples:
                            base64-decode $var
                            base64-decode aGkhIGknbGwgYmUgZW5jb2RlZCBpbnRvIGJhc2UgNjQ=""");
    }

    @Override
    public Function<String[], IO<String>> apply(
            final JsObj conf,
            final State state
                                               ) {
        return tokens -> {
            int nTokens = tokens.length;
            if (nTokens == 1) {
                return ASK_FOR_INPUT(new AskForInputParams("Type the string encoded in base64",
                                                           e -> e.length() == 1,
                                                           "Space blank is not a valid base64 scheme",
                                                           RetryPolicies.limitRetries(3)))
                        .then(encoded -> IO.succeed(new String(
                                decoder.decode(encoded),
                                StandardCharsets.UTF_8
                        )));
            }

            if (nTokens == 2) {
                return IO.succeed(
                        new String(decoder.decode(tokens[1]),
                                   StandardCharsets.UTF_8)
                                 );
            }

            return IO.fail(new IllegalArgumentException("Space blank is not a valid base64 character"));

        };
    }
}
