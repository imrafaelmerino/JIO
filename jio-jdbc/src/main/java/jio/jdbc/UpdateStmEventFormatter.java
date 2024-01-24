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
public final class UpdateStmEventFormatter implements Function<RecordedEvent, String> {

  /**
   * The singleton instance of JdbcEventFormatter.
   */
  public static final UpdateStmEventFormatter INSTANCE = new UpdateStmEventFormatter();
  private static final String EVENT_LABEL = "jio.jdbc.UpdateStm";
  private static final String SUCCESS_FORMAT = """
      event: db stm; label: %s; result: %s; rows_affected: %s,
      duration: %s; op-counter: %s""".replace("\n",
                                              " ");
  private static final String FAILURE_FORMAT = """
      event: db stm; label: %s; result: %s;
      exception: %s; duration: %s; sql: %s;
      op-counter: %s""".replace("\n",
                                " ");

  /**
   * Constructs a JdbcEventFormatter with the default identity function for SQL statements.
   */
  private UpdateStmEventFormatter() {

  }

  /**
   * Converts a RecordedEvent to a formatted string.
   *
   * @param e The RecordedEvent to be converted.
   * @return A formatted string representing the information from the RecordedEvent.
   */
  @Override
  public String apply(RecordedEvent e) {
    assert EVENT_LABEL.equals(e.getEventType()
                               .getName());
    var result = e.getValue(StmEvent.RESULT_FIELD);
    var label = e.getValue(StmEvent.LABEL_FIELD);
    boolean isSuccess = StmEvent.RESULT.SUCCESS.name()
                                               .equals(result);
    return isSuccess ?
           String.format(SUCCESS_FORMAT,
                         label,
                         result,
                         e.getValue(UpdateStmEvent.ROWS_AFFECTED_FIELD),
                         Fun.formatTime(e.getDuration()),
                         e.getValue(UpdateStmEvent.OP_COUNTER_FIELD)
                        ) :
           String.format(FAILURE_FORMAT,
                         label,
                         result,
                         e.getValue(StmEvent.EXCEPTION_FIELD),
                         Fun.formatTime(e.getDuration()),
                         e.getValue(UpdateStmEvent.SQL_FIELD),
                         e.getValue(UpdateStmEvent.OP_COUNTER_FIELD)
                        );
  }
}
