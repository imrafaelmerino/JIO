package jio.jdbc;

import java.util.function.Function;
import jdk.jfr.consumer.RecordedEvent;
import jio.time.Fun;

/**
 * A formatter for converting Java Flight Recorder (JFR) RecordedEvents related to JDBC query statements into
 * human-readable strings. This formatter provides methods for formatting both successful and failed events. It is
 * designed to work specifically with events of type {@code jio.jdbc.QueryStm}.
 * <p>
 * The formatting includes information such as start time, label, result, rows returned, duration, fetch size, SQL
 * statement, and query counter. The formatted strings are intended to be human-readable and can be used for logging or
 * other diagnostic purposes.
 * <p>
 * This class is implemented as a singleton, and the singleton instance is available as {@link #INSTANCE}. You can use
 * this instance to format {@code RecordedEvent} instances by calling the {@link #apply(RecordedEvent)} method.
 * <p>
 * The formatted output for a successful event is:
 * "{@code %s; db-query; label: %s; result: %s; rows_returned: %s; duration: %s; fetch_size: %s; query-counter: %s}".
 * <p>
 * The formatted output for an event with an exception is:
 * "{@code %s; db-query; label: %s; result: %s; exception: %s; duration: %s; fetch_size: %s; sql: %s; query-counter:
 * %s}".
 */
public final class EntitiesFoundEventFormatter implements Function<RecordedEvent, String> {

  /**
   * The singleton instance of QueryStmEventFormatter.
   */
  public static final EntitiesFoundEventFormatter INSTANCE = new EntitiesFoundEventFormatter();
  private static final String EVENT_LABEL = "jio.jdbc.QueryStm";
  private static final String SUCCESS_FORMAT = """
      db-query; label: %s; result: %s; rows_returned: %s; \
      duration: %s; fetch_size: %s; op-counter: %s; start_time: %s""";
  private static final String FAILURE_FORMAT = """
      db-query; label: %s; result: %s; \
      exception: %s; duration: %s; fetch_size: %s; \
      sql: %s; op-counter: %s; start_time: %s""";

  private EntitiesFoundEventFormatter() {

  }

  /**
   * Formats a given {@code RecordedEvent} related to JDBC query statements into a human-readable string. The formatting
   * includes information such as start time, label, result, rows returned, duration, fetch size, SQL statement, and
   * query counter. The formatted string is intended to be human-readable and can be used for logging or other
   * diagnostic purposes.
   *
   * @param event The {@code RecordedEvent} instance to be formatted.
   * @return A human-readable string representing the formatted information of the JDBC query statement event.
   */
  @Override
  public String apply(RecordedEvent event) {
    assert EVENT_LABEL.equals(event.getEventType()
                                   .getName());
    var result = event.getValue(EntitiesFoundEvent.RESULT_FIELD);
    var label = event.getValue(EntitiesFoundEvent.LABEL_FIELD);
    var fetchSize = event.getValue(EntitiesFoundEvent.FETCH_SIZE_FIELD);
    boolean isSuccess = EntitiesFoundEvent.RESULT.SUCCESS.name()
                                                         .equals(result);
    return isSuccess ? String.format(SUCCESS_FORMAT,
                                     label,
                                     result,
                                     event.getValue(EntitiesFoundEvent.ROWS_RETURNED_FIELD),
                                     Fun.formatTime(event.getDuration()),
                                     fetchSize,
                                     event.getValue(EntitiesFoundEvent.QUERY_COUNTER_FIELD),
                                     event.getStartTime()
                                    ) :
           String.format(FAILURE_FORMAT,
                         label,
                         result,
                         event.getValue(EntitiesFoundEvent.EXCEPTION_FIELD),
                         Fun.formatTime(event.getDuration()),
                         fetchSize,
                         event.getValue(EntitiesFoundEvent.SQL_FIELD),
                         event.getValue(EntitiesFoundEvent.QUERY_COUNTER_FIELD),
                         event.getStartTime()
                        );
  }
}
