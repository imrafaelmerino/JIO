package jio.jdbc;

import java.util.function.Function;
import jdk.jfr.consumer.RecordedEvent;
import jio.time.Fun;

/**
 * A formatter for converting Java Flight Recorder (JFR) RecordedEvents related to JDBC update statements into
 * human-readable strings. This formatter provides methods for formatting both successful and failed events. It is
 * designed to work specifically with events of type {@code jio.jdbc.UpdateStm}.
 * <p>
 * The formatting includes information such as start time, label, result, rows affected, duration, SQL statement, and
 * update counter. The formatted strings are intended to be human-readable and can be used for logging or other
 * diagnostic purposes.
 * <p>
 * This class is implemented as a singleton, and the singleton instance is available as {@link #INSTANCE}. You can use
 * this instance to format {@code RecordedEvent} instances by calling the {@link #apply(RecordedEvent)} method.
 * <p>
 * The formatted output for a successful event is:
 * "{@code %s; db-stm; label: %s; result: %s; rows_affected: %s, duration: %s; update-counter: %s}".
 * <p>
 * The formatted output for an event with an exception is:
 * "{@code %s; db-stm; label: %s; result: %s; exception: %s; duration: %s; sql: %s; update-counter: %s}".
 */
public final class UpdateStmExecutedEventFormatter implements Function<RecordedEvent, String> {

  /**
   * The singleton instance of UpdateStmEventFormatter.
   */
  public static final UpdateStmExecutedEventFormatter INSTANCE = new UpdateStmExecutedEventFormatter();
  private static final String EVENT_LABEL = "jio.jdbc.UpdateStm";
  private static final String SUCCESS_FORMAT = """
      db-stm; label: %s; result: %s; rows_affected: %s, \
      duration: %s; op-counter: %s; start_time: %s""";
  private static final String FAILURE_FORMAT = """
      db-stm; label: %s; result: %s; \
      exception: %s; duration: %s; sql: %s; \
      op-counter: %s; start_time: %s""";

  private UpdateStmExecutedEventFormatter() {

  }

  /**
   * Formats a given {@code RecordedEvent} related to JDBC update statements into a human-readable string. The
   * formatting includes information such as start time, label, result, rows affected, duration, SQL statement, and
   * update counter. The formatted string is intended to be human-readable and can be used for logging or other
   * diagnostic purposes.
   *
   * @param event The {@code RecordedEvent} instance to be formatted.
   * @return A human-readable string representing the formatted information of the JDBC update statement event.
   */
  @Override
  public String apply(RecordedEvent event) {
    assert EVENT_LABEL.equals(event.getEventType()
                                   .getName());
    var result = event.getValue(UpdateStmExecutedEvent.RESULT_FIELD);
    var label = event.getValue(UpdateStmExecutedEvent.LABEL_FIELD);
    boolean isSuccess = UpdateStmExecutedEvent.RESULT.SUCCESS.name()
                                                             .equals(result);
    return isSuccess ? String.format(SUCCESS_FORMAT,
                                     label,
                                     result,
                                     event.getValue(UpdateStmExecutedEvent.ROWS_AFFECTED_FIELD),
                                     Fun.formatTime(event.getDuration()),
                                     event.getValue(UpdateStmExecutedEvent.UPDATE_COUNTER_FIELD),
                                     event.getStartTime()
                                    ) :
           String.format(FAILURE_FORMAT,
                         label,
                         result,
                         event.getValue(UpdateStmExecutedEvent.EXCEPTION_FIELD),
                         Fun.formatTime(event.getDuration()),
                         event.getValue(UpdateStmExecutedEvent.SQL_FIELD),
                         event.getValue(UpdateStmExecutedEvent.UPDATE_COUNTER_FIELD),
                         event.getStartTime()

                        );
  }
}
