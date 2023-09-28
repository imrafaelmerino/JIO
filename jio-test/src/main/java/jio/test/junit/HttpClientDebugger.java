package jio.test.junit;

import jdk.jfr.consumer.RecordedEvent;
import jio.jfr.EventDebugger;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.function.Consumer;

class HttpClientDebugger extends EventDebugger {

    static final Consumer<RecordedEvent> consumer = e -> {
        var str = String.format("httpclient; %s; %s; %s; %s; %s; %s ms; counter=%s; %s",
                                e.getThread().getJavaName(),
                                e.getValue("method"),
                                e.getValue("uri"),
                                e.getValue("result"),
                                (e.getValue("exception") == null || "".equals(e.getValue("exception"))) ?
                                        e.getValue("statusCode") :
                                        e.getValue("exception"),
                                e.getDuration().isZero() ? "0" : e.getDuration().toMillis(),
                                e.getValue("reqCounter"),
                                e.getStartTime().atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
                               );
        System.out.println(str);


    };

    public HttpClientDebugger(String confName) {
        super("jio.httpclient", confName, consumer);
    }


}
