package jio.test.pbt;

import java.time.Instant;
import jsonvalues.JsInstant;
import jsonvalues.JsInt;
import jsonvalues.JsLong;
import jsonvalues.JsObj;
import jsonvalues.JsStr;

/**
 * Represents information related to a specific test execution.
 *
 * @param start              The instant when a test starts.
 * @param seed               The seed for random data generation.
 * @param generatedSeqNumber The sequence number for data generation.
 * @param input              The input data of the test.
 */
record Context(Instant start,
               long seed,
               int generatedSeqNumber,
               Object input,
               String tags
) {

  /**
   * Serializes this record into a JSON object, converting the input data into a string with its toString method. The
   * JSON schema is as follows:
   *
   * <pre>
   * {@code
   *     {
   *         "start": instant,
   *         "seed": long,
   *         "seq_number": int,
   *         "input": string,
   *         "tags": string
   * }
   * }
   * </pre>
   *
   * @return A JSON representation of the test context.
   */
  public JsObj toJson() {
    return JsObj.of("start",
                    JsInstant.of(start),
                    "seed",
                    JsLong.of(seed),
                    "seq_number",
                    JsInt.of(generatedSeqNumber),
                    "input",
                    JsStr.of(input.toString()),
                    "tags",
                    JsStr.of(tags)
                   );
  }
}
