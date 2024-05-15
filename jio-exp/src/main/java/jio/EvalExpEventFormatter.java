package jio;

import static jio.EvalExpEvent.RESULT;

import java.util.Objects;
import java.util.function.Function;
import jdk.jfr.consumer.RecordedEvent;

/**
 * A class that converts Java Flight Recorder (JFR) event with the name {@link #EVENT_NAME} to formatted strings. This
 * class is intended to be used as a Function for transforming RecordedEvents into human-readable strings.
 *
 * <p>
 * The formatting includes information such as the expression, result, output, duration, and context.
 * </p>
 *
 * <p>
 * Note: This class is designed to work with the JFR events created by jio-exp. Since it's just a function, you can
 * define your own formatters.
 * </p>
 *
 * <p>
 * The default formatting template is defined by the constant {@link #FORMAT}.
 * </p>
 *
 * @see #FORMAT
 * @see #EVENT_NAME
 */
public final class EvalExpEventFormatter implements Function<RecordedEvent, String> {

  /**
   * The singleton instance of ExpEventFormatter with the default identity formatter for output.
   */
  public static final EvalExpEventFormatter INSTANCE = new EvalExpEventFormatter(Function.identity());

  private static final String EXP_FIELD = "expression";
  private static final String VALUE_FIELD = "value";
  private static final String CONTEXT_FIELD = "context";
  private static final String RESULT_FIELD = "result";
  private static final String EXCEPTION_FIELD = "exception";
  /**
   * The function used to format the output string.
   */
  public final Function<String, String> formatOutput;
  @SuppressWarnings("InlineFormatString")
  private static final String FORMAT = """
      start_time: %s event: eval-exp; exp: %s; result: %s; \
      output: %s; duration: %s; context: %s;""";
  private static final String EVENT_NAME = "jio.exp.EvalExp";

  /**
   * Constructs an ExpEventFormatter with a custom output formatter.
   *
   * @param formatOutput The function to format the output string.
   */
  public EvalExpEventFormatter(Function<String, String> formatOutput) {
    this.formatOutput = Objects.requireNonNull(formatOutput);
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
                .equals(EVENT_NAME);
    var result = event.getValue(RESULT_FIELD);
    boolean isSuccess = RESULT.SUCCESS.name()
                                      .equals(result);
    return String.format(FORMAT,
                         event.getValue(EXP_FIELD),
                         event.getValue(RESULT_FIELD),
                         isSuccess ? formatOutput.apply(event.getValue(VALUE_FIELD)) : event.getValue(EXCEPTION_FIELD),
                         jio.time.Fun.formatTime(event.getDuration()),
                         event.getValue(CONTEXT_FIELD),
                         event.getStartTime()
                        );
  }
}
