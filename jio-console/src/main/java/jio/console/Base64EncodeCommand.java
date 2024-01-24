package jio.console;

import jio.IO;
import jsonvalues.JsObj;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.function.Function;

/**
 * Represents a command that encodes a specified string into a new string using the Base64 encoding scheme. The encoded
 * string is returned as the result of this command.
 * <p>
 * Usage: {@code base64-encode {string}}
 * <p>
 * Examples: - {@code base64-encode hi! i'll be encoded into base 64} - {@code base64-encode $var}
 */
class Base64EncodeCommand extends Command {

  private final static Base64.Encoder encoder = Base64.getEncoder();

  private static final String COMMAND_NAME = "base64-encode";

  public Base64EncodeCommand() {
    super(COMMAND_NAME,
          """
              Encodes the specified string into a new string using the Base64 encoding scheme.
              Usage: $command {string}
              Examples:
                  $command hi! i'll be encoded into base 64""".replace("$command",
                                                                       COMMAND_NAME)
         );
  }

  private static IO<String> encode(String text) {
    return IO.succeed(encoder.encodeToString(text.getBytes(StandardCharsets.UTF_8)));
  }

  @Override
  public Function<String[], IO<String>> apply(final JsObj conf,
                                              final State state
                                             ) {
    return tokens -> {
      int nTokens = tokens.length;
        if (nTokens == 1) {
            return Programs.ASK_FOR_INPUT(new Programs.AskForInputParams("Type the text"))
                           .then(Base64EncodeCommand::encode);
        }
      return encode(Functions.joinTail(tokens));
    };
  }
}
