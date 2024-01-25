package jio.jdbc;

import java.util.List;
import jio.ExceptionFun;

import java.util.concurrent.Callable;
import java.util.stream.Collectors;

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
                               String label)
      throws Exception {
    UpdateStmEvent event = null;
    if (enableJFR) {
      event = new UpdateStmEvent();
      event.begin();
      event.label = label;
    }
    try {
      var n = op.call();
      if (enableJFR) {
        event.rowsAffected = n;
        event.result = QueryStmEvent.RESULT.SUCCESS.name();
      }
      return n;

    } catch (Exception e) {
      if (enableJFR) {
        event.sql = sql;
        event.result = QueryStmEvent.RESULT.FAILURE.name();
        event.exception = ExceptionFun.findUltimateCause(e)
                                      .toString();

      }
      throw e;
    } finally {
      if (enableJFR) {
        event.commit();
      }
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
                                    String label)
      throws Exception {
    UpdateStmEvent event = null;
    if (enableJFR) {
      event = new UpdateStmEvent();
      event.begin();
      event.label = label;
    }
    try {
      var result = op.call();
      if (enableJFR) {
        event.rowsAffected = 1;
        event.result = QueryStmEvent.RESULT.SUCCESS.name();
      }
      return result;

    } catch (Exception e) {
      if (enableJFR) {
        event.sql = sql;
        event.result = QueryStmEvent.RESULT.FAILURE.name();
        event.exception = ExceptionFun.findUltimateCause(e)
                                      .toString();

      }
      throw e;
    } finally {
      if (enableJFR) {
        event.commit();
      }
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
                                      int fetchSize)
      throws Exception {
    QueryStmEvent event = null;
    if (enableJFR) {
      event = new QueryStmEvent();
      event.begin();
      event.label = label;
      event.fetchSize = fetchSize;
    }
    try {
      var result = op.call();
      if (enableJFR) {
        event.result = QueryStmEvent.RESULT.SUCCESS.name();
        event.rowsReturned = result.size();
      }
      return result;

    } catch (Exception e) {
      if (enableJFR) {
        event.sql = sql;
        event.result = QueryStmEvent.RESULT.FAILURE.name();
        event.exception = ExceptionFun.findUltimateCause(e)
                                      .toString();

      }
      throw e;
    } finally {
      if (enableJFR) {
        event.commit();
      }
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
    QueryStmEvent event = null;
    if (enableJFR) {
      event = new QueryStmEvent();
      event.begin();
      event.label = label;
      event.fetchSize = 1;
    }
    try {
      var result = op.call();
      if (enableJFR) {
        event.result = QueryStmEvent.RESULT.SUCCESS.name();
        event.rowsReturned = result == null ? 0 : 1;
      }
      return result;

    } catch (Exception e) {
      if (enableJFR) {
        event.sql = sql;
        event.result = QueryStmEvent.RESULT.FAILURE.name();
        event.exception = ExceptionFun.findUltimateCause(e)
                                      .toString();

      }
      throw e;
    } finally {
      if (enableJFR) {
        event.commit();
      }
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
                                   String label)
      throws Exception {
    BatchEvent event = null;
    if (enableJFR) {
      event = new BatchEvent();
      event.begin();
      event.label = label;
    }
    try {
      var result = op.call();
      if (enableJFR) {
        event.rowsAffected = result.rowsAffected();
        event.batchSize = result.batchSize();
        event.totalStms = result.totalStms();
        event.executedBatches = result.executedBatches();
        var errors = result.errors();
        if (errors.isEmpty()) {
          event.result = BatchEvent.RESULT.SUCCESS.name();
        } else {
          event.sql = sql;
          event.result = BatchEvent.RESULT.FAILURE.name();
          event.exception = errors
              .stream()
              .map(ExceptionFun::findUltimateCause)
              .map(Throwable::toString)
              .collect(Collectors.joining());
        }
      }
      return result;

    } catch (Exception e) {
      if (enableJFR) {
        event.sql = sql;
        event.result = BatchEvent.RESULT.FAILURE.name();
        event.exception = ExceptionFun.findUltimateCause(e)
                                      .toString();

      }
      throw e;
    } finally {
      if (enableJFR) {
        event.commit();
      }
    }
  }


}
