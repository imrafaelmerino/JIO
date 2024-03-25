package jio.mongodb;

import static jio.mongodb.MongoOpEvent.EXCEPTION_FIELD;
import static jio.mongodb.MongoOpEvent.OPERATION_FIELD;
import static jio.mongodb.MongoOpEvent.OPERATION_COUNTER;
import static jio.mongodb.MongoOpEvent.RESULT_FIELD;

import java.util.function.Function;
import jdk.jfr.consumer.RecordedEvent;
import jio.mongodb.MongoOpEvent.RESULT;
import jio.time.Fun;

/**
 * Formats recorded events from a jio-mongodb operation into a human-readable string. Since it's just a function you can
 * define your own formatters
 */
@SuppressWarnings("InlineFormatString")
public final class MongoClientOpEventFormatter implements Function<RecordedEvent, String> {

  /**
   * The singleton instance of HttpClientEventFormatter.
   */
  public static final MongoClientOpEventFormatter INSTANCE = new MongoClientOpEventFormatter();

  private static final String EVENT_LABEL = "jio.mongodb.Op";
  private static final String SUCCESS_FORMAT =
      """
          event: mongo-client; op: %s; result: %s; duration: %s; \
          op-counter: %s; start_time: %s""";
  private static final String FAILURE_FORMAT =
      """
          event: mongo-client; op: %s; result: %s; duration: %s; \
          op-counter: %s; exception: %s; start_time: %s""";

  private MongoClientOpEventFormatter() {
  }

  /**
   * Formats a recorded event into a human-readable string representation.
   *
   * @param event The recorded event to be formatted.
   * @return A formatted string representing the MongoDB client operation, result, duration, and exception (if any).
   */
  @Override
  public String apply(RecordedEvent event) {
    assert event.getEventType()
                .getName()
                .equals(EVENT_LABEL);
    var result = event.getValue(RESULT_FIELD);
    boolean isSuccess = RESULT.SUCCESS.name()
                                      .equals(result);
    return isSuccess ? String.format(SUCCESS_FORMAT,
                                     event.getValue(OPERATION_FIELD),
                                     result,
                                     Fun.formatTime(event.getDuration()),
                                     event.getValue(OPERATION_COUNTER),
                                     event.getStartTime()
                                    ) : String.format(FAILURE_FORMAT,
                                                      event.getValue(OPERATION_FIELD),
                                                      result,
                                                      Fun.formatTime(event.getDuration()),
                                                      event.getValue(OPERATION_COUNTER),
                                                      event.getValue(EXCEPTION_FIELD),
                                                      event.getStartTime()
                                                     );

  }
}
