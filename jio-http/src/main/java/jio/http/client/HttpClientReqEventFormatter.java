package jio.http.client;

import jdk.jfr.consumer.RecordedEvent;

import java.util.function.Function;
import jio.http.client.HttpReqEvent.RESULT;
import jio.time.Fun;

/**
 * A class that converts Java Flight Recorder (JFR) RecordedEvents related to HTTP client operations to formatted
 * strings. This class is intended to be used as a Function for transforming RecordedEvents into human-readable
 * strings.
 *
 * <p>
 * The formatting includes information such as the HTTP method, URI, result, status code (for successful events),
 * exception (for events with errors), duration, and request counter.
 * </p>
 *
 * <p>
 * The formatted output for a successful event is: "{@link  #SUCCESS_FORMAT}".
 * </p>
 *
 * <p>
 * The formatted output for an event with an exception is: "{@link  #FAILURE_FORMAT}".
 * </p>
 *
 * <p>
 * Note: This class is designed to work with the JFR events created by jio-http. Since it's just * a function you can
 * define your own formatters.
 * </p>
 *
 * @see #SUCCESS_FORMAT
 * @see #FAILURE_FORMAT
 */
public final class HttpClientReqEventFormatter implements Function<RecordedEvent, String> {

  /**
   * The singleton instance of HttpClientEventFormatter.
   */
  public static final HttpClientReqEventFormatter INSTANCE = new HttpClientReqEventFormatter();
  private static final String SUCCESS_FORMAT =
      """
          event: http-req; method: %s; uri: %s;
          result: %s; status-code: %s; duration: %s;
          req-counter: %s
          """.replace("\n",
                      " ");
  private static final String FAILURE_FORMAT = """
      event: http-req; method: %s; uri: %s;
      result: %s; exception: %s; duration: %s;
      req-counter: %s
      """.replace("\n",
                  " ");
  private static final String EVENT_NAME = "jio.http.client.Req";

  private HttpClientReqEventFormatter() {
  }

  private static final String METHOD_FIELD = "method";

  private static final String URI_FIELD = "uri";

  private static final String STATUS_CODE_FIELD = "statusCode";

  private static final String RESULT_FIELD = "result";

  private static final String REQ_COUNTER_FIELD = "reqCounter";

  private static final String EXCEPTION_FIELD = "exception";

  @Override
  public String apply(RecordedEvent e) {
    assert e.getEventType()
            .getName()
            .equals(EVENT_NAME);

    var result = e.getValue(RESULT_FIELD);
    boolean isSuccess = RESULT.SUCCESS.name()
                                      .equals(result);
    if (isSuccess) {
      return String.format(SUCCESS_FORMAT,
                           e.getValue(METHOD_FIELD),
                           e.getValue(URI_FIELD),
                           result,
                           e.getValue(STATUS_CODE_FIELD),
                           Fun.formatTime(e.getDuration()),
                           e.getValue(REQ_COUNTER_FIELD)
                          );
    }
    return String.format(FAILURE_FORMAT,
                         e.getValue(METHOD_FIELD),
                         e.getValue(URI_FIELD),
                         result,
                         e.getValue(EXCEPTION_FIELD),
                         Fun.formatTime(e.getDuration()),
                         e.getValue(REQ_COUNTER_FIELD)
                        );

  }
}