package jio.http.client.oauth;

import jio.http.client.MyHttpClient;
import jio.http.client.HttpLambda;

import java.net.http.HttpResponse;

/**
 * Represents a http client with oauth support
 */
public interface MyOauthHttpClient extends MyHttpClient {

    /**
     * Http lambda that takes a req builder, send the request asynchronously to the server and
     * wrap the response into a http response, requesting an access token when necessary and
     * parsing the body response into a string.
     *
     * @return a http lambda
     */
    HttpLambda<String> oauthOfString();

    /**
     * Http lambda that takes a req builder, send the request asynchronously to the server and
     * wrap the response into a http response, requesting an access token when necessary and
     * parsing the body response into an array of bytes
     *
     * @return a http lambda
     */
    HttpLambda<byte[]> oauthOfBytes();

    /**
     * Http lambda that takes a req builder, send the request asynchronously to the server and
     * wrap the response into a http response, requesting an access token when necessary  and
     * discarding the body response
     *
     * @return a http lambda
     */
    HttpLambda<Void> oauthDiscarding();

    /**
     * HttpLambda that takes a request builder and returns a JIO effect with the http response, parsing
     * the body with the given handler and requesting an access token when necessary. There are different
     * predefined http lambdas in this class to parse the most common types: {@link #ofString strings} and {@link #ofBytes()}
     * etc
     *
     * @param handler the body response handler
     * @param <T>     the response body type
     * @return a new HttpLambda
     * @see HttpResponse.BodyHandlers for more body handlers implementations
     *
     */
    <T> HttpLambda<T> oauthBodyHandler(final HttpResponse.BodyHandler<T> handler);


}
