package jio.test.junit;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for enabling debugging of stub interactions in JUnit tests.
 *
 * <p>When applied to a test class or test method, this annotation enables debugging for stub interactions,
 * allowing you to collect information about stub executions, their results, and associated events.
 *
 * <p>You can configure the duration for which stub debugging is active using the `duration` attribute. By default,
 * debugging is enabled for 1000 milliseconds (1 second). You can also specify a custom debugging configuration using the
 * `conf` attribute, allowing you to fine-tune debugging behavior for specific scenarios.
 *
 * <p>Usage example:
 *
 * <pre>
 * {@code
 * import org.junit.jupiter.api.Test;
 * import org.junit.jupiter.api.extension.ExtendWith;
 * import jio.test.junit.DebugStub;
 * import jio.test.junit.Debugger;
 *
 * @ExtendWith(Debugger.class)
 * @DebugStub(duration = 2000, conf = "custom-config")
 * public class MyStubTest {
 *     // Test methods involving stub interactions go here
 * }
 * }
 * </pre>
 *
 * <p>In this example, stub debugging is enabled for 2 seconds, and a custom debugging configuration named
 * "custom-config" is used.
 *
 * @see Debugger
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface DebugStub {
    /**
     * Specifies the duration (in milliseconds) for which stub debugging is active.
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
