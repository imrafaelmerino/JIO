package jio.test.junit;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for enabling debugging of HTTP client interactions in JUnit tests.
 *
 * <p>When applied to a test class or test method, this annotation enables debugging for HTTP client interactions,
 * allowing you to collect information about outgoing HTTP requests, responses, and their associated events.
 *
 * <p>You can configure the duration for which HTTP client debugging is active using the `duration` attribute. By default,
 * debugging is enabled for 1000 milliseconds (1 second). You can also specify a custom debugging configuration using the
 * `conf` attribute, allowing you to fine-tune debugging behavior for specific scenarios.
 *
 * <p>Usage example:
 *
 * <pre>
 * {@code
 * import org.junit.jupiter.api.Test;
 * import org.junit.jupiter.api.extension.ExtendWith;
 * import jio.test.junit.DebugHttpClient;
 * import jio.test.junit.Debugger;
 *
 * @ExtendWith(Debugger.class)
 * @DebugHttpClient(duration = 2000, conf = "custom-config")
 * public class MyHttpClientTest {
 *     // Test methods involving HTTP client interactions go here
 * }
 * }
 * </pre>
 *
 * <p>In this example, HTTP client debugging is enabled for 2 seconds, and a custom debugging configuration named
 * "custom-config" is used.
 *
 * @see Debugger
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface DebugHttpClient {
    /**
     * Specifies the duration (in milliseconds) for which HTTP client debugging is active.
     *
     * @return the duration in milliseconds
     */
    int duration() default 1000;

    /**
     * Specifies a custom debugging configuration to fine-tune debugging behavior for specific scenarios.
     *
     * @return the name of the custom debugging configuration
     */
    String conf() default "default";
}
