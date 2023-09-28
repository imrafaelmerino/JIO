package jio.test.junit;

import jdk.jfr.consumer.RecordedEvent;
import jio.jfr.EventDebugger;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.function.Consumer;

class HttpServerDebugger extends EventDebugger {

    private static final Consumer<RecordedEvent> consumer = e -> {
        var str = String.format("httpserver; %s; %s; %s; %s; %s; %s; %s; %s; %s ms; counter=%s; %s; %s",
                                e.getThread().getJavaName(),
                                e.getValue("remoteHostAddress"),
                                e.getValue("remoteHostPort"),
                                e.getValue("protocol"),
                                e.getValue("method"),
                                e.getValue("uri"),
                                e.getValue("result"),
                                (e.getValue("exception") == null || "".equals(e.getValue("exception"))) ?
                                        e.getValue("statusCode") :
                                        e.getValue("exception"),
                                e.getDuration().isZero() ? "0" : e.getDuration().toMillis(),
                                e.getValue("reqCounter"),
                                e.getValue("reqHeaders"),
                                e.getStartTime().atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
                               );
        System.out.println(str);
    };


    public HttpServerDebugger(String confName) {
        super("jio.httpserver", confName, consumer);
    }


}
