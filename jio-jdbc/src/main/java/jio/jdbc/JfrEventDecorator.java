package jio.jdbc;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;
import jio.ExceptionFun;
import jio.IO;
import jio.jdbc.TxExecutedEvent.RESULT;

/**
 * Utility class for decorating operations with Java Flight Recorder (JFR) events.
 */
class JfrEventDecorator {

  private JfrEventDecorator() {
  }

  /**
   * Wraps the provided operation with JFR events if enabled.
   *
   * @param op        The operation to wrap.
   * @param sql       The SQL statement associated with the operation.
   * @param enableJFR Indicates whether to enable JFR events.
   * @param label     The label to identify the statement
   * @return The result of the operation.
   * @throws Exception If an exception occurs during the operation.
   */
  static int decorateUpdateStm(Callable<Integer> op,
                               String sql,
                               boolean enableJFR,
                               String label) throws Exception {
    if (enableJFR) {
      UpdateStmExecutedEvent event = new UpdateStmExecutedEvent();
      event.begin();
      try {
        var n = op.call();
        event.end();
        event.rowsAffected = n;
        event.result = EntitiesFoundEvent.RESULT.SUCCESS.name();
        return n;
      } catch (Exception e) {
        event.end();
        event.sql = sql;
        event.result = EntitiesFoundEvent.RESULT.FAILURE.name();
        event.exception = ExceptionFun.findUltimateCause(e)
                                      .toString();

        throw e;
      } finally {
        if (event.shouldCommit()) {
          event.label = label;
          event.commit();
        }
      }
    } else {
      return op.call();
    }
  }

  /**
   * Wraps the provided operation with JFR events if enabled.
   *
   * @param op        The operation to wrap.
   * @param sql       The SQL statement associated with the operation.
   * @param enableJFR Indicates whether to enable JFR events.
   * @param label     The label to identify the insert statement
   * @return The result of the operation.
   * @throws Exception If an exception occurs during the operation.
   */
  static <O> O decorateInsertOneStm(Callable<O> op,
                                    String sql,
                                    boolean enableJFR,
                                    String label) throws Exception {
    if (enableJFR) {
      UpdateStmExecutedEvent event = new UpdateStmExecutedEvent();
      event.begin();
      try {
        var result = op.call();
        event.end();
        event.rowsAffected = 1;
        event.result = UpdateStmExecutedEvent.RESULT.SUCCESS.name();
        return result;
      } catch (Exception e) {
        event.end();
        event.sql = sql;
        event.result = UpdateStmExecutedEvent.RESULT.FAILURE.name();
        event.exception = ExceptionFun.findUltimateCause(e)
                                      .toString();
        throw e;
      } finally {
        if (event.shouldCommit()) {
          event.label = label;
          event.commit();
        }
      }
    } else {
      return op.call();
    }
  }

  /**
   * Wraps the provided operation with JFR events if enabled.
   *
   * @param <O>       The type of the operation result.
   * @param op        The operation to wrap.
   * @param sql       The SQL statement associated with the operation.
   * @param enableJFR Indicates whether to enable JFR events.
   * @param label     The label to identify the query
   * @param fetchSize the fetchSize used to fetch record from the DB and load them into the ResultSet
   * @return The result of the operation.
   * @throws Exception If an exception occurs during the operation.
   */
  static <O> List<O> decorateQueryStm(Callable<List<O>> op,
                                      String sql,
                                      boolean enableJFR,
                                      String label,
                                      int fetchSize) throws Exception {
    if (enableJFR) {
      EntitiesFoundEvent event = new EntitiesFoundEvent();
      event.begin();
      try {
        var result = op.call();
        event.end();
        if (event.shouldCommit()) {
          event.label = label;
          event.fetchSize = fetchSize;
          event.result = EntitiesFoundEvent.RESULT.SUCCESS.name();
          event.rowsReturned = result.size();
          event.commit();
        }
        return result;
      } catch (Exception e) {
        event.end();
        if (event.shouldCommit()) {
          event.label = label;
          event.fetchSize = fetchSize;
          event.sql = sql;
          event.result = EntitiesFoundEvent.RESULT.FAILURE.name();
          event.exception = ExceptionFun.findUltimateCause(e)
                                        .toString();
          event.commit();
        }
        throw e;
      }
    } else {
      return op.call();
    }
  }

  /**
   * Wraps the provided operation with JFR events if enabled.
   *
   * @param op        The operation to wrap.
   * @param sql       The SQL statement associated with the operation.
   * @param enableJFR Indicates whether to enable JFR events.
   * @param <O>       The type of the operation result.
   * @param label     The label to identify the statement
   * @return The result of the operation.
   * @throws Exception If an exception occurs during the operation.
   */
  static <O> O decorateQueryOneStm(Callable<O> op,
                                   String sql,
                                   boolean enableJFR,
                                   String label) throws Exception {
    if (enableJFR) {
      EntitiesFoundEvent event = new EntitiesFoundEvent();
      event.begin();
      try {
        var result = op.call();
        event.end();
        if (event.shouldCommit()) {
          event.label = label;
          event.fetchSize = 1;
          event.result = EntitiesFoundEvent.RESULT.SUCCESS.name();
          event.rowsReturned = result == null ? 0 : 1;
          event.commit();
        }

        return result;
      } catch (Exception e) {
        event.end();
        if (event.shouldCommit()) {
          event.label = label;
          event.fetchSize = 1;
          event.sql = sql;
          event.result = EntitiesFoundEvent.RESULT.FAILURE.name();
          event.exception = ExceptionFun.findUltimateCause(e)
                                        .toString();
          event.commit();
        }
        throw e;
      }
    } else {
      return op.call();
    }
  }

