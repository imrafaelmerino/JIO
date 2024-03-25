package jio.jdbc;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Savepoint;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Callable;
import jio.IO;
import jio.Lambda;
import jio.ListExp;

/**
 * Represents a builder for creating transactions in a JDBC context. This builder provides methods for configuring
 * transaction properties, such as isolation level, Java Flight Recorder (JFR) event recording, and labels. It supports
 * the execution of multiple JDBC operations within a transaction, either in parallel or sequentially, and provides
 * flexibility for handling transactional results and rollbacks.
 * <p>
 * The transactions created by this builder can operate in parallel or sequentially, and they support the use of
 * savepoints. Savepoints allow you to set points within a transaction, and in case of an error, you can roll back the
 * transaction to a specific savepoint, preserving changes made up to that point. It's important to note that savepoints
 * must be supported by the underlying JDBC driver for them to be effective. If your JDBC driver does not support
 * savepoints, attempting to use them may result in exceptions or unexpected behavior.
 */
public final class TxBuilder {

  final DatasourceBuilder datasourceBuilder;

  /**
   * Specifies the isolation level for the transaction. The isolation level determines the degree to which the
   * operations within a transaction are isolated from the operations in other transactions.
   */
  final TX_ISOLATION isolation;

  /**
   * Creates a new instance of {@code TxBuilder} with the specified {@link DatasourceBuilder} and transaction isolation
   * level.
   *
   * <p>
   * This static factory method is used to conveniently instantiate a {@code TxBuilder} for building transactional
   * operations.
   * </p>
   *
   * @param datasourceBuilder An instance of {@link DatasourceBuilder} providing configuration for creating a database
   *                          connection.
   * @param level             The transaction isolation level represented by the {@link TX_ISOLATION} enum.
   * @return A new instance of {@code TxBuilder} configured with the provided {@link DatasourceBuilder} and transaction
   * isolation level.
   */
  public static TxBuilder of(DatasourceBuilder datasourceBuilder,
                             TX_ISOLATION level) {
    return new TxBuilder(datasourceBuilder,
                         level);
  }

  private boolean enableJFR = true;
  private String label;

  /**
   * Sets a label for the Java Flight Recorder (JFR) event associated with this database query statement builder. The
   * label provides a descriptive identifier for the event and can be useful for tracking and analyzing events.
   *
   * @param label The label to be assigned to the JFR event.
   * @return This {@code QueryStmBuilder} instance with the specified event label.
   */
  public TxBuilder withEventLabel(String label) {
    this.label = Objects.requireNonNull(label);
    return this;
  }

  /**
   * Disables recording of Java Flight Recorder (JFR) events for the update operation.
   *
   * @return This UpdateStmBuilder instance for method chaining.
   */
  public TxBuilder withoutRecordedEvents() {
    this.enableJFR = false;
    return this;
  }

  /**
   * Enumeration representing transaction isolation levels for use with the {@link TxBuilder} class.
   *
   * <p>
   * Transaction isolation levels define the visibility of changes made by one transaction to other concurrent
   * transactions. Different isolation levels provide different trade-offs between consistency and performance.
   * </p>
   *
   * <p>
   * This enumeration includes the following isolation levels:
   * </p>
   *
   * <ul>
   * <li>{@link #TRANSACTION_READ_UNCOMMITTED}: The lowest isolation level where transactions can read uncommitted
   * changes made by other transactions.</li>
   * <li>{@link #TRANSACTION_READ_COMMITTED}: Transactions can only read committed changes made by other
   * transactions.</li>
   * <li>{@link #TRANSACTION_REPEATABLE_READ}: Transactions can read committed changes and can repeat the same read
   * operation and get the same results.</li>
   * <li>{@link #TRANSACTION_SERIALIZABLE}: The highest isolation level where transactions are completely isolated from
   * each other.</li>
   * </ul>
   */
  public enum TX_ISOLATION {

