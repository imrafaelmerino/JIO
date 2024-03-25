package jio.test.junit;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.function.Consumer;
import jdk.jfr.consumer.RecordedEvent;
import jio.test.Utils;
import jio.time.Fun;

@SuppressWarnings("InlineFormatString")
final class DatabaseTxDebugger implements Consumer<RecordedEvent> {

  private static final String FORMAT_SUC = """
      ------ jdbc-client transaction -----
      |  Label: %s
      |  Result: %s
      |  Duration: %s
      |  Op Counter: %s
      |  Thread: %s
      |  Event Start Time: %s
      ----------------------
      """;

  private static final String FAILURE_OR_PARTIAL_SUCCESS_FORMAT = """
      ------ jdbc-client transaction -----
      |  Label: %s
      |  Result: %s
      |  Duration: %s
      |  Exception: %s
      |  Op Counter: %s
      |  Thread: %s
      |  Event Start Time: %s
      ----------------------
      """;
  static final String EVENT_NAME = "jio.jdbc.Tx";

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
                                            event.getValue("txCounter"),
                                            Utils.getThreadName(event.getThread()),
                                            event.getStartTime()
                                                 .atZone(ZoneId.systemDefault())
                                                 .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
    ) : String.format(FAILURE_OR_PARTIAL_SUCCESS_FORMAT,
                      label,
                      event.getValue(EventFields.RESULT),
                      Fun.formatTime(event.getDuration()
                                          .toNanos()),
                      event.getValue(EventFields.EXCEPTION),
                      event.getValue("txCounter"),
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
