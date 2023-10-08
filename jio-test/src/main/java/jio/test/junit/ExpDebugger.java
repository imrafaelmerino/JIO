package jio.test.junit;

import jdk.jfr.consumer.RecordedEvent;
import jio.jfr.EventDebugger;
import jio.test.Utils;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.function.Consumer;

class ExpDebugger extends EventDebugger {
    private static String FORMAT = """
            event: eval-expression, expression: %s, result: %s, duration: %s, output: %s
            context: %s, thread: %s,  start-time: %s
            """;

    static final Consumer<RecordedEvent> consumer = e -> {
        String exc = e.getValue("exception");
        boolean isSuccess = exc == null || "".equals(exc);
        var str = String.format(FORMAT,
                                e.getValue("expression"),
                                e.getValue("result"),
                                Utils.formatTime(e.getDuration().toNanos()),
                                isSuccess ? e.getValue("value") : exc,
                                e.getValue("context"),
                                e.getThread().getJavaName(),
                                e.getStartTime()
                                 .atZone(ZoneId.systemDefault())
                                 .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)

                               );
        synchronized (System.out) {
            System.out.println(str);
            System.out.flush();
        }


    };

    public ExpDebugger(String confName) {
        super("jio.exp", confName, consumer);
    }


}
