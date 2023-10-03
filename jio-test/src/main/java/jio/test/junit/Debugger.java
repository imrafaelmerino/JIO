package jio.test.junit;

import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
/**
 * JUnit extension for enabling debugging of various components in tests.
 *
 * <p>When used as a JUnit extension, this class allows you to enable and configure debugging for different
 * components, such as stub interactions, HTTP clients, HTTP servers, MongoDB clients, and expressions.
 *
 * <p>This extension offers the flexibility to enable debugging for specific components, control the duration
 * of debugging, and specify custom debugging configurations.
 *
 * <p>The `Debugger` extension can be applied at both the class and method levels using the following annotations:
 * - {@link DebugHttpClient} for enabling HTTP client debugging
 * - {@link DebugHttpServer} for enabling HTTP server debugging
 * - {@link DebugMongoClient} for enabling MongoDB client debugging
 * - {@link DebugExp} for enabling expression debugging
 *
 * <p>By default, the duration for debugging is set to 1000 milliseconds (1 second) for each component. You can
 * customize the duration for each component individually using the corresponding annotation. The debugging
 * duration determines how long the test execution will be monitored for debugging events.
 *
 * <p>You can also specify a custom debugging configuration using the `conf` attribute in the respective
 * annotation. Custom configurations allow you to fine-tune debugging behavior for specific scenarios.
 *
 * <p>Usage example:
 *
 * <pre>
 * {@code
 * import org.junit.jupiter.api.Test;
 * import org.junit.jupiter.api.extension.ExtendWith;
 * import jio.test.junit.*;
 *
 * @ExtendWith(Debugger.class)
 * @DebugStub(duration = 2000, conf = "custom-config")
 * public class MyStubTest {
 *     // Test methods involving stub interactions go here
 * }
 * }
 * </pre>
 *
 * <p>In this example, stub debugging is enabled for 2 seconds using a custom debugging configuration named
 * "custom-config."
 *
 * <p>When a debugging duration is specified, the test execution may not finish until that duration has elapsed,
 * depending on the component being debugged. Therefore, it's important to set an appropriate debugging duration
 * to avoid unnecessary delays in test execution.
 *
 * <p>Each component's debugging events are collected from the Java Flight Recorder (JFR) system via Jio, which
 * provides insights into component behavior during test execution.
 *
 * @see DebugHttpClient
 * @see DebugHttpServer
 * @see DebugMongoClient
 * @see DebugExp
 */
public class Debugger implements BeforeEachCallback, AfterEachCallback, AfterAllCallback {

    MongoDebugger mongoEventDebugger;
    ExpDebugger expEventDebugger;
    HttpClientDebugger httpClientDebugger;
    HttpServerDebugger httpServerDebugger;

    @Override
    public void afterEach(ExtensionContext context) {
        if (mongoEventDebugger != null) mongoEventDebugger.awaitTermination();
        if (expEventDebugger != null) expEventDebugger.awaitTermination();
        if (httpClientDebugger != null) httpClientDebugger.awaitTermination();
        if (httpServerDebugger != null) httpServerDebugger.awaitTermination();
    }

    @Override
    public void beforeEach(ExtensionContext context) {
        intDebuggers(context);
    }

    private void intDebuggers(ExtensionContext context) {
        DebugExp debugExp = context.getRequiredTestMethod()
                                   .getAnnotation(DebugExp.class);
        if (debugExp == null)
            debugExp = context.getRequiredTestClass()
                              .getAnnotation(DebugExp.class);
        if (debugExp != null) {
            expEventDebugger = new ExpDebugger(debugExp.conf());
            expEventDebugger.startAsync(debugExp.duration());
            System.out.println("Registered expressions debugger for " + debugExp.duration() + " ms");
        }

        DebugHttpClient debugHttpClient = context.getRequiredTestMethod()
                                                 .getAnnotation(DebugHttpClient.class);
        if (debugHttpClient == null)
            debugHttpClient = context.getRequiredTestClass()
                                     .getAnnotation(DebugHttpClient.class);
        if (debugHttpClient != null) {
            httpClientDebugger = new HttpClientDebugger(debugHttpClient.conf());
            httpClientDebugger.startAsync(debugHttpClient.duration());
            System.out.println("Registered Http client debugger for " + debugHttpClient.duration() + " ms");
        }


        DebugHttpServer debugHttpServer = context.getRequiredTestMethod()
                                                 .getAnnotation(DebugHttpServer.class);
        if (debugHttpServer == null)
            debugHttpServer = context.getRequiredTestClass()
                                     .getAnnotation(DebugHttpServer.class);
        if (debugHttpServer != null) {
            httpServerDebugger = new HttpServerDebugger(debugHttpServer.conf());
            httpServerDebugger.startAsync(debugHttpServer.duration());
            System.out.println("Registered Http server debugger for " + debugHttpServer.duration() + " ms");
        }

        DebugMongoClient debugMongoClient = context.getRequiredTestMethod()
                                                   .getAnnotation(DebugMongoClient.class);
        if (debugMongoClient == null)
            debugMongoClient = context.getRequiredTestClass()
                                      .getAnnotation(DebugMongoClient.class);
        if (debugMongoClient != null) {
            mongoEventDebugger = new MongoDebugger(debugMongoClient.conf());
            mongoEventDebugger.startAsync(debugMongoClient.duration());
            System.out.println("Registered MongoDB client debugger for " + debugMongoClient.duration() + " ms");
        }
    }


    void start() {

    }

    void close() {
        if (mongoEventDebugger != null) mongoEventDebugger.close();
        if (expEventDebugger != null) expEventDebugger.close();
        if (httpClientDebugger != null) httpClientDebugger.close();
        if (httpServerDebugger != null) httpServerDebugger.close();
    }


    @Override
    public void afterAll(ExtensionContext extensionContext) throws Exception {
        close();
    }
}
