package jio.test.pbt;

/**
 * Represents the result of the execution of a property test
 */
public sealed interface TestResult permits TestException, TestFailure, TestSuccess {
    /**
     * singleton tha represents a successful execution of a property test
     */
    TestSuccess SUCCESS = new TestSuccess();

}
