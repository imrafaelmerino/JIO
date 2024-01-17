package jio.test.junit;

import jdk.jfr.consumer.RecordedEvent;
import jio.test.Utils;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.function.Consumer;

@SuppressWarnings("InlineFormatString")
final class EvalExpDebugger implements Consumer<RecordedEvent> {
    private static final String FORMAT = """
            event: eval, expression: %s, result: %s, output: %s
            duration: %s, context: %s, thread: %s, event-start-time: %s
            """;

    @Override
    public void accept(RecordedEvent e) {
        assert e.getEventType().getName().equals("jio.exp");
        String exc = e.getValue("exception");
        boolean isSuccess = exc == null || exc.isEmpty();
        var str = String.format(FORMAT,
                                e.getValue("expression"),
                                e.getValue("result"),
                                isSuccess ? e.getValue("value") : exc,
                                Utils.formatTime(e.getDuration().toNanos()),
                                e.getValue("context"),
                                Utils.getThreadName(e.getThread()),
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
