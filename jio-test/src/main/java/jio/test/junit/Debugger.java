package jio.test.junit;

import jdk.jfr.Configuration;
import jdk.jfr.consumer.EventStream;
import jdk.jfr.consumer.RecordedEvent;
import jdk.jfr.consumer.RecordingStream;
import jio.test.Utils;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * JUnit's extension for enabling debugging of various components in tests.
 *
 * <p>When used as a JUnit extension, this class allows you to enable and configure debugging for different
 * components HTTP clients, HTTP servers, MongoDB clients, and expressions evaluation.
 *
 * <p>This extension offers the flexibility to enable debugging for specific components, control the duration
 * of debugging, and specify custom debugging configurations.
 *
 *
 * <p>The debugging duration determines how long the test execution will be monitored for debugging events.
 *
 * <p>You can also specify a custom JFR configuration but most of the time leaving this parameter empty and using the
 * default configuration is enough.
 *
 * <p>Usage example:
 *
 * <pre>
 * {@code
 * import jio.test.junit.Debugger;
 * import org.junit.jupiter.api.extension.RegisterExtension;
 * public class MyTest {
 *
 *     @RegisterExtension
 *     static Debugger debugger = Debugger.of(Duration.ofSeconds(2));
 *
 *    // Test methods involving go here
 * }
 * }
 * </pre>
 *
 * <p>The test execution may not finish until the stream duration has elapsed. Therefore, it's important to set an
 * appropriate debugging duration to avoid unnecessary delays in test execution.
 *
 * <p>Each component's debugging events are collected from the Java Flight Recorder (JFR) system via Jio, which
 * provides insights into component behavior during test execution.
 */
public final class Debugger implements AfterAllCallback, BeforeAllCallback {

    String conf;
    Duration duration;
    EventStream stream;
    Map<String, Consumer<RecordedEvent>> debuggers = new HashMap<>();

    private Debugger(final String conf, final Duration duration) {
        this.conf = Objects.requireNonNull(conf);
        this.duration = Objects.requireNonNull(duration);
    }

    /**
     * Create an instance of the Debugger with a custom JFR configuration and duration.
     *
     * @param conf     A custom JFR configuration for debugging events.
     * @param duration The duration for monitoring test execution with debugging.
     * @return A Debugger instance.
     */
    public static Debugger of(final String conf, final Duration duration){
        return new Debugger(conf,duration);
    }

    /**
     * Create an instance of the Debugger with the default JFR configuration and duration.
     *
     * @param duration The duration for monitoring test execution with debugging.
     * @return A Debugger instance with the default JFR configuration.
     */
    public static Debugger of(final Duration duration){
        return new Debugger("default",duration);
    }


    @Override
    public void afterAll(ExtensionContext extensionContext) throws Exception {
        if (stream != null) {
            stream.awaitTermination();
            stream.close();
        }
    }


    @Override
    public void beforeAll(ExtensionContext context) throws Exception {
        debuggers.put("jio.exp", new EvalExpDebugger());
        debuggers.put("jio.httpclient", new HttpClientDebugger());
        debuggers.put("jio.httpserver", new HttpServerDebugger());
        debuggers.put("jio.mongodb", new MongoDBDebugger());
        stream = new RecordingStream(Configuration.getConfiguration(conf));

        for (var entry : debuggers.entrySet()) {
            stream.onEvent(entry.getKey(), entry.getValue());
        }
        stream.setOrdered(true);
        stream.setStartTime(Instant.now());
        stream.setEndTime(Instant.now().plus(duration));
        stream.startAsync();
        System.out.println(String.format("Started JFR stream for %s in %s\n",
                                         Utils.formatTime(duration.toNanos()),
                                         context.getDisplayName()));
    }
}
