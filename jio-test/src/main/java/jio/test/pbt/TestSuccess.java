package jio.test.pbt;

/**
 * Represents the successful execution of a property test. Instances of this class indicate that a property test has
 * passed without any issues.
 * <p>
 * This class provides a singleton instance, {@link TestResult#SUCCESS}, which must be used to represent a successful
 * property test result.
 * </p>
 */
public final class TestSuccess implements TestResult {

  TestSuccess() {
  }
}
