package jio.test.junit;

import jdk.jfr.consumer.RecordedEvent;
import jio.test.Utils;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.function.Consumer;

class MongoDBDebugger implements Consumer<RecordedEvent> {
    private static String FORMAT_SUC = """
            event: mongodb-op, op: %s, duration: %s, result: %s
            op: %s, thread: %s,  event-start-time: %s
            """;
    private static String FORMAT_ERR = """
            event: mongodb-op, op: %s, duration: %s, result: %s, exception: %s
            thread: %s,  event-start-time: %s
            """;

    @Override
    public void accept(RecordedEvent e) {
        String exception = e.getValue("exception");
        boolean isSuccess = exception == null || "".equals(exception);
        var str = isSuccess ?
                String.format(FORMAT_SUC,
                              e.getValue("operation"),
                              Utils.formatTime(e.getDuration().toNanos()),
                              e.getValue("result"),
                              e.getThread().getJavaName(),
                              e.getStartTime()
                               .atZone(ZoneId.systemDefault())
                               .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
                             ) :
                String.format(FORMAT_ERR,
                              e.getValue("operation"),
                              Utils.formatTime(e.getDuration().toNanos()),
                              e.getValue("result"),
                              exception,
                              e.getThread().getJavaName(),
                              e.getStartTime()
                               .atZone(ZoneId.systemDefault())
                               .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
                             );
        synchronized (System.out) {
            System.out.println(str);
            System.out.flush();
        }
    }
}
