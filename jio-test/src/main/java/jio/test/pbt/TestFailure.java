package jio.test.pbt;

import static java.util.Objects.requireNonNull;

/**
 * Represents an observed failure during the execution of a property test. A reason must be specified to create a
 * TestFailure.
 */
@SuppressWarnings("serial")
public final class TestFailure extends Exception implements TestResult {

  /**
   * Constructs a new TestFailure with the specified failure reason.
   *
   * @param reason The reason explaining why the test failed.
   */
  TestFailure(String reason) {
    super(reason);
  }

  /**
   * Creates a TestFailure with the given failure reason.
   *
   * @param reason The failure reason.
   * @return A TestFailure instance representing the failure reason.
   */
  public static TestFailure reason(final String reason) {
    return new TestFailure(requireNonNull(reason));
  }
}
