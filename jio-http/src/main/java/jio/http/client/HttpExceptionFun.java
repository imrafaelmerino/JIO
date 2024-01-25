package jio.http.client;

import java.net.http.HttpConnectTimeoutException;
import jio.ExceptionFun;
import jio.http.client.oauth.AccessTokenNotFound;
import java.net.ConnectException;
import java.net.http.HttpTimeoutException;
import java.util.function.Predicate;

/**
 * A utility class containing predicates to identify specific exceptions that may occur when connecting to a server.
 * These predicates help you classify exceptions, such as timeouts, unresolved hosts, network issues, and more.
 */
public final class HttpExceptionFun {

  private HttpExceptionFun() {
  }


  /**
   * Predicate to check if the given throwable indicates a request timeout. This predicate can be used to filter or
   * handle exceptions related to HTTP request timeouts.
   *
   * <p>The predicate checks if the ultimate cause of the given throwable is an instance of
   * {@link HttpTimeoutException}.</p>
   *
   * @see HttpTimeoutException
   */
  public static final Predicate<Throwable> HAS_REQUEST_TIMEOUT =
      e -> ExceptionFun.findCauseRecursively(cause -> cause instanceof HttpTimeoutException)
                       .apply(e)
                       .isPresent();


  /**
   * Predicate to check if the given throwable indicates a connection timeout. This predicate can be used to filter or
   * handle exceptions related to connection timeouts during HTTP requests.
   *
   * <p>The predicate checks if the ultimate cause of the given throwable is an instance of {@link ConnectException}
   * with the message "HTTP connect timed out".</p>
   *
   * @see ConnectException
   */
  public static final Predicate<Throwable> HAS_CONNECTION_TIMEOUT =
      exc -> ExceptionFun.findCauseRecursively(it -> it instanceof HttpConnectTimeoutException)
                         .apply(exc)
                         .isPresent();

  /**
   * Predicate to check if the given throwable indicates that an OAuth token was not found. This predicate can be used
   * to filter or handle exceptions related to missing OAuth tokens.
   *
   * <p>The predicate checks if the throwable ultimate cause is an instance of {@link AccessTokenNotFound}.</p>
   *
   * @see AccessTokenNotFound
   */
  public static final Predicate<Throwable> HAS_OAUTH_TOKEN_NOT_FOUND =
      exc -> ExceptionFun.findCauseRecursively(it -> it instanceof AccessTokenNotFound)
                         .apply(exc)
                         .isPresent();


}
