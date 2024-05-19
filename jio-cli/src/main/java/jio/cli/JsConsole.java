package jio.cli;

import java.util.Objects;
import jio.IO;
import jio.Lambda;
import jio.RetryPolicies;
import jsonvalues.JsNothing;
import jsonvalues.JsPath;
import jsonvalues.JsValue;
import jsonvalues.spec.JsParserException;
import jsonvalues.spec.JsSpec;

/**
 * Represents a lambda that takes a JsPath and returns a JIO effect that executes an interactive program to compose the
 * associated JsValue to that path.
 * <p>
 * Use the static method {@link #of(JsSpec)} to create JsConsole programs from the spec the output introduced by the use
 * has to conform to.
 *
 * @param <Output> type of the JsValue returned
 * @see JsObjConsole
 * @see JsTupleConsole
 */
public interface JsConsole<Output extends JsValue> extends Lambda<JsPath, Output> {

  /**
   * Factory method to create console programs that ask for the user to type in a json output that conforms to the given
   * spec
   *
   * @param spec the spec the output has to conform to
   * @return JsConsole program
   */
  static JsConsole<JsValue> of(final JsSpec spec) {
    Objects.requireNonNull(spec);
    return path -> IO.lazy(()->ConsolePrinter.printPrompt(String.format("%s%s -> ",
                                                           Functions.indent(path),
                                                           path
                                                                       ))
                                            )
                                 .then(_ -> ConsoleReaders.READ_LINE)
                                 .then(s -> {
                                         try {
                                           if (s.isEmpty()) {
                                             return IO.succeed(JsNothing.NOTHING);
                                           }
                                           return IO.succeed(spec.parse(s));
                                         } catch (JsParserException e) {
                                           return IO.fail(e);
                                         }
                                       }
                                      )
                                 .peekFailure(exc -> ConsolePrinter.printlnError(STR."\{Functions.indent(path)}Error: \{exc.getMessage()}"))
                                 .retry(RetryPolicies.limitRetries(3));
  }
}
