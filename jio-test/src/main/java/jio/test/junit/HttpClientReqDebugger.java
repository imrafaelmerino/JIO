package jio.test.junit;

import java.time.ZoneOffset;
import jdk.jfr.consumer.RecordedEvent;
import jio.test.Utils;
import jio.time.Fun;

import java.time.format.DateTimeFormatter;
import java.util.function.Consumer;

@SuppressWarnings("InlineFormatString")
final class HttpClientReqDebugger implements Consumer<RecordedEvent> {

  private static final String FORMAT_SUC = """
      ------ httpclient-req -----
      |  Result: %s
      |  Status Code: %s
      |  Duration: %s
      |  Method: %s
      |  URI Host: %s
      |  URI Path: %s
      |  Request Counter: %s
      |  Thread: %s
      |  Event Start Time: %s
      ----------------------
      """;

  private static final String FORMAT_ERR = """
      ------ httpclient-req -----
      |  Result: %s
      |  Exception: %s
      |  Duration: %s
      |  Method: %s
      |  URI Host: %s
      |  URI Path: %s
      |  Request Counter: %s
      |  Thread: %s
      |  Event Start Time: %s
      ----------------------
      """;
  static final String EVENT_NAME = "jio.http.client.Req";

  @Override
  public void accept(RecordedEvent event) {
    assert EVENT_NAME.equals(event.getEventType()
                                  .getName());
    var result = event.getValue(EventFields.RESULT);
    boolean isSuccess = "SUCCESS".equals(result);
    var str = String.format(isSuccess ? FORMAT_SUC : FORMAT_ERR,
                            isSuccess ? Utils.categorizeHttpStatusCode(event.getValue(EventFields.STATUS_CODE))
                                : result,
                            isSuccess ? event.getValue(EventFields.STATUS_CODE) : event.getValue(EventFields.EXCEPTION),
                            Fun.formatTime(event.getDuration()
                                                .toNanos()),
                            event.getValue(EventFields.METHOD),
                            event.getValue(EventFields.URI_HOST),
                            event.getValue(EventFields.URI_PATH),
                            event.getValue(EventFields.REQ_COUNTER),
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
