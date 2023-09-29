package jio.test.pbt;


import static java.util.Objects.requireNonNull;

/**
 * Represents an exception that is thrown during the execution of a property test.
 * This exception implements the TestResult interface to indicate test failure.
 */
public final class TestException extends Throwable implements TestResult {

    /**
     * Constructs a new TestException with the specified cause.
     *
     * @param cause The underlying cause of the exception.
     */
    TestException(Throwable cause) {
        super(requireNonNull(cause));
    }
}
