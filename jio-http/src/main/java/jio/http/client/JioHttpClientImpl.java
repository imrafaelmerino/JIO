package jio.http.client;

import static java.util.Objects.requireNonNull;
import static jio.http.client.HttpReqEvent.RESULT.FAILURE;
import static jio.http.client.HttpReqEvent.RESULT.SUCCESS;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Predicate;
import jio.ExceptionFun;
import jio.IO;
import jio.RetryPolicy;

final class JioHttpClientImpl implements JioHttpClient {

  private final AtomicLong counter = new AtomicLong(0);

  private final HttpClient client;

  private final Predicate<Throwable> reqRetryPredicate;
  private final RetryPolicy reqRetryPolicy;
  private final HttpLambda<byte[]> ofBytesLambda;
  private final HttpLambda<Void> discardingLambda;
  private final HttpLambda<String> ofStringLambda;

  private final boolean recordEvents;

  JioHttpClientImpl(final HttpClient.Builder client,
                    RetryPolicy reqRetryPolicy,
                    Predicate<Throwable> reqRetryPredicate,
                    boolean recordEvents
                   ) {
    this.client = requireNonNull(client).build();
    this.reqRetryPolicy = reqRetryPolicy;
    this.reqRetryPredicate = reqRetryPredicate;
    this.recordEvents = recordEvents;
    this.ofBytesLambda = bodyHandler(HttpResponse.BodyHandlers.ofByteArray());
    this.discardingLambda = bodyHandler(HttpResponse.BodyHandlers.discarding());
    this.ofStringLambda = bodyHandler(HttpResponse.BodyHandlers.ofString());
  }

  <O> CompletableFuture<HttpResponse<O>> requestWrapper(final JioHttpClientImpl myClient,
                                                        final HttpRequest request,
                                                        final HttpResponse.BodyHandler<O> handler
                                                       ) {

    if (recordEvents) {

      HttpReqEvent event = new HttpReqEvent();
      event.begin();

      event.reqCounter = myClient.counter.incrementAndGet();

      return myClient.client.sendAsync(request,
                                       handler
                                      )
                            .whenComplete((resp,
                                           failure) -> {
                              event.end();
                              if (event.shouldCommit()) {
                                var uri = request.uri();
                                event.host = uri.getHost();
                                event.path = uri.getPath();
                                event.method = request.method();
                                event.result = SUCCESS.name();
                                if (resp != null) {
                                  event.statusCode = resp.statusCode();
                                  event.result = SUCCESS.name();
                                } else {
                                  Throwable cause = ExceptionFun.findUltimateCause(failure);
                                  event.exception = String.format("%s:%s",
                                                                  cause.getClass()
                                                                       .getName(),
                                                                  cause.getMessage()
                                                                 );
                                  event.result = FAILURE.name();
                                }
                                event.commit();
                              }
                            });
    } else {
      return myClient.client.sendAsync(request,
                                       handler
                                      );
    }
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
