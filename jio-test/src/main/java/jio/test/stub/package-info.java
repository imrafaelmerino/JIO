/**
 * The {@code jio.test.stub} package provides utility classes and stubs for creating and controlling behaviors of
 * asynchronous IO operations in testing scenarios. These classes are particularly useful for simulating various IO
 * behaviors, including success, failure, delays, and sequencing, to aid in testing and development.
 *
 * <p>Key Classes:</p>
 *
 * <ul>
 * <li>{@link jio.test.stub.StubBuilder}: A stub for generating `IO` instances using generators. This allows you to
 * specify the behavior of IO operations.</li>
 * <li>{@link jio.test.stub.ClockStub}: Class for creating different kinds of stubs that stand in for
 * {@link jio.time.Clock clocks}. Useful for controlling time-related behavior during testing.</li>
 * </ul>
 *
 * <p>Usage Examples:</p>
 *
 * <p>Generating IO instances with generators:</p>
 * <pre>
 * {@code
 * var gen1 = Gen.seq(n -> IO.succeed(n));
 * var gen2 = Gen.seq(n-> IO.fail(new RuntimeException("bad luck!")));
 * var gen = Combinators.oneOf(gen1, gen2);
 * }
 * </pre>
 *
 * <p>Creating a stub for controlling IO behavior:</p>
 * <pre>
 * {@code
 * var stub = Stub.ofGen(Gens.seq(n -> IO.succeed(n)));
 * IO<Integer> result = stub.get();
 * }
 * </pre>
 *
 * <p>Simulating clock behavior in testing:</p>
 * <pre>
 * {@code
 * Clock customClock = ClockStub.fromReference(Instant.now());
 * }
 * </pre>
 * <p>
 * For creating stubs in the {@link com.sun.net.httpserver.HttpServer}, exists the subpackage
 * {@link jio.test.stub.httpserver}
 *
 * @see jio.test.stub.StubBuilder
 * @see jio.test.stub.ClockStub
 * @see jio.test.stub.httpserver
 */
package jio.test.stub;
