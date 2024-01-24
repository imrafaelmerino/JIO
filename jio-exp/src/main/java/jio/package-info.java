/**
 * The {@code jio} package contains a set of abstractions and implementations for building and managing effectful
 * computations in a functional and composable manner. These abstractions are designed to work with Java's
 * CompletableFuture and provide a powerful toolset for working with asynchronous operations, error handling, and
 * composition of complex computations.
 *
 * <p>The primary building blocks in this package are the {@link jio.IO} type, which represents an
 * effectful computation, and various subtypes like {@link jio.Val} and {@link jio.Exp}. The key concepts and classes in
 * this package are as follows:
 *
 * <h2>IO: Effectful Computations</h2>
 * The core abstraction in this package is the {@link jio.IO} interface, which represents an effectful computation. It
 * is a monadic type that allows you to describe asynchronous operations, error handling, and composition of
 * computations. {@code IO} can be thought of as a recipe for a computation.
 *
 * <h2>Exp: Composable Expressions</h2>
 * The {@link jio.Exp} class hierarchy represents composable expressions, which can be further reduced or composed of
 * different operations. Subtypes like {@link jio.ListExp}, {@link jio.JsObjExp}, and {@link jio.IfElseExp} allow you to
 * work with expressions of specific arities or structures. Expressions of type {@code Exp} are composable and can
 * involve multiple sub-effects that need to be executed in a specific order.
 *
 * <h2>Lambda: Functional Abstractions</h2>
 * The {@link jio.Lambda} interface represents a function that takes an input and produces an {@code IO} effect. It
 * provides methods for transforming predicates and functions into {@code Lambda} instances, making it easier to work
 * with functional abstractions in the context of effectful computations.
 *
 * <h2>RetryPolicy: Error Handling Strategies</h2>
 * The {@link jio.RetryPolicy} interface defines a strategy for handling errors and retries in effectful computations.
 * Implementations of this interface allow you to control how and when a computation should be retried based on
 * specified criteria.
 *
 * <p>Overall, the {@code jio} package provides a robust framework for building and managing
 * complex, asynchronous computations in a functional style. It encourages the separation of effectful operations from
 * their execution, promotes composability, and offers powerful error handling mechanisms to create resilient and
 * reliable applications.
 */
package jio;
