package jio.jdbc;

import java.util.function.Function;
import jdk.jfr.consumer.RecordedEvent;
import jio.time.Fun;

/**
 * A formatter for converting Java Flight Recorder (JFR) RecordedEvents related to JDBC batch statements into
 * human-readable strings. This formatter provides methods for formatting both successful and failed events. It is
 * designed to work specifically with events of type {@code jio.jdbc.BatchStm}.
 * <p>
 * The formatting includes information such as start time, label, result, duration, rows affected, executed batches,
 * batch size, statement size, SQL statement, exception (if any), and batch counter. The formatted strings are intended
 * to be human-readable and can be used for logging or other diagnostic purposes.
 * <p>
 * This class is implemented as a singleton, and the singleton instance is available as {@link #INSTANCE}. You can use
 * this instance to format {@code RecordedEvent} instances by calling the {@link #apply(RecordedEvent)} method.
 * <p>
 * The formatted output for a successful event is:
 * "{@code %s; db-batch; label: %s; result: %s; duration: %s; rows_affected: %s; batch-counter: %s}".
 * <p>
 * The formatted output for an event with an exception or partial success is:
 * "{@code %s; db-batch; label: %s; result: %s; duration: %s; rows_affected: %s; executed_batches:%s; batch_size: %s;
 * stms_size: %s; sql: %s; exception: %s; batch-counter: %s}".
 */
public final class BatchExecutedEventFormatter implements Function<RecordedEvent, String> {

  /**
   * The singleton instance of BatchEventFormatter.
   */
  public static final BatchExecutedEventFormatter INSTANCE = new BatchExecutedEventFormatter();
  private static final String EVENT_LABEL = "jio.jdbc.BatchStm";
  private static final String SUCCESS_FORMAT = """
      db-batch; label: %s; result: %s; duration: %s; \
      rows_affected: %s; op-counter: %s; start_time: %s""";
  private static final String FAILURE_OR_PARTIAL_SUCCESS_FORMAT = """
      db-batch; label: %s; result: %s; duration: %s; \
      rows_affected: %s; executed_batches:%s; batch_size: %s; \
      stms_size: %s; sql: %s; exception: %s; \
      op-counter: %s; start_time: %s""";

  private BatchExecutedEventFormatter() {

  }

  /**
   * Formats a given {@code RecordedEvent} related to JDBC batch statements into a human-readable string. The formatting
   * includes information such as start time, label, result, duration, rows affected, executed batches, batch size,
   * statement size, SQL statement, exception (if any), and batch counter. The formatted string is intended to be
   * human-readable and can be used for logging or other diagnostic purposes.
   *
   * @param event The {@code RecordedEvent} instance to be formatted.
   * @return A human-readable string representing the formatted information of the JDBC batch statement event.
   */

  @Override
  public String apply(RecordedEvent event) {
    assert event.getEventType()
                .getName()
                .equals(EVENT_LABEL);
    var label = event.getValue(BatchExecutedEvent.LABEL_FIELD);
    var result = event.getValue(BatchExecutedEvent.RESULT_FIELD);
    boolean isSuccess = BatchExecutedEvent.RESULT.SUCCESS.name()
                                                         .equals(result);
    return isSuccess ? String.format(SUCCESS_FORMAT,
                                     label,
                                     result,
                                     Fun.formatTime(event.getDuration()),
                                     event.getValue(BatchExecutedEvent.ROWS_AFFECTED_FIELD),
                                     event.getValue(BatchExecutedEvent.BATCH_COUNTER_FIELD),
                                     event.getStartTime()) :
           String.format(FAILURE_OR_PARTIAL_SUCCESS_FORMAT,
                         label,
                         result,
                         Fun.formatTime(event.getDuration()),
                         event.getValue(BatchExecutedEvent.ROWS_AFFECTED_FIELD),
                         event.getValue(BatchExecutedEvent.EXECUTED_BATCHES_FIELD),
                         event.getValue(BatchExecutedEvent.BATCH_SIZE_FIELD),
                         event.getValue(BatchExecutedEvent.STM_SIZE_FIELD),
                         event.getValue(BatchExecutedEvent.SQL_FIELD),
                         event.getValue(BatchExecutedEvent.EXCEPTION_FIELD),
                         event.getValue(BatchExecutedEvent.BATCH_COUNTER_FIELD),
                         event.getStartTime()
                        );
  }
}
