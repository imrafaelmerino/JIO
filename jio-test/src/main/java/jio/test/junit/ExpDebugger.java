package jio.test.junit;

import jdk.jfr.consumer.RecordedEvent;
import jio.jfr.EventDebugger;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.function.Consumer;

class ExpDebugger extends EventDebugger {
    static final Consumer<RecordedEvent> consumer = e -> {
        var str = String.format("exp; %s; %s; %s; %s; %s ms; %s; %s",
                                e.getThread().getJavaName(),
                                e.getValue("expression"),
                                e.getValue("result"),
                                (e.getValue("exception") == null || "".equals(e.getValue("exception"))) ?
                                        e.getValue("value") :
                                        e.getValue("exception"),
                                e.getDuration().isZero() ? "0" : e.getDuration().toMillis(),
                                e.getValue("context"),
                                e.getStartTime().atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
                               );
        System.out.println(str);
    };

    public ExpDebugger(String confName) {
        super("jio.exp", confName, consumer);
    }


}
