package jio.http.client;

import jio.RetryPolicy;

import java.net.http.HttpResponse;
import java.util.function.Predicate;

/**
 * Represents a wrapper around the http java client to make http request asynchronously using {@link jio.Lambda lambdas}
 * and therefore taking advantage of the JIO API. Use the builder {@link MyHttpClientBuilder} to create
 * instances.
 * For every request, an event {@link ClientReqEvent} is created and written to the Flight Recorder system
 * You can also define a retry policy and a retry condition that will be applied to every request
 * with the builder options {@link MyHttpClientBuilder#setRetryPolicy(RetryPolicy)} and
 * {@link MyHttpClientBuilder#setRetryPredicate(Predicate)}
 *
 */
public interface MyHttpClient {

    /**
     * HttpLambda that takes a request builder and returns a JIO effect with the http response, parsing
     * the response body into a String. The body is decoded using the character set specified in the
     * Content-Type response header. If there is no such header, or the character set is not supported,
     * then UTF_8 is used. When the HttpResponse object is returned, the body has been completely written
     * to the string.
     *
     * @return a http lambda
     */
    HttpLambda<String> ofString();

    /**
     * HttpLambda that takes a request builder and returns a JIO effect with the http response, parsing
     * the response body into an array of bytes. When the HttpResponse object is returned, the body has
     * been completely written to the string.
     *
     * @return a http lambda
     */
    HttpLambda<byte[]> ofBytes();

    /**
     * HttpLambda that takes a request builder and returns a JIO effect with the http response, discarding
     * the body response.
     *
     * @return a http lambda
     */
    HttpLambda<Void> discarding();

    /**
     * HttpLambda that takes a request builder and returns a JIO effect with the http response, parsing
     * the body with the given handler. There are different predefined http lambdas in
     * this class to parse the most common types: {@link #ofString strings} and {@link #ofBytes()}
     * etc
     *
     * @param handler the body response handler
     * @param <T>     the response body type
     * @return a new HttpLambda
     * @see HttpResponse.BodyHandlers for more body handlers implementations
     *
     */
    <T> HttpLambda<T> bodyHandler(final HttpResponse.BodyHandler<T> handler);

}
