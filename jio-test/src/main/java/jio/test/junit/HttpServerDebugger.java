package jio.test.junit;

import jdk.jfr.consumer.RecordedEvent;
import jio.jfr.EventDebugger;
import jio.test.Utils;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.function.Consumer;

class HttpServerDebugger extends EventDebugger {

    private static final String FORMAT_SUC = """
            event: httpserver-req, result: %s, status-code: %s, duration: %s, req-counter: %s
            protocol: %s, method: %s, uri: %s, remoteHostAddress: %s, remoteHostPort: %s, headers: %s
            thread: %s, start-time: %s
            """;
    private static final String FORMAT_ERR = """
            event: httpserver-req, result: %s, exception: %s, duration: %s, req-counter: %s
            protocol: %s, method: %s, uri: %s, remoteHostAddress: %s, remoteHostPort: %s, headers: %s
            thread: %s, start-time: %s
            """;

    @SuppressWarnings("UnnecessaryLambda")
    private static final Consumer<RecordedEvent> consumer = e -> {
        String exception = e.getValue("exception");
        boolean isSuccess = exception == null || "".equals(exception);
        var str = String.format(isSuccess ? FORMAT_SUC : FORMAT_ERR,
                                e.getValue("result"),
                                isSuccess ?
                                        e.getValue("statusCode") :
                                        exception,
                                Utils.formatTime(e.getDuration().toNanos()),
                                e.getValue("reqCounter"),
                                e.getValue("protocol"),
                                e.getValue("method"),
                                e.getValue("uri"),
                                e.getValue("remoteHostAddress"),
                                e.getValue("remoteHostPort"),
                                e.getValue("reqHeaders"),
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


    public HttpServerDebugger(String confName) {
        super("jio.httpserver", confName, consumer);
    }


}
