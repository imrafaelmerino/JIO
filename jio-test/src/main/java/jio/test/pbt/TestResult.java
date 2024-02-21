package jio.test.pbt;

/**
 * Represents the result of the execution of a property test. This sealed interface has three implementations:
 * TestException, TestFailure, and TestSuccess.
 */
public sealed interface TestResult permits TestFailure, TestSuccess {

  /**
   * A singleton that represents a successful execution of a property test.
   */
  TestSuccess SUCCESS = new TestSuccess();
}