    /**
     * The lowest isolation level where transactions can read uncommitted changes made by other transactions.
     */
    TRANSACTION_READ_UNCOMMITTED(Connection.TRANSACTION_READ_UNCOMMITTED),
    /**
     * Transactions can only read committed changes made by other transactions.
     */
    TRANSACTION_READ_COMMITTED(Connection.TRANSACTION_READ_COMMITTED),
    /**
     * Transactions can read committed changes and can repeat the same read operation and get the same results.
     */
    TRANSACTION_REPEATABLE_READ(Connection.TRANSACTION_REPEATABLE_READ),

    /**
     * The highest isolation level where transactions are completely isolated from each other.
     */
    TRANSACTION_SERIALIZABLE(Connection.TRANSACTION_SERIALIZABLE);

    private final int level;

    TX_ISOLATION(final int level) {
      this.level = level;
    }
  }

  private TxBuilder(DatasourceBuilder datasourceBuilder,
                    TX_ISOLATION isolation) {
    this.datasourceBuilder = Objects.requireNonNull(datasourceBuilder);
    this.isolation = Objects.requireNonNull(isolation);
  }

  /**
   * Builds and returns a parallel transaction that executes a list of JDBC operations concurrently. The resulting
   * {@code IO} represents the entire transaction and includes a list of outputs from individual operations. The
   * transaction is configured with the specified settings, and operations are executed on virtual threads for improved
   * concurrency and resource utilization.
   *
   * @param lambdas  The list of JDBC operations to be executed in parallel.
   * @param <Output> The type of the output result from each operation.
   * @return An {@code IO} representing the parallel JDBC transaction with a list of outputs.
   * @see JfrEventDecorator#decorateTx(IO, String, boolean)
   */
  public <Output> IO<List<Output>> buildPar(List<Lambda<Connection, Output>> lambdas) {
    Objects.requireNonNull(lambdas);
    IO<List<Output>> tx = IO.resource(getConnection(),
                                      connection -> lambdas.stream()
                                                           .map(statement -> statement.apply(connection))
                                                           .collect(ListExp.parCollector())
                                                           .then(result -> IO.task(() -> {
                                                                   connection.commit();
                                                                   return result;
                                                                 }),
                                                                 exc -> {
                                                                   try {
                                                                     connection.rollback();
                                                                     return IO.fail(exc);
                                                                   } catch (SQLException e) {
                                                                     return IO.fail(exc);
                                                                   }
                                                                 })
                                     );
    return JfrEventDecorator.decorateTx(tx,
                                        label,
                                        enableJFR);
  }

  /**
   * Builds and returns a sequential transaction that executes a list of JDBC operations sequentially. The resulting
   * {@code IO} represents the entire transaction and includes the output from the last operation. The transaction is
   * configured with the specified settings, and operations are executed on virtual threads for improved concurrency and
   * resource utilization.
   *
   * @param lambdas  The list of JDBC operations to be executed sequentially.
   * @param <Output> The type of the output result from each operation.
   * @return An {@code IO} representing the sequential JDBC transaction with the output from the last operation.
   * @see JfrEventDecorator#decorateTx(IO, String, boolean)
   */
  public <Output> IO<List<Output>> buildSeq(List<Lambda<Connection, Output>> lambdas) {
    Objects.requireNonNull(lambdas);
    IO<List<Output>> tx = IO.resource(getConnection(),
                                      connection -> lambdas.stream()
                                                           .map(statement -> statement.apply(connection))
                                                           .collect(ListExp.seqCollector())
                                                           .then(result -> IO.task(() -> {
                                                                   connection.commit();
                                                                   return result;
                                                                 }),
                                                                 exc -> {
                                                                   try {
                                                                     connection.rollback();
                                                                     return IO.fail(exc);
                                                                   } catch (SQLException e) {
                                                                     return IO.fail(exc);
                                                                   }
                                                                 })
                                     );
    return JfrEventDecorator.decorateTx(tx,
                                        label,
                                        enableJFR);
  }

