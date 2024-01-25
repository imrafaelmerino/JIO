package jio.http.client;

import java.io.IOException;
import java.util.concurrent.Executors;
import jio.ExceptionFun;
import jio.IO;
import jio.RetryPolicy;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Predicate;

import static java.util.Objects.requireNonNull;
import static jio.http.client.HttpReqEvent.RESULT.FAILURE;
import static jio.http.client.HttpReqEvent.RESULT.SUCCESS;

final class JioHttpClientImpl implements JioHttpClient {

  private final AtomicLong counter = new AtomicLong(0);

  private final HttpClient javaClient;

  private final Predicate<Throwable> reqRetryPredicate;
  private final RetryPolicy reqRetryPolicy;
  private final HttpLambda<byte[]> ofBytesLambda;
  private final HttpLambda<Void> discardingLambda;
  private final HttpLambda<String> ofStringLambda;

  private final boolean recordEvents;


  JioHttpClientImpl(final HttpClient.Builder javaClient,
                    final RetryPolicy reqRetryPolicy,
                    final Predicate<Throwable> reqRetryPredicate,
                    final boolean recordEvents
                   ) {
    this.javaClient = requireNonNull(javaClient).build();
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
      event.begin();
      event.uri = request.uri()
                         .toString();
      event.method = request.method();
      event.reqCounter = myClient.counter.incrementAndGet();

      try {
        var resp = myClient.javaClient.send(request,
                                            handler
                                           );
        event.statusCode = resp.statusCode();
        event.result = SUCCESS.name();
        return resp;
      } catch (Exception e) {
        event.exception = ExceptionFun.findUltimateCause(e)
                                      .toString();
        event.result = FAILURE.name();
        throw e;
      } finally {
        event.commit();
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
      return builder -> {
        requireNonNull(builder);
        return IO.task(() -> requestWrapper(this,
                                            builder.build(),
                                            handler
                                           ),
                       Executors.newVirtualThreadPerTaskExecutor()
                      )
                 .retry(reqRetryPredicate,
                        reqRetryPolicy
                       );
      };
    }
    if (reqRetryPolicy != null) {
      return builder -> {
        requireNonNull(builder);
        return IO.task(() -> requestWrapper(this,
                                            builder.build(),
                                            handler
                                           ),
                       Executors.newVirtualThreadPerTaskExecutor()
                      )
                 .retry(reqRetryPolicy);
      };
    }
    return builder -> {
      requireNonNull(builder);
      return IO.task(() -> requestWrapper(this,
                                          builder.build(),
                                          handler
                                         ),
                     Executors.newVirtualThreadPerTaskExecutor()
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



