package jio.test.junit;

import jdk.jfr.consumer.RecordedEvent;
import jio.jfr.EventDebugger;
import jio.test.Utils;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.function.Consumer;

class HttpClientDebugger extends EventDebugger {

    private static final String FORMAT_SUC = """
            event: httpclient-req, result: %s, status-code: %s, duration: %s
            method: %s, uri: %s, req-counter: %s
            thread: %s, start-time: %s
            """;
    private static final String FORMAT_ERR = """
            event: httpclient-req, result: %s, exception: %s, duration: %s
            method: %s, uri: %s, req-counter: %s
            thread: %s, start-time: %s""";
    static final Consumer<RecordedEvent> consumer = e -> {

        String exception = e.getValue("exception");
        boolean isSuccess = exception == null || "".equals(exception);
        var str = String.format(isSuccess ? FORMAT_SUC : FORMAT_ERR,
                                e.getValue("result"),
                                isSuccess ?
                                        e.getValue("statusCode") :
                                        exception,
                                Utils.formatTime(e.getDuration().toNanos()),
                                e.getValue("method"),
                                e.getValue("uri"),
                                e.getValue("reqCounter"),
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

    public HttpClientDebugger(String confName) {
        super("jio.httpclient", confName, consumer);
    }


}
