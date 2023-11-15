package jio.test.junit;

import jdk.jfr.consumer.RecordedEvent;
import jio.test.Utils;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.function.Consumer;
@SuppressWarnings("InlineFormatString")
final class HttpClientDebugger implements Consumer<RecordedEvent> {

    private static final String FORMAT_SUC = """
            event: httpclient-req, result: %s, status-code: %s
            duration: %s method: %s, uri: %s, req-counter: %s
            thread: %s, event-start-time: %s
            """;
    private static final String FORMAT_ERR = """
            event: httpclient-req, result: %s, exception: %s
            duration: %s method: %s, uri: %s, req-counter: %s
            thread: %s, event-start-time: %s
            """;


    @Override
    public void accept(RecordedEvent e) {
        String exception = e.getValue("exception");
        boolean isSuccess = exception == null || exception.isEmpty();
        var str = String.format(isSuccess ? FORMAT_SUC : FORMAT_ERR,
                                isSuccess ?
                                        Utils.categorizeHttpStatusCode(e.getValue("statusCode")) :
                                        e.getValue("result"),
                                isSuccess ?
                                        e.getValue("statusCode") :
                                        exception,
                                Utils.formatTime(e.getDuration().toNanos()),
                                e.getValue("method"),
                                e.getValue("uri"),
                                e.getValue("reqCounter"),
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
