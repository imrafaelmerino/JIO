package jio.http.client;

import jio.RetryPolicy;

import java.net.http.HttpClient;
import java.util.Objects;
import java.util.function.Predicate;

/**
 * Builder to create {@link MyHttpClient} instances.
 */
public final class MyHttpClientBuilder {
    private final HttpClient client;
    private Predicate<Throwable> reqRetryPredicate;
    private RetryPolicy reqRetryPolicy;

    /**
     * Constructor to create a http client builder from a http client
     *
     * @param client the http client
     */
    public MyHttpClientBuilder(HttpClient client) {
        this.client = Objects.requireNonNull(client);
    }


    /**
     * Option to define a default retry policy that will be applied to every request sent by this http client,
     * making retries according to the policy whenever an exception happens. To narrow down what
     * exceptions are retryable, set a retry predicate with {@link #setRetryPredicate(Predicate)}
     *
     * @param reqRetryPolicy the policy  (if null no request is retried)
     * @return this builder
     */
    public MyHttpClientBuilder setRetryPolicy(RetryPolicy reqRetryPolicy) {
        this.reqRetryPolicy = Objects.requireNonNull(reqRetryPolicy);
        return this;
    }

    /**
     * Option to define a predicate that takes an exception and returns true if the retry policy specified
     * with {@link #setRetryPolicy(RetryPolicy)} (RetryPolicy)} must be applied. If a retry policy is not
     * set, then this option is ignored.
     *
     * @param reqRetryPredicate the predicate (if null, option ignored)
     * @return this builder
     */
    public MyHttpClientBuilder setRetryPredicate(Predicate<Throwable> reqRetryPredicate) {
        this.reqRetryPredicate = Objects.requireNonNull(reqRetryPredicate);
        return this;
    }


    /**
     * Creates the http client with the specified options
     *
     * @return a http client
     */
    public MyHttpClient create() {
        return new MyHttpClientImpl(client,
                                    reqRetryPolicy,
                                    reqRetryPredicate
        );
    }


}