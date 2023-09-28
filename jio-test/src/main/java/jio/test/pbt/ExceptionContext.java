package jio.test.pbt;


import jsonvalues.JsArray;
import jsonvalues.JsObj;
import jsonvalues.JsStr;

import java.util.Arrays;
import java.util.List;

/**
 * Represents information related to an exception that happened during the execution
 * of a specific test.
 *
 * @param context   the context of the exception
 * @param exception the exception
 */
public record ExceptionContext(Context context,
                               Throwable exception) {

    /**
     * serializes this record into a json converting the exception into a Json with its
     * message, class name and stacktrace. The json schema is the following
     *
     * <pre>
     *     {@code
     *           {
     *               "context": JsObj,
     *               "message": String,
     *               "type":  String,
     *               "stacktrace": JsArray[String]
     *           }
     *
     *     }
     * </pre>
     *
     * @return a json
     * @see Context#toJson()
     */
    public JsObj toJson() {
        List<JsStr> stacktrace = Arrays.stream(exception.getStackTrace())
                                       .map(it -> JsStr.of(it.toString()))
                                       .toList();

        return JsObj.of("context", context.toJson(),
                        "message", exception.getMessage() != null ? JsStr.of(exception.getMessage()) : JsStr.of(""),
                        "type", JsStr.of(exception.getClass().getName()),
                        "stacktrace", JsArray.ofIterable(stacktrace)
                       );
    }


}
