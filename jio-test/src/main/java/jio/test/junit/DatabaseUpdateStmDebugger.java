package jio.test.junit;

import jdk.jfr.consumer.RecordedEvent;
import jio.test.Utils;
import jio.time.Fun;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.function.Consumer;

@SuppressWarnings("InlineFormatString")
final class DatabaseUpdateStmDebugger implements Consumer<RecordedEvent> {

  private static final String FORMAT_SUC = """
      ------ jdbc-client statement -----
      |  Label: %s
      |  Result: %s
      |  Duration: %s
      |  Rows Affected: %s
      |  Op Counter: %s
      |  Thread: %s
      |  Event Start Time: %s
      ----------------------
      """;

  private static final String FORMAT_ERR = """
      ------ jdbc-client update statement -----
      |  Label: %s
      |  Result: %s
      |  Duration: %s
      |  Exception: %s
      |  SQL: %s
      |  Rows Affected: %s
      |  Op Counter: %s
      |  Thread: %s
      |  Event Start Time: %s
      ----------------------
      """;
  static final String EVENT_NAME = "jio.jdbc.UpdateStm";

  @Override
  public void accept(RecordedEvent event) {
    assert EVENT_NAME.equals(event.getEventType()
                                  .getName());
    var result = event.getValue(EventFields.RESULT);
    var label = event.getValue(EventFields.LABEL);
    boolean isSuccess = "SUCCESS".equals(result);
    var message = isSuccess ? String.format(FORMAT_SUC,
                                            label,
                                            event.getValue(EventFields.RESULT),
                                            Fun.formatTime(event.getDuration()
                                                                .toNanos()),
                                            event.getValue(EventFields.ROWS_AFFECTED),
                                            event.getValue("updateCounter"),
                                            Utils.getThreadName(event.getThread()),
                                            event.getStartTime()
                                                 .atZone(ZoneId.systemDefault())
                                                 .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
    ) : String.format(FORMAT_ERR,
                      label,
                      event.getValue(EventFields.RESULT),
                      Fun.formatTime(event.getDuration()
                                          .toNanos()),
                      event.getValue(EventFields.EXCEPTION),
                      event.getValue(EventFields.SQL),
                      event.getValue(EventFields.ROWS_AFFECTED),
                      event.getValue("updateCounter"),
                      Utils.getThreadName(event.getThread()),
                      event.getStartTime()
                           .atZone(ZoneId.systemDefault())
                           .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
    );
    synchronized (System.out) {
      System.out.println(message);
      System.out.flush();
    }
  }
}
