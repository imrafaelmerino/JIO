package jio.http.client;

import jio.RetryPolicy;

import java.net.http.HttpClient;
import java.util.Objects;
import java.util.function.Predicate;

/**
 * Builder for creating custom {@link MyHttpClient} instances with configurable options. This builder allows you to
 * customize the behavior of the HTTP client, including specifying a retry policy and a retry predicate for handling
 * exceptions during HTTP requests.
 *
 * <p>The builder also provides an option to disable the recording of Java Flight Recorder (JFR) events for
 * HTTP requests. JFR event recording is enabled by default.</p>
 */
public final class MyHttpClientBuilder {
    private final HttpClient client;
    private Predicate<Throwable> reqRetryPredicate;
    private RetryPolicy reqRetryPolicy;
    private boolean recordEvents = true;

    /**
     * Constructs a MyHttpClientBuilder with the specified HTTP client.
     *
     * @param client The HTTP client to be used for building MyHttpClient instances.
     * @see HttpClient
     */
    public MyHttpClientBuilder(HttpClient client) {
        this.client = Objects.requireNonNull(client);
    }


    /**
     * Sets a default retry policy that will be applied to every request sent by this HTTP client, allowing for retries
     * when exceptions occur during requests. You can specify the behavior of retries using a RetryPolicy.
     *
     * @param reqRetryPolicy The retry policy to be applied to HTTP requests (if null, no requests are retried).
     * @return This builder with the specified retry policy.
     */
    public MyHttpClientBuilder setRetryPolicy(RetryPolicy reqRetryPolicy) {
        this.reqRetryPolicy = Objects.requireNonNull(reqRetryPolicy);
        return this;
    }

    /**
     * Sets a predicate that takes an exception and returns true if the retry policy specified with
     * {@link #setRetryPolicy(RetryPolicy)} should be applied. This predicate allows you to selectively apply the retry
     * policy based on the type or condition of the exception.
     *
     * @param reqRetryPredicate The predicate to determine if the retry policy should be applied (if null, the retry
     *                          policy is applied to all exceptions).
     * @return This builder with the specified retry predicate.
     */
    public MyHttpClientBuilder setRetryPredicate(Predicate<Throwable> reqRetryPredicate) {
        this.reqRetryPredicate = Objects.requireNonNull(reqRetryPredicate);
        return this;
    }
    /**
     * Disables the recording of Java Flight Recorder (JFR) events for HTTP requests performed by the client.
     * By default, JFR events are recorded (enabled). Use this method to disable recording if needed.
     *
     * @return This builder with JFR event recording disable.
     */
    public MyHttpClientBuilder disableRecordEvents(){
        this.recordEvents = false;
        return this;
    }


    /**
     * Creates a new instance of MyHttpClient with the configured options.
     *
     * @return A MyHttpClient instance configured with the specified options.
     * @see MyHttpClient
     */

    public MyHttpClient create() {
        return new MyHttpClientImpl(client,
                                    reqRetryPolicy,
                                    reqRetryPredicate,
                                    recordEvents);
    }


}