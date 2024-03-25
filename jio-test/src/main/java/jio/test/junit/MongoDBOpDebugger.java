package jio.test.junit;

import java.time.ZoneOffset;
import jdk.jfr.consumer.RecordedEvent;
import jio.test.Utils;
import jio.time.Fun;
import java.time.format.DateTimeFormatter;
import java.util.function.Consumer;

@SuppressWarnings("InlineFormatString")
final class MongoDBOpDebugger implements Consumer<RecordedEvent> {

  private static final String FORMAT_SUC = """
      ------ mongodb op -----
      |  Operation: %s
      |  Result: %s
      |  Duration: %s
      |  Op counter: %s
      |  Thread: %s
      |  Event Start Time: %s
      ----------------------
      """;

  private static final String FORMAT_ERR = """
      ------ mongodb op -----
      |  Operation: %s
      |  Result: %s
      |  Duration: %s
      |  Op counter: %s
      |  Exception: %s
      |  Thread: %s
      |  Event Start Time: %s
      ----------------------
      """;
  static final String EVENT_NAME = "jio.mongodb.Op";

  @Override
  public void accept(RecordedEvent event) {
    assert EVENT_NAME.equals(event.getEventType()
                                  .getName());
    var result = event.getValue(EventFields.RESULT);
    boolean isSuccess = "SUCCESS".equals(result);
    var str = isSuccess ? String.format(FORMAT_SUC,
                                        event.getValue(EventFields.OPERATION),
                                        result,
                                        Fun.formatTime(event.getDuration()),
                                        event.getValue(EventFields.OPERATION_COUNTER),
                                        Utils.getThreadName(event.getThread()),
                                        event.getStartTime()
                                             .atZone(ZoneOffset.UTC)
                                             .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
    ) : String.format(FORMAT_ERR,
                      event.getValue(EventFields.OPERATION),
                      result,
                      Fun.formatTime(event.getDuration()),
                      event.getValue(EventFields.OPERATION_COUNTER),
                      event.getValue(EventFields.EXCEPTION),
                      Utils.getThreadName(event.getThread()),
                      event.getStartTime()
                           .atZone(ZoneOffset.UTC)
                           .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
    );
    synchronized (System.out) {
      System.out.println(str);
      System.out.flush();
    }
  }
}