  /**
   * Builds and returns a transaction that executes a single JDBC statement within a transaction. The resulting
   * {@code Lambda} represents the entire transaction, and the specified {@code ClosableStatement} is executed on the
   * provided input parameters. The transaction is configured with the specified settings, and the operation is executed
   * on a virtual thread for improved concurrency.
   *
   * @param closableStatement The JDBC statement to be executed within the transaction.
   * @param <Params>          The type of input parameters for the JDBC statement.
   * @param <Output>          The type of the output result from the JDBC statement.
   * @return A {@code Lambda} representing the JDBC transaction with the specified statement.
   * @see JfrEventDecorator#decorateTx(IO, String, boolean)
   */
  public <Params, Output> Lambda<Params, Output> build(ClosableStatement<Params, Output> closableStatement) {
    Objects.requireNonNull(closableStatement);
    return params -> {
      IO<Output> tx = IO.resource(getConnection(),
                                  connection -> closableStatement.apply(params,
                                                                        connection)
                                                                 .then(result -> IO.task(() -> {
                                                                         connection.commit();
                                                                         return result;
                                                                       }),
                                                                       exc -> {
                                                                         try {
                                                                           connection.rollback();
                                                                           return IO.fail(exc);
                                                                         } catch (SQLException e) {
                                                                           return IO.fail(e);
                                                                         }
                                                                       })
                                 );
      return JfrEventDecorator.decorateTx(tx,
                                          label,
                                          enableJFR);
    };
  }

  private Callable<Connection> getConnection() {
    return () -> {
      Connection connection = datasourceBuilder.get()
                                               .getConnection();
      connection.setAutoCommit(false);

      connection.setTransactionIsolation(isolation.level);
      return connection;
    };
  }

  /**
   * Builds and returns a transaction that executes a single JDBC statement within a transaction, allowing for rollbacks
   * to specific savepoints. The resulting {@code Lambda} represents the entire transaction, and the specified
   * {@code ClosableStatement} is executed on the provided input parameters. The transaction is configured with the
   * specified settings, and the operation is executed on a virtual thread for improved concurrency.
   *
   * @param closableStatement The JDBC statement to be executed within the transaction.
   * @param <Params>          The type of input parameters for the JDBC statement.
   * @param <Output>          The type of the output result from the JDBC statement.
   * @return A {@code Lambda} representing the JDBC transaction with the specified statement and support for savepoints.
   * @see JfrEventDecorator#decorateTxWithSavePoints(IO, String, boolean)
   */
  public <Params, Output> Lambda<Params, TxResult> buildWithSavePoints(ClosableStatement<Params, Output> closableStatement) {
    Objects.requireNonNull(closableStatement);
    return params -> IO.resource(getConnection(),
                                 connection -> {
                                   IO<TxResult> tx = closableStatement.apply(params,
                                                                             connection)
                                                                      .then(result -> IO.task(() -> {
                                                                              connection.commit();
                                                                              return new TxSuccess<>(result);
                                                                            }),
                                                                            exc -> {
                                                                              try {
                                                                                if (exc instanceof RollBackToSavePoint rollBackToSavePoint) {
                                                                                  Savepoint savepoint = rollBackToSavePoint.savepoint;
                                                                                  connection.rollback(savepoint);
                                                                                  connection.commit();
                                                                                  return IO.succeed(new TxPartialSuccess(rollBackToSavePoint.savepoint.getSavepointName(),
                                                                                                                         rollBackToSavePoint.output,
                                                                                                                         rollBackToSavePoint)
                                                                                                   );
                                                                                } else {
                                                                                  connection.rollback();
                                                                                  return IO.fail(exc);
                                                                                }
                                                                              } catch (SQLException e) {
                                                                                return IO.fail(e);
                                                                              }
                                                                            });
                                   return JfrEventDecorator.decorateTxWithSavePoints(tx,
                                                                                     label,
                                                                                     enableJFR);
                                 }
                                );
  }

}
