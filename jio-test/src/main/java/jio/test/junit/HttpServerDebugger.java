package jio.test.junit;

import jdk.jfr.consumer.RecordedEvent;
import jio.test.Utils;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.function.Consumer;

class HttpServerDebugger implements Consumer<RecordedEvent> {

    private static final String FORMAT_SUC = """
            event: httpserver-req, result: %s, status-code: %s, duration: %s
            protocol: %s, method: %s, uri: %s, req-counter: %s
            remoteHostAddress: %s, remoteHostPort: %s, headers: %s
            thread: %s, event-start-time: %s
            """;
    private static final String FORMAT_ERR = """
            event: httpserver-req, result: %s, exception: %s, duration: %s
            protocol: %s, method: %s, uri: %s, req-counter: %s
            remoteHostAddress: %s, remoteHostPort: %s, headers: %s
            thread: %s, event-start-time: %s
            """;

    @Override
    public void accept(RecordedEvent e) {
        String exception = e.getValue("exception");
        boolean isSuccess = exception == null || "".equals(exception);
        var str = String.format(isSuccess ? FORMAT_SUC : FORMAT_ERR,
                                isSuccess ?
                                        Utils.categorizeHttpStatusCode(e.getValue("statusCode")) :
                                        e.getValue("result"),
                                isSuccess ?
                                        e.getValue("statusCode") :
                                        exception,
                                Utils.formatTime(e.getDuration().toNanos()),
                                e.getValue("protocol"),
                                e.getValue("method"),
                                e.getValue("uri"),
                                e.getValue("reqCounter"),
                                e.getValue("remoteHostAddress"),
                                e.getValue("remoteHostPort"),
                                e.getValue("reqHeaders"),
                                DebuggerUtils.getThreadName(e.getThread()),
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
