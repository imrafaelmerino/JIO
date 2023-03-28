package jio.test.junit;

import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.time.Duration;

public class JioDebugger implements
        BeforeEachCallback, AfterEachCallback {


    RecordedEventDebugger consumer;

    @Override
    public void afterEach(ExtensionContext context) {
        if(consumer!=null)consumer.awaitTermination();
    }

    @Override
    public void beforeEach(ExtensionContext context) {
        System.out.println("JIO events produced by "
                                   + context.getDisplayName() + " will printed out on the console");
        var duration = context.getRequiredTestMethod()
                              .getAnnotation(DebuggerDuration.class);
        if(duration == null) duration = context.getRequiredTestClass()
                                              .getAnnotation(DebuggerDuration.class);
        if(duration == null) throw new IllegalStateException("The extension JioDebugger requires to specify the duration of the JFR stream with the annotation @StreamDuration");
        consumer = new RecordedEventDebugger(Duration.ofMillis(duration.millis()));
        System.out.println("Registered JIO_DEBUGGER for " + duration.millis() +" ms");
    }

}
