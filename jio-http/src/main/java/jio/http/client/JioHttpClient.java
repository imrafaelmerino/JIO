package jio.http.client;

import java.net.http.HttpResponse;
import java.util.function.Predicate;
import jio.RetryPolicy;

/**
 * Represents a wrapper around the HTTP Java client to make HTTP requests asynchronously using
 * {@link jio.Lambda lambdas} and therefore taking advantage of the JIO API. Instances of this interface can be created
 * using the builder {@link JioHttpClientBuilder}.
 * <p>
 * For every request, an event {@link HttpReqEvent} is created and written to the Flight Recorder system. This allows
 * you to capture request and response details for debugging and performance analysis. Event recording is enabled by
 * default.
 * <p>
 * You can also define a retry policy and a retry condition that will be applied to every request with the builder
 * options {@link JioHttpClientBuilder#withRetryPolicy(RetryPolicy)} and
 * {@link JioHttpClientBuilder#withRetryPredicate(Predicate)}.
 *
 * @see HttpReqEvent
 * @see JioHttpClientBuilder#withoutRecordedEvents To disable event recording.
 */
public interface JioHttpClient extends AutoCloseable {

  /**
   * Provides an HTTP lambda that takes a request builder and returns a JIO effect with the HTTP response, parsing the
   * response body into a String. The body is decoded using the character set specified in the Content-Type response
   * header. If there is no such header, or the character set is not supported, then UTF-8 is used. When the
   * HttpResponse object is returned, the body has been completely written to the string.
   *
   * @return An HTTP lambda for handling responses as strings.
   */
  HttpLambda<String> ofString();

  /**
   * Provides an HTTP lambda that takes a request builder and returns a JIO effect with the HTTP response, parsing the
   * response body into an array of bytes. When the HttpResponse object is returned, the body has been completely
   * written to the byte array.
   *
   * @return An HTTP lambda for handling responses as byte arrays.
   */
  HttpLambda<byte[]> ofBytes();

  /**
   * Provides an HTTP lambda that takes a request builder and returns a JIO effect with the HTTP response, discarding
   * the response body.
   *
   * @return An HTTP lambda for discarding response bodies.
   */
  HttpLambda<Void> discarding();

  /**
   * Provides an HTTP lambda that takes a request builder and returns a JIO effect with the HTTP response, parsing the
   * body with the given handler. Various predefined HTTP lambdas are available for parsing common response types, such
   * as {@link #ofString()} for strings and {@link #ofBytes()} for byte arrays.
   *
   * @param handler The body response handler.
   * @param <T>     The response body type.
   * @return A new HTTP lambda with the specified body response handler.
   * @see HttpResponse.BodyHandlers for more body handler implementations.
   */
  <T> HttpLambda<T> bodyHandler(final HttpResponse.BodyHandler<T> handler);

  void shutdown();

  void shutdownNow();

  @Override
  void close();

}
