package jio.jdbc;

import java.sql.Savepoint;
import java.util.Objects;

/**
 * Represents an exception for rolling back to a specific savepoint within a transaction.
 */
@SuppressWarnings("serial")
public final class RollBackToSavePoint extends Exception {

  final Savepoint savepoint;
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
