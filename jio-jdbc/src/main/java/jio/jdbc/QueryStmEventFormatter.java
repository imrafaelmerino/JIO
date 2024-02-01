package jio.jdbc;

import jdk.jfr.consumer.RecordedEvent;

import java.util.function.Function;
import jio.time.Fun;

/**
 * A class that converts Java Flight Recorder (JFR) RecordedEvents to formatted strings. This class is intended to be
 * used as a Function for transforming RecordedEvents into human-readable strings.
 *
 * <p>
 * The formatting includes information such as the result, duration, SQL statement, and counter.
 * </p>
 *
 * <p>
 * The formatted output for a successful event is: "result: %s, duration: %s, sql: %s, counter: %s".
 * </p>
 *
 * <p>
 * The formatted output for an event with an exception is: "result: %s, duration: %s, exception: %s, sql: %s, counter:
 * %s".
 * </p>
 *
 * <p>
 * Note: This class is designed to work with the JFR events persisted by jio-jdbc. Since it's just a function you can
 * define your own formatters
 * </p>
 */
public final class QueryStmEventFormatter implements Function<RecordedEvent, String> {

  /**
   * The singleton instance of JdbcEventFormatter.
   */
  public static final QueryStmEventFormatter INSTANCE = new QueryStmEventFormatter();
  private static final String EVENT_LABEL = "jio.jdbc.QueryStm";
  private static final String SUCCESS_FORMAT = """
      event: db query; label: %s; result: %s; rows_returned: %s;
      duration: %s; fetch_size: %s; op-counter: %s;
      start_time: %s""".replace("\n",
                                " ");
  private static final String FAILURE_FORMAT = """
      event: db query; label: %s; result: %s;
      exception: %s; duration: %s; fetch_size: %s;
      sql: %s; op-counter: %s; start_time: %s""".replace("\n",
                                                         " ");

  /**
   * Constructs a JdbcEventFormatter with the default identity function for SQL statements.
   */
  private QueryStmEventFormatter() {

  }

  /**
   * Converts a RecordedEvent to a formatted string.
   *
   * @param event The RecordedEvent to be converted.
   * @return A formatted string representing the information from the RecordedEvent.
   */
  @Override
  public String apply(RecordedEvent event) {
    assert EVENT_LABEL.equals(event.getEventType()
                                   .getName());
    var result = event.getValue(StmEvent.RESULT_FIELD);
    var label = event.getValue(StmEvent.LABEL_FIELD);
    var fetchSize = event.getValue(QueryStmEvent.FETCH_SIZE_FIELD);
    boolean isSuccess = StmEvent.RESULT.SUCCESS.name()
                                               .equals(result);
    return isSuccess ?
           String.format(SUCCESS_FORMAT,
                         label,
                         result,
                         event.getValue(QueryStmEvent.ROWS_RETURNED_FIELD),
                         Fun.formatTime(event.getDuration()),
                         fetchSize,
                         event.getValue(QueryStmEvent.OP_COUNTER_FIELD),
                         event.getStartTime()
                        ) :
           String.format(FAILURE_FORMAT,
                         label,
                         result,
                         event.getValue(StmEvent.EXCEPTION_FIELD),
                         Fun.formatTime(event.getDuration()),
                         fetchSize,
                         event.getValue(QueryStmEvent.SQL_FIELD),
                         event.getValue(QueryStmEvent.OP_COUNTER_FIELD),
                         event.getStartTime()
                        );
  }
}
