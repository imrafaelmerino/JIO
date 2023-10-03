/**
 * Package containing JUnit extensions and annotations for testing with Jio.
 *
 * <p>The classes and annotations in this package are designed to facilitate testing with Jio, a library for
 * enabling testability and observability in Java applications. Jio provides tools for stubbing, debugging, and
 * monitoring various components of your codebase during testing.
 *
 * <p>The key classes and annotations in this package include:
 * - {@link jio.test.junit.Debugger}: A JUnit extension that enables debugging of different components during test execution.
 * - {@link jio.test.junit.DebugHttpClient}: An annotation for enabling debugging of HTTP client interactions.
 * - {@link jio.test.junit.DebugHttpServer}: An annotation for enabling debugging of HTTP server interactions.
 * - {@link jio.test.junit.DebugMongoClient}: An annotation for enabling debugging of MongoDB client interactions.
 * - {@link jio.test.junit.DebugExp}: An annotation for enabling debugging of expressions and observability.
 *
 * <p>These tools help you gain insights into the behavior of your code during tests, monitor interactions with
 * external services, and ensure that your code is functioning correctly.
 *
 * <p>Usage example:
 * <pre>
 * {@code
 * import org.junit.jupiter.api.Test;
 * import org.junit.jupiter.api.extension.ExtendWith;
 * import jio.test.junit.*;
 *
 * @ExtendWith(Debugger.class)
 * public class MyComponentTest {
 *     // Test methods and annotations using Jio testing tools
 * }
 * }
 * </pre>
 *
 * <p>By using the classes and annotations provided in this package, you can enhance the effectiveness of your
 * tests and gain valuable insights into the behavior of your code.
 *
 * @see jio.test.junit.Debugger
 * @see jio.test.junit.DebugHttpClient
 * @see jio.test.junit.DebugHttpServer
 * @see jio.test.junit.DebugMongoClient
 * @see jio.test.junit.DebugExp
 */
package jio.test.junit;
