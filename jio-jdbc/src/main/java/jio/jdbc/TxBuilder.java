package jio.jdbc;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;
import jio.IO;
import jio.Lambda;
import jio.ListExp;

public final class TxBuilder {

  final DatasourceBuilder datasourceBuilder;

  final TX_ISOLATION level;

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


  public enum TX_ISOLATION {
    TRANSACTION_READ_UNCOMMITTED,
    TRANSACTION_READ_COMMITTED,
    TRANSACTION_REPEATABLE_READ,
    TRANSACTION_SERIALIZABLE
  }

  private TxBuilder(DatasourceBuilder datasourceBuilder,
                    TX_ISOLATION level) {
    this.datasourceBuilder = Objects.requireNonNull(datasourceBuilder);
    this.level = Objects.requireNonNull(level);
  }

  public <Output> IO<List<Output>> buildPar(List<Lambda<Connection, Output>> lambdas) {
    Objects.requireNonNull(lambdas);
    return IO.resource(() -> datasourceBuilder.get()
                                              .getConnection(),
                       connection ->
                           lambdas.stream()
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
  }

  public <Output> IO<List<Output>> buildSeq(List<Lambda<Connection, Output>> lambdas) {
    Objects.requireNonNull(lambdas);
    return IO.resource(() -> datasourceBuilder.get()
                                              .getConnection(),
                       connection ->
                           lambdas.stream()
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
  }


  public <Params, Output> Lambda<Params, Output> build(ClosableStatement<Params, Output> closableStatement) {
    Objects.requireNonNull(closableStatement);
    return params -> IO.resource(() -> datasourceBuilder.get()
                                                        .getConnection(),
                                 connection ->
                                     closableStatement.apply(params)
                                                      .apply(connection)
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
  }


}