  /**
   * Wraps the provided batch operation with JFR events if enabled.
   *
   * @param op        The batch to wrap.
   * @param sql       The SQL statement associated with the operation.
   * @param enableJFR Indicates whether to enable JFR events.
   * @param label     The label to identify the statement
   * @return The result of the operation.
   * @throws Exception If an exception occurs during the operation.
   */
  static BatchResult decorateBatch(Callable<BatchResult> op,
                                   String sql,
                                   boolean enableJFR,
                                   String label) throws Exception {
    if (enableJFR) {
      BatchExecutedEvent event = new BatchExecutedEvent();
      event.begin();
      try {
        var result = op.call();
        event.end();
        if (event.shouldCommit()) {
          event.label = label;
          switch (result) {
            case BatchSuccess success -> {
              event.rowsAffected = success.rowsAffected();
              event.result = BatchExecutedEvent.RESULT.SUCCESS.name();
            }
            case BatchPartialSuccess partialSuccess -> {
              event.batchSize = partialSuccess.batchSize();
              event.totalStms = partialSuccess.totalStms();
              event.executedBatches = partialSuccess.executedBatches();
              event.sql = sql;
              event.result = BatchExecutedEvent.RESULT.PARTIAL_SUCCESS.name();
              event.exception = partialSuccess.errors()
                                              .stream()
                                              .map(ExceptionFun::findUltimateCause)
                                              .map(Throwable::toString)
                                              .collect(Collectors.joining());
            }
            case BatchFailure failure -> {
              event.batchSize = failure.batchSize();
              event.totalStms = failure.totalStms();
              event.executedBatches = failure.executedBatches();
              event.sql = sql;
              event.result = BatchExecutedEvent.RESULT.FAILURE.name();
              event.exception = ExceptionFun.findUltimateCause(failure.error())
                                            .toString();
            }
          }

          event.commit();

        }
        return result;

      } catch (Exception e) {
        event.end();
        if (event.shouldCommit()) {
          event.label = label;
          event.sql = sql;
          event.result = BatchExecutedEvent.RESULT.FAILURE.name();
          event.exception = ExceptionFun.findUltimateCause(e)
                                        .toString();
          event.commit();
        }
        throw e;
      }
    } else {
      return op.call();
    }
  }

  static <O> IO<O> decorateTx(IO<O> tx,
                              String label,
                              boolean enableJFR) {
    if (enableJFR) {
      return IO.lazy(() -> {
        var event = new TxExecutedEvent();
        event.begin();
        return event;
      })
               .then(event -> tx.then(txResult -> {
                 event.end();
                 if (event.shouldCommit()) {
                   event.label = label;
                   event.result = RESULT.SUCCESS.name();
                   event.commit();
                 }
                 return IO.succeed(txResult);
               },
                                      exc -> {
                                        event.end();
                                        if (event.shouldCommit()) {
                                          event.label = label;
                                          event.result = RESULT.FAILURE.name();
                                          event.exception = ExceptionFun.findUltimateCause(exc)
                                                                        .toString();
                                          event.commit();
                                        }
                                        return IO.fail(exc);
                                      }));
    } else {
      return tx;
    }
  }

  static IO<TxResult> decorateTxWithSavePoints(IO<TxResult> tx,
                                               String label,
                                               boolean enableJFR) {
    if (enableJFR) {
      return IO.lazy(() -> {
        var event = new TxExecutedEvent();
        event.begin();
        return event;
      })
               .then(event -> tx.then(txResult -> {
                 event.end();
                 if (event.shouldCommit()) {
                   event.label = label;
                   if (txResult instanceof TxPartialSuccess partialSuccess) {
                     event.savePoint = partialSuccess.savePointName();
                     event.exception = ExceptionFun.findUltimateCause(partialSuccess.cause())
                                                   .toString();
                     event.result = RESULT.PARTIAL_SUCCESS.name();

                   } else {
                     event.result = RESULT.SUCCESS.name();
                   }

                   event.commit();
                 }
                 return IO.succeed(txResult);
               },
                                      exc -> {
                                        event.end();
                                        if (event.shouldCommit()) {
                                          event.label = label;
                                          event.result = RESULT.SUCCESS.name();
                                          event.exception = ExceptionFun.findUltimateCause(exc)
                                                                        .toString();
                                          event.commit();
                                          ;
                                        }
                                        return IO.fail(exc);
                                      }));
    } else {
      return tx;
    }
  }

}
