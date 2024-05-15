package jio.jdbc;

import java.util.function.Function;
import jdk.jfr.consumer.RecordedEvent;
import jio.jdbc.TxExecutedEvent.RESULT;
import jio.time.Fun;

/**
 * A formatter for converting {@link RecordedEvent} instances related to JDBC transactions into human-readable strings.
 * This formatter provides methods for formatting success, success with savepoint, and failure events. It is designed to
 * work specifically with events of type {@code jio.jdbc.Tx}.
 * <p>
 * The formatting templates include information such as start time, label, result, duration, savepoint (if applicable),
 * exception (if applicable), and transaction counter. The formatted strings are intended to be human-readable and can
 * be used for logging or other diagnostic purposes.
 * <p>
 * This class is implemented as a singleton, and the singleton instance is available as {@link #INSTANCE}. You can use
 * this instance to format {@code RecordedEvent} instances by calling the {@link #apply(RecordedEvent)} method.
 */
public final class TxExecutedEventFormatter implements Function<RecordedEvent, String> {

  /**
   * The singleton instance of TxEventFormatter.
   */
  public static final TxExecutedEventFormatter INSTANCE = new TxExecutedEventFormatter();
  private static final String EVENT_LABEL = "jio.jdbc.Tx";
  private static final String SUCCESS_FORMAT = """
      db-tx; label: %s; result: %s; duration: %s; \
      tx-counter: %s; start_time: %s""";

  private static final String SUCCESS_WITH_SAVEPOINT_FORMAT = """
      db-tx; label: %s; result: %s; duration: %s; \
      save_point: %s; exception: %s; \
      op-counter: %s; start_time: %s""";
  private static final String FAILURE_FORMAT = """
      db-tx; label: %s; result: %s; \
      exception: %s; duration: %s;op-counter: %s; start_time: %s""";

  private TxExecutedEventFormatter() {

  }

  /**
   * Formats a given {@code RecordedEvent} related to JDBC transactions into a human-readable string. The formatting
   * templates include information such as start time, label, result, duration, savepoint (if applicable), exception (if
   * applicable), and transaction counter. The formatted string is intended to be human-readable and can be used for
   * logging or other diagnostic purposes.
   *
   * @param event The {@code RecordedEvent} instance to be formatted.
   * @return A human-readable string representing the formatted information of the JDBC transaction event.
   */
  @Override
  public String apply(RecordedEvent event) {
    assert EVENT_LABEL.equals(event.getEventType()
                                   .getName());
    var result = event.getValue(TxExecutedEvent.RESULT_FIELD);
    var label = event.getValue(TxExecutedEvent.LABEL_FIELD);
    boolean isSuccess = TxExecutedEvent.RESULT.SUCCESS.name()
                                                      .equals(result);
    boolean isSuccessWithSavePoint = RESULT.PARTIAL_SUCCESS.name()
                                                           .equals(result);

    if (isSuccess) {
      return String.format(SUCCESS_FORMAT,
                           label,
                           result,
                           Fun.formatTime(event.getDuration()),
                           event.getValue(TxExecutedEvent.TX_COUNTER_FIELD),
                           event.getStartTime()
                          );
    }
    if (isSuccessWithSavePoint) {
      return String.format(SUCCESS_WITH_SAVEPOINT_FORMAT,
                           label,
                           result,
                           Fun.formatTime(event.getDuration()),
                           event.getValue(TxExecutedEvent.SAVEPOINT_FIELD),
                           event.getValue(TxExecutedEvent.EXCEPTION_FIELD),
                           event.getValue(TxExecutedEvent.TX_COUNTER_FIELD),
                           event.getStartTime()
                          );
    }
    return String.format(FAILURE_FORMAT,
                         label,
                         result,
                         event.getValue(TxExecutedEvent.EXCEPTION_FIELD),
                         Fun.formatTime(event.getDuration()),
                         event.getValue(TxExecutedEvent.TX_COUNTER_FIELD),
                         event.getStartTime()
                        );
  }
}
