package jio.test.junit;

import java.time.ZoneOffset;
import jdk.jfr.consumer.RecordedEvent;
import jio.test.Utils;
import jio.time.Fun;

import java.time.format.DateTimeFormatter;
import java.util.function.Consumer;

@SuppressWarnings("InlineFormatString")
final class DatabaseQueryStmDebugger implements Consumer<RecordedEvent> {

  private static final String FORMAT_SUC = """
      ------ jdbc-client query -----
      |  Label: %s
      |  Result: %s
      |  Duration: %s
      |  Fetch Size: %s
      |  Rows Returned: %s
      |  Op Counter: %s
      |  Thread: %s
      |  Event Start Time: %s
      ----------------------
      """;

  private static final String FORMAT_ERR = """
      ------ jdbc-client query -----
      |  Label: %s
      |  Result: %s
      |  Duration: %s
      |  Exception: %s
      |  SQL: %s
      |  Fetch Size: %s
      |  Rows Returned: %s
      |  Op Counter: %s
      |  Thread: %s
      |  Event Start Time: %s
      ----------------------
      """;
  static final String EVENT_NAME = "jio.jdbc.QueryStm";

  @Override
  public void accept(RecordedEvent event) {
    assert EVENT_NAME.equals(event.getEventType()
                                  .getName());

    var result = event.getValue(EventFields.RESULT);
    var label = event.getValue(EventFields.LABEL);
    var fetchSize = event.getValue(EventFields.FETCH_SIZE);
    boolean isSuccess = "SUCCESS".equals(result);
    var message = isSuccess ? String.format(FORMAT_SUC,
                                            label,
                                            event.getValue(EventFields.RESULT),
                                            Fun.formatTime(event.getDuration()
                                                                .toNanos()),
                                            fetchSize,
                                            event.getValue(EventFields.ROW_RETURNED),
                                            event.getValue("queryCounter"),
                                            Utils.getThreadName(event.getThread()),
                                            event.getStartTime()
                                                 .atZone(ZoneOffset.UTC)
                                                 .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
    ) : String.format(FORMAT_ERR,
                      label,
                      event.getValue(EventFields.RESULT),
                      Fun.formatTime(event.getDuration()
                                          .toNanos()),
                      event.getValue(EventFields.EXCEPTION),
                      event.getValue(EventFields.SQL),
                      fetchSize,
                      event.getValue(EventFields.ROW_RETURNED),
                      event.getValue("queryCounter"),
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
