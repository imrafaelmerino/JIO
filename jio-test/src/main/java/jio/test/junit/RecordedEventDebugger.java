package jio.test.junit;

import jdk.jfr.Configuration;
import jdk.jfr.consumer.EventStream;
import jdk.jfr.consumer.RecordedEvent;
import jdk.jfr.consumer.RecordingStream;

import java.io.IOException;
import java.text.ParseException;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

final class RecordedEventDebugger {


    static final String HTTPCLIENT_EVENT_NAME = "jio.httpclient";
    static final String MONGODB_EVENT_NAME = "jio.mongodb";
    static final String EXP_EVENT_NAME = "jio.exp";
    static final String HTTP_SERVER_EVENT_NAME = "jio.httpserver";
    static final String STUB_EVENT_NAME = "jio.stub";

    private final EventStream es;

    RecordedEventDebugger(Duration duration) {
        Configuration config;
        try {
            config = Configuration.getConfiguration("default");
            es = new RecordingStream(config);

            es.onEvent(EXP_EVENT_NAME, RecordedEventDebugger::printExpEvent);
            es.onEvent(HTTPCLIENT_EVENT_NAME, RecordedEventDebugger::printHttpClientEvent);
            es.onEvent(MONGODB_EVENT_NAME, RecordedEventDebugger::printMongoDBEvent);
            es.onEvent(STUB_EVENT_NAME, RecordedEventDebugger::printStubEvent);
            es.onEvent(HTTP_SERVER_EVENT_NAME, RecordedEventDebugger::printHttpServerEvent);

            es.setEndTime(Instant.now().plus(duration));

            es.startAsync();

        } catch (IOException | ParseException e) {
            throw new RuntimeException(e);
        }

    }

    private static void printHttpServerEvent(RecordedEvent e) {
            var str = String.format("httpserver; %s; %s; %s; %s; %s; %s; %s; %s; %s ms; counter=%s; %s; %s",
                                    e.getThread().getJavaName(),
                                    e.getValue("remoteHostAddress"),
                                    e.getValue("remoteHostPort"),
                                    e.getValue("protocol"),
                                    e.getValue("method"),
                                    e.getValue("uri"),
                                    e.getValue("result"),
                                    "".equals(e.getValue("exception")) ?
                                            e.getValue("statusCode") :
                                            e.getValue("exception"),
                                    e.getDuration().isZero() ? "0" : e.getDuration().toMillis(),
                                    e.getValue("reqCounter"),
                                    e.getValue("reqHeaders"),
                                    e.getStartTime().atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
                                   );
            System.out.println(str);

        }



    private static void printStubEvent(RecordedEvent e) {

            var str = String.format("stub; %s; %s; %s; %s ms; counter=%s; %s",
                                    e.getThread().getJavaName(),
                                    e.getValue("result"),
                                    "".equals(e.getValue("exception")) ?
                                            e.getValue("value") :
                                            e.getValue("exception"),
                                    e.getDuration().isZero() ? "0" : e.getDuration().toMillis(),
                                    e.getValue("counter"),
                                    e.getStartTime().atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
                                   );
            System.out.println(str);

        }


    private static void printMongoDBEvent(RecordedEvent e) {
            String exception = e.getValue("exception");
            var str = "".equals(exception) ?
                    String.format("mongodb; %s; %s; %s; %s ms; %s",
                                  e.getThread().getJavaName(),
                                  e.getValue("operation"),
                                  e.getValue("result"),
                                  e.getDuration().isZero() ? "0" : e.getDuration().toMillis(),
                                  e.getStartTime()
                                   .atZone(ZoneId.systemDefault())
                                   .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
                                 ) :
                    String.format("mongodb; %s; %s; %s; %s; %s ms; %s",
                                  e.getThread().getJavaName(),
                                  e.getValue("operation"),
                                  e.getValue("result"),
                                  exception,
                                  e.getDuration().isZero() ? "0" : e.getDuration().toMillis(),
                                  e.getStartTime()
                                   .atZone(ZoneId.systemDefault())
                                   .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
                                 );
            System.out.println(str);

        }


    private static void printHttpClientEvent(RecordedEvent e) {
            var str = String.format("httpclient; %s; %s; %s; %s; %s; %s ms; counter=%s; %s",
                                    e.getThread().getJavaName(),
                                    e.getValue("method"),
                                    e.getValue("uri"),
                                    e.getValue("result"),
                                    "".equals(e.getValue("exception")) ? e.getValue("statusCode") : e.getValue("exception"),
                                    e.getDuration().isZero() ? "0" : e.getDuration().toMillis(),
                                    e.getValue("reqCounter"),
                                    e.getStartTime().atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
                                   );
            System.out.println(str);

        }


    private static void printExpEvent(RecordedEvent e) {
            var str = String.format("exp; %s; %s; %s; %s; %s ms; %s; %s",
                                    e.getThread().getJavaName(),
                                    e.getValue("expression"),
                                    e.getValue("result"),
                                    "".equals(e.getValue("exception")) ?
                                            e.getValue("value") :
                                            e.getValue("exception"),
                                    e.getDuration().isZero() ? "0" : e.getDuration().toMillis(),
                                    e.getValue("context"),
                                    e.getStartTime().atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
                                   );
            System.out.println(str);

        }



    void awaitTermination() {
        try {
            es.awaitTermination();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

}
