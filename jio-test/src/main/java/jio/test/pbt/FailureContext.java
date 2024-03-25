package jio.test.pbt;

import jsonvalues.JsObj;
import jsonvalues.JsStr;

/**
 * Represents information related to a failure that occurred during the execution of a specific test.
 *
 * @param context The context of the failure.
 * @param failure The failure.
 */
public record FailureContext(Context context,
                             TestFailure failure
) {

  /**
   * Serializes this record into a JSON object. The JSON schema is as follows:
   *
   * <pre>
   * {@code
   *     {
   *         "context": JsObj (see Context#toJson()),
   *         "reason": String (the reason for the failure)
   * }
   * }
   * </pre>
   *
   * @return A JSON representation of the failure context.
   * @see Context#toJson()
   */
  public JsObj toJson() {
    return JsObj.of("context",
                    context.toJson(),
                    "reason",
                    JsStr.of(failure.getMessage())
                   );
  }
}
