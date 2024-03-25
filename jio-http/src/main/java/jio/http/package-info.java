/**
 * The {@code jio.http} package provides a comprehensive framework for building HTTP clients and servers using the JIO
 * API. It offers a seamless and asynchronous approach to making HTTP requests and handling responses, making it
 * well-suited for various use cases, including client-server communication and web services integration.
 * <p>
 * This package includes the following key components:
 * <ul>
 * <li>{@link jio.http.server.HttpServerBuilder}: A builder to create HTTP servers with ease. Ideal for building
 * lightweight and high-performance HTTP servers for testing, development, or production environments.
 * You can configure various aspects of the server, including request handlers and execution settings.</li>
 * <li>{@link jio.http.client.JioHttpClient}: An HTTP client interface designed to make HTTP requests asynchronously
 * using {@link jio.Lambda lambdas}. For each request, an event of type {code ClientReqEvent}
 * is created and written to the Java Flight Recorder (JFR) system, providing detailed request and response
 * information for analysis and debugging.</li>
 * <li>{@link jio.http.client.oauth.OauthHttpClient}: An extension of {@link jio.http.client.JioHttpClient} with OAuth
 * support. It facilitates OAuth authentication using the Client Credentials Grant flow, automatically handling
 * access token expiration and refreshes as needed. Ideal for integrating with OAuth-protected APIs securely.</li>
 * <li>JFR (Java Flight Recorder) Integration: The package seamlessly integrates with JFR, enabling you to capture
 * performance metrics, request/response details, and custom events for HTTP interactions. This built-in integration
 * simplifies debugging, profiling, and performance analysis of your HTTP-based applications.</li>
 * <li>Effortless Composability with {@link jio.http.client.HttpLambda}: The package leverages HTTP lambdas
 * ({@link jio.http.client.HttpLambda}) for making HTTP requests and composing effects effortlessly using the JIO API.
 * You can create complex workflows by chaining and combining HTTP lambdas, making your code concise and
 * maintainable.</li>
 * </ul>
 * <p>
 * By using the {@code jio.http} package and the JIO API, you can build robust and efficient HTTP clients and servers,
 * harnessing the power of asynchronous programming and seamless integration with JFR for monitoring and analysis.
 * Whether you need to create a simple HTTP server for testing purposes or a secure OAuth-enabled client, this package
 * offers the tools and flexibility you need to achieve your HTTP-related goals with ease.
 *
 * @see jio.http.server.HttpServerBuilder
 * @see jio.http.client.JioHttpClient
 * @see jio.http.client.oauth.OauthHttpClient
 * @see jio.http.client.HttpLambda
 * @see jio.Lambda
 */
package jio.http;
