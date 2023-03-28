package jio.console;

import jio.IO;
import jsonvalues.JsParserException;
import jsonvalues.spec.JsIO;
import jsonvalues.spec.JsReader;
import jio.Lambda;
import jio.RetryPolicies;
import jsonvalues.JsNothing;
import jsonvalues.JsPath;
import jsonvalues.JsValue;
import jsonvalues.spec.JsSpec;

import java.util.Objects;


import static jio.console.Functions.indent;

/**
 * Represents a lambda that takes a JsPath and returns a JIO effect that executes an interactive program
 * to compose the associated JsValue to that path.
 * <p>
 * Use the static method {@link #of(JsSpec)} to create JsConsole programs from the spec
 * the value introduced by the use has to conform to.
 *
 * @param <T> type of the JsValue returned
 * @see JsObjConsole
 * @see JsTupleConsole
 */
public interface JsConsole<T extends JsValue> extends Lambda<JsPath, T> {


    /**
     * Factory method to create console programs that ask for the user to
     * type in a json value that conforms to the given spec
     * @param spec the spec the value has to conform to
     * @return JsConsole program
     */
    static JsConsole<JsValue> of(final JsSpec spec) {
        Objects.requireNonNull(spec);
        return path -> Programs.PRINT_LINE(String.format("%s%s ->",
                                                         indent(path),
                                                         path
                                                        )
                                          )
                               .then(__ -> Programs.READ_LINE)
                               .then(s ->
                                     {
                                         try {
                                             if (s.isEmpty()) return IO.succeed(JsNothing.NOTHING);
                                             JsReader reader = JsIO.INSTANCE.createReader(s.getBytes());
                                             return IO.succeed(spec.readNextValue(reader)
                                                              );
                                         } catch (JsParserException e) {
                                             return IO.fail(e);
                                         }
                                     }
                                    )
                               .peekFailure(exc -> System.out.println(indent(path) + "Error: " + exc.getMessage()))
                               .retry(RetryPolicies.limitRetries(3));
    }
}
