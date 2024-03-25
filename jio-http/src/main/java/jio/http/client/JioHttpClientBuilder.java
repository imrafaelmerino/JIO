package jio.http.client;

import java.net.http.HttpClient;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Predicate;
import java.util.function.Supplier;
import jio.RetryPolicy;

/**
 * Builder for creating custom {@link JioHttpClient} instances with configurable options. This builder allows you to
 * customize the behavior of the HTTP client, including specifying a retry policy and a retry predicate for handling
 * exceptions during HTTP requests.
 *
 * <p>The builder also provides an option to disable the recording of Java Flight Recorder (JFR)
 * events for HTTP requests. JFR event recording is enabled by default.</p>
 *
 * <p>This builder creates HTTP client hat uses virtual threads for synchronous of the requests</p>
 */
public final class JioHttpClientBuilder implements Supplier<JioHttpClient> {

  private final HttpClient.Builder client;
  private Predicate<Throwable> reqRetryPredicate;
  private RetryPolicy reqRetryPolicy;
  private boolean recordEvents = true;

  private JioHttpClientBuilder(HttpClient.Builder builder) {

    //since we don't use `sendAsync` method we don't need an executor. Turns out that the java API
    //create a useless thread from this executor in any case, what is a wasting of resources
    //that's why a pool of just one thread is created
    ExecutorService executor = Executors.newSingleThreadExecutor();
    this.client = Objects.requireNonNull(builder)
                         .executor(executor);
    Runtime.getRuntime()
           .addShutdownHook(new Thread(() -> {
             if (executor != null) {
               executor.shutdownNow();
             }
           }));
  }

  /**
   * Constructs a JioHttpClientBuilder with the specified HTTP client.
   *
   * @param builder The HTTP client builder to be used for building JioHttpClient instances.
   * @return a JIO http client builder
   * @see HttpClient
   */
  public static JioHttpClientBuilder of(final HttpClient.Builder builder) {
    return new JioHttpClientBuilder(builder);
  }

  /**
   * Sets a default retry policy that will be applied to every request sent by this HTTP client, allowing for retries
   * when exceptions occur during requests. You can specify the behavior of retries using a RetryPolicy.
   *
   * @param reqRetryPolicy The retry policy to be applied to HTTP requests (if null, no requests are retried).
   * @return This builder with the specified retry policy.
   */
  public JioHttpClientBuilder withRetryPolicy(RetryPolicy reqRetryPolicy) {
    this.reqRetryPolicy = Objects.requireNonNull(reqRetryPolicy);
    return this;
  }

  /**
   * Sets a predicate that takes an exception and returns true if the retry policy specified with
   * {@link #withRetryPolicy(RetryPolicy)} should be applied. This predicate allows you to selectively apply the retry
   * policy based on the type or condition of the exception.
   *
   * @param reqRetryPredicate The predicate to determine if the retry policy should be applied (if null, the retry
   *                          policy is applied to all exceptions).
   * @return This builder with the specified retry predicate.
   */
  public JioHttpClientBuilder withRetryPredicate(Predicate<Throwable> reqRetryPredicate) {
    this.reqRetryPredicate = Objects.requireNonNull(reqRetryPredicate);
    return this;
  }

  /**
   * Disables the recording of Java Flight Recorder (JFR) events for HTTP requests performed by the client. By default,
   * JFR events are recorded (enabled). Use this method to disable recording if needed.
   *
   * @return This builder with JFR event recording disable.
   */
  public JioHttpClientBuilder withoutRecordedEvents() {
    this.recordEvents = false;
    return this;
  }

  /**
   * Creates a new instance of JioHttpClient with the configured options.
   *
   * @return A JioHttpClient instance configured with the specified options.
   * @see JioHttpClient
   */

  @Override
  public JioHttpClient get() {
    return new JioHttpClientImpl(client,
                                 reqRetryPolicy,
                                 reqRetryPredicate,
                                 recordEvents
    );
  }

}