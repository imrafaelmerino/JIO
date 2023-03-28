package jio.pbt;


import static java.util.Objects.requireNonNull;

/**
 * Represent an exception thrown during the execution of a property test.
 */
@SuppressWarnings("serial")
public final class TestException extends Throwable implements TestResult {
    TestException(Throwable cause) {
        super(requireNonNull(cause));
    }
}
