package jio.http.client;

import static java.util.Objects.requireNonNull;
import static jio.http.client.HttpReqEvent.RESULT.FAILURE;
import static jio.http.client.HttpReqEvent.RESULT.SUCCESS;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Predicate;
import jio.ExceptionFun;
import jio.IO;
import jio.RetryPolicy;

final class JioHttpClientImpl implements JioHttpClient {

  private final AtomicLong counter = new AtomicLong(0);

  private final HttpClient javaClient;

  private final Predicate<Throwable> reqRetryPredicate;
  private final RetryPolicy reqRetryPolicy;
  private final HttpLambda<byte[]> ofBytesLambda;
  private final HttpLambda<Void> discardingLambda;
  private final HttpLambda<String> ofStringLambda;
  private final boolean recordEvents;

  JioHttpClientImpl(final HttpClient.Builder javaClientBuilder,
                    final RetryPolicy reqRetryPolicy,
                    final Predicate<Throwable> reqRetryPredicate,
                    final boolean recordEvents
  ) {
    this.javaClient = requireNonNull(javaClientBuilder).build();
    this.reqRetryPolicy = reqRetryPolicy;
    this.reqRetryPredicate = reqRetryPredicate;
    this.recordEvents = recordEvents;
    this.ofBytesLambda = bodyHandler(HttpResponse.BodyHandlers.ofByteArray());
    this.discardingLambda = bodyHandler(HttpResponse.BodyHandlers.discarding());
    this.ofStringLambda = bodyHandler(HttpResponse.BodyHandlers.ofString());
  }

  <O> HttpResponse<O> requestWrapper(final JioHttpClientImpl myClient,
                                     final HttpRequest request,
                                     final HttpResponse.BodyHandler<O> handler
  ) throws IOException, InterruptedException {

    if (recordEvents) {
      var event = new HttpReqEvent();
      event.reqCounter = myClient.counter.incrementAndGet();
      event.begin();
      try {
        var resp = myClient.javaClient.send(request,
                                            handler
        );
        event.end();
        if (event.shouldCommit()) {
          var uri = request.uri();
          event.host = uri.getHost();
          event.path = uri.getPath();
          event.method = request.method();
          event.statusCode = resp.statusCode();
          event.result = SUCCESS.name();
          event.commit();
        }
        return resp;
      } catch (Exception e) {
        if (event.shouldCommit()) {
          event.exception = ExceptionFun.findUltimateCause(e)
                                        .toString();
          var uri = request.uri();
          event.host = uri.getHost();
          event.path = uri.getPath();
          event.method = request.method();
          event.result = FAILURE.name();
          event.commit();
        }
        throw e;
      }

    } else {
      return myClient.javaClient.send(request,
                                      handler
      );
    }

  }

  @Override
  public <T> HttpLambda<T> bodyHandler(final HttpResponse.BodyHandler<T> handler) {
    requireNonNull(handler);
    if (reqRetryPolicy != null && reqRetryPredicate != null) {
      return requestBuilder -> {
        requireNonNull(requestBuilder);
        return IO.task(() -> requestWrapper(this,
                                            requestBuilder.build(),
                                            handler
        )
        )
                 .retry(reqRetryPredicate,
                        reqRetryPolicy
                 );
      };
    }
    if (reqRetryPolicy != null) {
      return requestBuilder -> {
        requireNonNull(requestBuilder);
        return IO.task(() -> requestWrapper(this,
                                            requestBuilder.build(),
                                            handler
        )
        )
                 .retry(reqRetryPolicy);
      };
    }
    return requestBuilder -> {
      requireNonNull(requestBuilder);
      return IO.task(() -> requestWrapper(this,
                                          requestBuilder.build(),
                                          handler
      )
      );
    };
  }

  @Override
  public void shutdown() {
    javaClient.shutdown();
  }

  @Override
  public void shutdownNow() {
    javaClient.shutdownNow();
  }

  @Override
  public void close() {
    javaClient.close();
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
