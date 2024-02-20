package jio.test.junit;

import jdk.jfr.consumer.RecordedEvent;
import jio.test.Utils;
import jio.time.Fun;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.function.Consumer;

@SuppressWarnings("InlineFormatString")
final class EvalExpDebugger implements Consumer<RecordedEvent> {

  private static final String FORMAT = """
      ------ eval-exp --------
      |  Expression: %s
      |  Result: %s
      |  Duration: %s
      |  Output: %s
      |  Context: %s
      |  Thread: %s
      |  Event Start Time: %s
      -------------------------
      """;
  static final String EVENT_NAME = "jio.exp.EvalExp";

  @Override
  public void accept(RecordedEvent event) {
    assert EVENT_NAME.equals(event.getEventType()
                                  .getName());
    String result = event.getValue(EventFields.RESULT);
    boolean isSuccess = "SUCCESS".equals(result);
    var str = String.format(FORMAT,
                            event.getValue(EventFields.EXPRESSION),
                            event.getValue(EventFields.RESULT),
                            Fun.formatTime(event.getDuration()),
                            isSuccess ? event.getValue(EventFields.VALUE) : event.getValue(EventFields.EXCEPTION),
                            event.getValue(EventFields.CONTEXT),
                            Utils.getThreadName(event.getThread()),
                            event.getStartTime()
                                 .atZone(ZoneId.systemDefault())
                                 .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)

    );
    synchronized (System.out) {
      System.out.println(str);
      System.out.flush();
    }

  }
}
