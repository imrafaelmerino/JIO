package jio.test.junit;

import jdk.jfr.consumer.RecordedEvent;
import jio.jfr.EventDebugger;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.function.Consumer;

class MongoDebugger extends EventDebugger {

    static final Consumer<RecordedEvent> consumer = e -> {
        String exception = e.getValue("exception");
        var str = (e.getValue("exception") == null || "".equals(e.getValue("exception"))) ?
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
    };

    public MongoDebugger(String confName) {
        super("jio.mongodb", confName, consumer);
    }


}
