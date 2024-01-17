package jio.test.junit;

import jdk.jfr.consumer.RecordedEvent;
import jio.test.Utils;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.function.Consumer;

@SuppressWarnings("InlineFormatString")
final class JdbcTestDebugger implements Consumer<RecordedEvent> {

    private static final String FORMAT_SUC = """
            event: jdbc-stm, result: %s, duration: %s
            sql: %s
            op-counter: %s, thread: %s, event-start-time: %s
            """;
    private static final String FORMAT_ERR = """
            event: jdbc-stm, result: %s, duration: %s exception: %s
            sql: %s
            op-counter: %s, thread: %s, event-start-time: %s
            """;


    @Override
    public void accept(RecordedEvent e) {
        assert e.getEventType().getName().equals("jio.jdbc");
        String exception = e.getValue("exception");
        boolean isSuccess = exception == null || exception.isEmpty();
        var message = isSuccess ?
                String.format(FORMAT_SUC,
                              e.getValue("result"),
                              Utils.formatTime(e.getDuration().toNanos()),
                              e.getValue("sql"),
                              e.getValue("opCounter"),
                              Utils.getThreadName(e.getThread()),
                              e.getStartTime()
                               .atZone(ZoneId.systemDefault())
                               .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
                             ) :
                String.format(FORMAT_ERR,
                              e.getValue("result"),
                              Utils.formatTime(e.getDuration().toNanos()),
                              exception,
                              e.getValue("sql"),
                              e.getValue("opCounter"),
                              Utils.getThreadName(e.getThread()),
                              e.getStartTime()
                               .atZone(ZoneId.systemDefault())
                               .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
                             );
        synchronized (System.out) {
            System.out.println(message);
            System.out.flush();
        }
    }
}
