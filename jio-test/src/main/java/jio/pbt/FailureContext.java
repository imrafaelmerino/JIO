package jio.pbt;


import jsonvalues.JsObj;
import jsonvalues.JsStr;

/**
 * Represents information related to a failure that was observed during the execution
 * of a specific test.
 *
 * @param context the context of the failure
 * @param failure the failure
 */
public record FailureContext(Context context,
                             TestFailure failure
) {

    /**
     * serializes this record into a json. The json schema is the following
     *
     * <pre>
     *     {@code
     *           {
     *               "context": JsObj,
     *               "reason": string
     *           }
     *     }
     * </pre>
     *
     * @return a json
     * @see Context#toJson()
     */
    public JsObj toJson() {
        return JsObj.of("context", context.toJson(),
                        "reason", JsStr.of(failure.getMessage())
                       );
    }
}
