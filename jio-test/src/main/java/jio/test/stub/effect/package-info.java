/**
 * This package contains classes for creating stubs and testing effects created by the JIO-EXP library.
 * These stubs are used to simulate various effects, such as successful computations, failures with exceptions,
 * and delays in IO operations, allowing developers to control and test these effects in a controlled environment.
 * The package provides different types of stubs for simulating various effect scenarios, including successful
 * and failed computations with optional delays.
 * <p>
 * The main classes in this package include:
 * </p>
 * <ul>
 *     <li>{@link jio.test.stub.effect.IOStub}: An abstract class representing a stub for simulating {@link jio.IO IO} effects.
 *     It provides methods for creating successful computations, failures with exceptions, and introducing delays in
 *     effect execution.</li>
 *     <li>{@link jio.test.stub.effect.ValStub}: A concrete subclass of {@link jio.test.stub.effect.IOStub} that represents a stub
 *     for successful computations with optional delays. It allows developers to specify the value and delay for each
 *     computation.</li>
 *     <li>{@link jio.test.stub.effect.FailureStub}: A concrete subclass of {@link jio.test.stub.effect.IOStub} that represents
 *     a stub for failures with exceptions, followed by successful computations with optional delays. Developers can specify
 *     the exception, value, and delay for each computation.</li>
 *     <li>{@link jio.test.stub.effect.ClockStub}: A class for creating clock stubs that can be used to simulate different
 *     clock behaviors. Developers can create clock stubs based on a reference time or a custom function to control the
 *     ticking of time.</li>
 * </ul>
 * <p>
 * These stubs are essential for writing tests that involve time-sensitive or effectful operations, allowing for controlled
 * testing of different scenarios without relying on external resources or real-world timings.
 * </p>
 */
package jio.test.stub.effect;
