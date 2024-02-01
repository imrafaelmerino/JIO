package jio.jdbc;

import jdk.jfr.consumer.RecordedEvent;

import java.util.function.Function;
import jio.time.Fun;

/**
 * A class that converts Java Flight Recorder (JFR) RecordedEvents to formatted strings. This class is intended to be
 * used as a Function for transforming RecordedEvents into human-readable strings.
 *
 * <p>
 * The formatting includes information such as the result, duration,  and counter.
 * </p>
 *
 * <p>
 * The formatted output for a successful event is: "result: %s, duration: %s,  counter: %s".
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
public final class BatchEventFormatter implements Function<RecordedEvent, String> {

  /**
   * The singleton instance of JdbcEventFormatter.
   */
  public static final BatchEventFormatter INSTANCE = new BatchEventFormatter();
  private static final String EVENT_LABEL = "jio.jdbc.BatchStm";
  private static final String SUCCESS_FORMAT = """
      event: db batch; label: %s; result: %s; duration: %s;
      rows_affected: %s; op-counter: %s;
      start_time: %s""".replace("\n",
                                " ");
  private static final String FAILURE_FORMAT = """
      event: db batch; label: %s; result: %s; duration: %s;
      rows_affected: %s; executed_batches:%s; batch_size: %s;
      stms_size: %s; sql: %s; exception: %s;
      op-counter: %s; start_time: %s""".replace("\n",
                                                " ");


  /**
   * Constructs a JdbcEventFormatter with the default identity function for SQL statements.
   */
  private BatchEventFormatter() {

  }

  /**
   * Converts a RecordedEvent to a formatted string.
   *
   * @param event The RecordedEvent to be converted.
   * @return A formatted string representing the information from the RecordedEvent.
   */
  @Override
  public String apply(RecordedEvent event) {
    assert event.getEventType()
                .getName()
                .equals(EVENT_LABEL);
    var label = event.getValue(StmEvent.LABEL_FIELD);
    var result = event.getValue(StmEvent.RESULT_FIELD);
    boolean isSuccess = StmEvent.RESULT.SUCCESS.name()
                                               .equals(result);
    return isSuccess ?
           String.format(SUCCESS_FORMAT,
                         label,
                         result,
                         Fun.formatTime(event.getDuration()),
                         event.getValue(BatchEvent.ROWS_AFFECTED_FIELD),
                         event.getValue(QueryStmEvent.OP_COUNTER_FIELD),
                         event.getStartTime()
                        ) :
           String.format(FAILURE_FORMAT,
                         label,
                         result,
                         Fun.formatTime(event.getDuration()),
                         event.getValue(BatchEvent.ROWS_AFFECTED_FIELD),
                         event.getValue(BatchEvent.EXECUTED_BATCHES_FIELD),
                         event.getValue(BatchEvent.BATCH_SIZE_FIELD),
                         event.getValue(BatchEvent.STM_SIZE_FIELD),
                         event.getValue(BatchEvent.SQL_FIELD),
                         event.getValue(StmEvent.EXCEPTION_FIELD),
                         event.getValue(QueryStmEvent.OP_COUNTER_FIELD),
                         event.getStartTime()
                        );
  }
}
