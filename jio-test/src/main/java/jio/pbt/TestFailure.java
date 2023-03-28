package jio.pbt;

import static java.util.Objects.requireNonNull;

/**
 * Represents an observed failure during the execution of a property test.
 * A reason must be specified to create a TestFailure
 */
@SuppressWarnings("serial")
public final class TestFailure extends Exception implements TestResult {
    /**
     * Creates a TestFailure from a reason explaining why the test failed
     *
     * @param reason the failure reason
     * @return a TestFailure
     */
    public static TestFailure reason(final String reason) {
        return new TestFailure(requireNonNull(reason));
    }

    TestFailure(String reason) {
        super(reason);
    }

}
