package jio.test.pbt;

import jsonvalues.*;

import java.time.Instant;

/**
 * Represents information related to a specific test execution
 *
 * @param start              instant when a test starts
 * @param seed               seed of the random data generation
 * @param generatedSeqNumber number of generation
 * @param input              the input data of the test
 */
public record Context(Instant start,
                      long seed,
                      int generatedSeqNumber,
                      Object input
) {

    /**
     * serializes this record into a json converting the input data into a string with its
     * toString method. The json schema is the following
     *
     * <pre>
     *     {@code
     *           {
     *               "start": instant,
     *               "seed": long
     *               "seq_number": int
     *               "input": string
     *           }
     *
     *     }
     * </pre>
     *
     * @return a json
     */
    public JsObj toJson() {
        return JsObj.of("start", JsInstant.of(start),
                        "seed", JsLong.of(seed),
                        "seq_number", JsInt.of(generatedSeqNumber),
                        "input", JsStr.of(input.toString())
                       );
    }
}
