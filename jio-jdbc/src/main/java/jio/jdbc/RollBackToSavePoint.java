package jio.jdbc;

import java.sql.Savepoint;
import java.util.Objects;

/**
 * Represents an exception for rolling back to a specific savepoint within a transaction.
 *
 * <p>
 * This exception is thrown when it's necessary to roll back to a specific savepoint within a transaction. It contains
 * information about the savepoint and any associated output result.
 * </p>
 *
 * <p>
 * Instances of this exception can be created using the provided factory methods:
 * </p>
 *
 * <ul>
 * <li>{@link #of(Savepoint, Object)}: Creates a new instance without a specific cause.</li>
 * <li>{@link #of(Savepoint, Object, Throwable)}: Creates a new instance with a specified cause (typically an
 * exception).</li>
 * </ul>
 */
@SuppressWarnings("serial")
public final class RollBackToSavePoint extends Exception {

  /**
   * The savepoint to which the transaction should be rolled back.
   */
  final Savepoint savepoint;

  /**
   * The output result associated with the savepoint.
   */
  final Object output;

  private RollBackToSavePoint(final Throwable cause,
                              final Savepoint savepoint,
                              final Object output) {
    super(cause);
    this.savepoint = savepoint;
    this.output = output;
  }

  private RollBackToSavePoint(Savepoint savepoint,
                              Object output) {
    this.savepoint = Objects.requireNonNull(savepoint);
    this.output = output;
  }

  /**
   * Factory method to create a new instance of {@code RollBackToSavePoint}.
   *
   * @param savepoint The savepoint to which the transaction should be rolled back.
   * @param output    The output result associated with the savepoint.
   * @return A new instance of {@code RollBackToSavePoint}.
   */
  public static RollBackToSavePoint of(Savepoint savepoint,
                                       Object output) {
    return new RollBackToSavePoint(savepoint,
                                   output);
  }

  /**
   * Factory method to create a new instance of {@code RollBackToSavePoint} with a cause.
   *
   * @param savepoint The savepoint to which the transaction should be rolled back.
   * @param output    The output result associated with the savepoint.
   * @param cause     The original cause of the rollback, typically an exception.
   * @return A new instance of {@code RollBackToSavePoint} with a cause.
   */
  public static RollBackToSavePoint of(Savepoint savepoint,
                                       Object output,
                                       Throwable cause) {
    return new RollBackToSavePoint(cause,
                                   savepoint,
                                   output);
  }
}
