package jio.jdbc;

import jdk.jfr.consumer.RecordedEvent;

import java.util.function.Function;

import static jio.jdbc.StmEvent.SQL_LABEL;

/**
 * A class that converts Java Flight Recorder (JFR) RecordedEvents to formatted strings. This class is intended to be
 * used as a Function for transforming RecordedEvents into human-readable strings.
 *
 * <p>
 * The formatting includes information such as the result, duration, SQL statement, and counter.
 * </p>
 *
 * <p>
 * The formatted output for a successful event is: "result: %s, duration: %s, sql: %s, counter: %s".
 * </p>
 *
 * <p>
 * The formatted output for an event with an exception is: "result: %s, duration: %s, exception: %s, sql: %s, counter:
 * %s".
 * </p>
 *
 * <p>
 * Note: This class is designed to work with the JFR events persisted by jio-jdbc. Since it's just a function you can
 * define your own formatters
 * </p>
 */
public final class JdbcEventFormatter implements Function<RecordedEvent, String> {

    /**
     * The singleton instance of JdbcEventFormatter.
     */
    public static final JdbcEventFormatter INSTANCE = new JdbcEventFormatter();

    /**
     * Converts a RecordedEvent to a formatted string.
     *
     * @param e The RecordedEvent to be converted.
     * @return A formatted string representing the information from the RecordedEvent.
     */
    @Override
    public String apply(RecordedEvent e) {
        assert e.getEventType().getName().equals("jio.jdbc");
        String exception = e.getValue(StmEvent.EXCEPTION_LABEL);
        boolean isSuccess = exception == null || exception.isEmpty();
        return isSuccess ?
                String.format("result: %s; duration: %s; sql: %s; op-counter: %s",
                              e.getValue(StmEvent.RESULT_LABEL),
                              e.getDuration().toMillis(),
                              sqlToStr.apply(e.getValue(StmEvent.SQL_LABEL)),
                              e.getValue(StmEvent.OP_COUNTER_LABEL)
                             ) :
                String.format("result: %s; exception: %s; duration: %s; sql: %s; op-counter: %s",
                              e.getValue(StmEvent.RESULT_LABEL),
                              exception,
                              e.getDuration().toMillis(),
                              sqlToStr.apply(e.getValue(StmEvent.SQL_LABEL)),
                              e.getValue(StmEvent.OP_COUNTER_LABEL)
                             );
    }

    private final Function<String, String> sqlToStr;

    /**
     * Constructs a JdbcEventFormatter with a custom SQL-to-String function.
     *
     * @param sqlToStr The function to convert SQL statements to strings.
     */
    public JdbcEventFormatter(Function<String, String> sqlToStr) {
        this.sqlToStr = sqlToStr;
    }

    /**
     * Constructs a JdbcEventFormatter with the default identity function for SQL statements.
     */
    private JdbcEventFormatter() {
        sqlToStr = Function.identity();
    }
}
