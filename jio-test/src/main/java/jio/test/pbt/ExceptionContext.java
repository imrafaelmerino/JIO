package jio.test.pbt;

import java.util.Arrays;
import java.util.List;
import jsonvalues.JsArray;
import jsonvalues.JsObj;
import jsonvalues.JsStr;

/**
 * Represents information related to an exception that occurred during the execution of a specific test.
 *
 * @param context   The context of the exception.
 * @param exception The exception.
 */
public record ExceptionContext(Context context,
                               Throwable exception) {

  /**
   * Serializes this record into a JSON object, converting the exception into a JSON object with its message, class
   * name, and stacktrace. The JSON schema is as follows:
   *
   * <pre>
   * {@code
   *     {
   *         "context": JsObj (see Context#toJson()),
   *         "message": String (or empty string if null),
   *         "type": String (class name of the exception),
   *         "stacktrace": JsArray[String]
   * }
   * }
   * </pre>
   *
   * @return A JSON representation of the exception context.
   * @see Context#toJson()
   */
  public JsObj toJson() {
    List<JsStr> stacktrace = Arrays.stream(exception.getStackTrace())
                                   .map(it -> JsStr.of(it.toString()))
                                   .toList();

    return JsObj.of("context",
                    context.toJson(),
                    "message",
                    exception.getMessage() != null ? JsStr.of(exception.getMessage()) : JsStr.of(""),
                    "type",
                    JsStr.of(exception.getClass()
                                      .getName()),
                    "stacktrace",
                    JsArray.ofIterable(stacktrace)
                   );
  }

}
