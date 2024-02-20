/**
 * The {@code jio.http.client} package provides classes and interfaces for building and using HTTP clients with support
 * for various features, including request retries and response handling as JIO effects. Key classes and interfaces in
 * this package include:
 * <ul>
 * <li>{@link jio.http.client.JioHttpClient}: An interface representing an HTTP client that allows asynchronous HTTP
 * requests with various response handling options.</li>
 * <li>{@link jio.http.client.JioHttpClientBuilder}: A builder class for creating instances of {@code JioHttpClient}
 * with customizable retry policies for requests.</li>
 * <li>{@link jio.http.client.HttpExceptionFun}: A utility class containing predicates to identify specific exceptions
 * that may occur when connecting to a server, such as timeouts, unresolved hosts, and more.</li>
 * </ul>
 * This package is designed to simplify and enhance the functionality of Java's standard {@code HttpClient} by providing
 * features such as request retries and request/response handling as JIO effects.
 */
package jio.http.client;
