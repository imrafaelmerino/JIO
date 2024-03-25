package jio.test.junit;

import java.time.ZoneOffset;
import jdk.jfr.consumer.RecordedEvent;
import jio.test.Utils;
import jio.time.Fun;

import java.time.format.DateTimeFormatter;
import java.util.function.Consumer;

@SuppressWarnings("InlineFormatString")
final class DatabaseBatchDebugger implements Consumer<RecordedEvent> {

  private static final String FORMAT_SUC = """
      ------ jdbc-client batch -----
      |  Label: %s
      |  Result: %s
      |  Duration: %s
      |  Rows Affected: %s
      |  Executed Batches: %s
      |  Batch Size: %s
      |  Statements Size: %s
      |  Op Counter: %s
      |  Thread: %s
      |  Event Start Time: %s
      -------------------------------
      """;

  private static final String FAILURE_OR_PARTIAL_SUCCESS_FORMAT = """
      ------ jdbc-client batch -----
      |  Label: %s
      |  Result: %s
      |  Duration: %s
      |  Rows Affected: %s
      |  Executed Batches: %s
      |  Batch Size: %s
      |  Statements Size: %s
      |  SQL: %s
      |  Exception: %s
      |  Op Counter: %s
      |  Thread: %s
      |  Event Start Time: %s
      -------------------------------
      """;
  static final String EVENT_NAME = "jio.jdbc.BatchStm";

  @Override
  public void accept(RecordedEvent event) {
    assert event.getEventType()
                .getName()
                .equals(EVENT_NAME);
    var result = event.getValue(EventFields.RESULT);
    var label = event.getValue(EventFields.LABEL);
    boolean isSuccess = "SUCCESS".equals(result);
    var message = isSuccess ? String.format(FORMAT_SUC,
                                            label,
                                            event.getValue(EventFields.RESULT),
                                            Fun.formatTime(event.getDuration()),
                                            event.getValue(EventFields.ROWS_AFFECTED),
                                            event.getValue(EventFields.EXECUTED_BATCHES),
                                            event.getValue(EventFields.BATCH_SIZE),
                                            event.getValue(EventFields.TOTAL_STMS),
                                            event.getValue("batchCounter"),
                                            Utils.getThreadName(event.getThread()),
                                            event.getStartTime()
                                                 .atZone(ZoneOffset.UTC)
                                                 .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
    ) : String.format(FAILURE_OR_PARTIAL_SUCCESS_FORMAT,
                      label,
                      event.getValue(EventFields.RESULT),
                      Fun.formatTime(event.getDuration()),
                      event.getValue(EventFields.ROWS_AFFECTED),
                      event.getValue(EventFields.EXECUTED_BATCHES),
                      event.getValue(EventFields.BATCH_SIZE),
                      event.getValue(EventFields.TOTAL_STMS),
                      event.getValue(EventFields.SQL),
                      event.getValue(EventFields.EXCEPTION),
                      event.getValue("batchCounter"),
                      Utils.getThreadName(event.getThread()),
                      event.getStartTime()
                           .atZone(ZoneOffset.UTC)
                           .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
    );
    synchronized (System.out) {
      System.out.println(message);
      System.out.flush();
    }
  }
}
