package jio.http.client;

import jio.*;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Predicate;

import static java.util.Objects.requireNonNull;
import static jio.http.client.ClientReqEvent.RESULT.FAILURE;
import static jio.http.client.ClientReqEvent.RESULT.SUCCESS;

class MyHttpClientImpl implements MyHttpClient {

    private final AtomicLong counter = new AtomicLong(0);

    private final HttpClient client;

    private final Predicate<Throwable> reqRetryPredicate;
    private final RetryPolicy reqRetryPolicy;
    private final HttpLambda<byte[]> ofBytesLambda;
    private final HttpLambda<Void> discardingLambda;
    private final HttpLambda<String> ofStringLambda;


    MyHttpClientImpl(final HttpClient client,
                     RetryPolicy reqRetryPolicy,
                     Predicate<Throwable> reqRetryPredicate
                    ) {
        this.client = requireNonNull(client);
        this.reqRetryPolicy = reqRetryPolicy;
        this.reqRetryPredicate = reqRetryPredicate;
        this.ofBytesLambda = bodyHandler(HttpResponse.BodyHandlers.ofByteArray());
        this.discardingLambda = bodyHandler(HttpResponse.BodyHandlers.discarding());
        this.ofStringLambda = bodyHandler(HttpResponse.BodyHandlers.ofString());
    }

    static <O> CompletableFuture<HttpResponse<O>> requestWrapper(final MyHttpClientImpl myClient,
                                                                 final HttpRequest request,
                                                                 final HttpResponse.BodyHandler<O> handler
                                                                ) {

        ClientReqEvent event = new ClientReqEvent(request.method(),
                                                  request.uri()
        );
        event.begin();

        event.reqCounter = myClient.counter.incrementAndGet();

        return myClient.client.sendAsync(request,
                                         handler
                                        )
                              .whenComplete((resp, failure) -> {
                                  try {
                                      if (resp != null) {
                                          event.statusCode = resp.statusCode();
                                          event.result = SUCCESS.name();
                                      } else {
                                          Throwable cause = failure.getCause();
                                          event.exception = String.format("%s:%s",
                                                                          cause.getClass().getName(),
                                                                          cause.getMessage()
                                                                         );
                                          event.result = FAILURE.name();
                                      }
                                  } finally {
                                      event.commit();
                                  }
                              });
    }

    @Override
    public <T> HttpLambda<T> bodyHandler(final HttpResponse.BodyHandler<T> handler) {
        requireNonNull(handler);
        if (reqRetryPolicy != null && reqRetryPredicate != null) {
            return builder -> {
                requireNonNull(builder);
                return IO.effect(() -> requestWrapper(this,
                                                      builder.build(),
                                                      handler
                                                     )
                                )
                         .retry(reqRetryPredicate,
                                reqRetryPolicy
                               );
            };
        }
        if (reqRetryPolicy != null) {
            return builder -> {
                requireNonNull(builder);
                return IO.effect(() -> requestWrapper(this,
                                                      builder.build(),
                                                      handler
                                                     )
                                )
                         .retry(reqRetryPolicy);
            };
        }
        return builder -> {
            requireNonNull(builder);
            return IO.effect(() -> requestWrapper(this,
                                                  builder.build(),
                                                  handler
                                                 )
                            );
        };
    }

    @Override
    public HttpLambda<String> ofString() {
        return ofStringLambda;
    }

    @Override
    public HttpLambda<byte[]> ofBytes() {
        return ofBytesLambda;
    }

    @Override
    public HttpLambda<Void> discarding() {
        return discardingLambda;
    }
}



