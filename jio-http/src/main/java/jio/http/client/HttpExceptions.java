package jio.http.client;

import jio.Fun;
import jio.http.client.oauth.AccessTokenNotFound;
import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketException;
import java.net.http.HttpTimeoutException;
import java.nio.channels.UnresolvedAddressException;
import java.util.Objects;
import java.util.function.Predicate;

/**
 * A utility class containing predicates to identify specific exceptions that may occur when connecting to a server.
 * These predicates help you classify exceptions, such as timeouts, unresolved hosts, network issues, and more.
 */
public final class HttpExceptions {

  private HttpExceptions() {
  }

  /**
   * Predicate to check if the given throwable indicates an unresolved address error. This predicate can be used to
   * filter or handle exceptions related to unresolved addresses.
   *
   * <p>The predicate checks if the ultimate cause of the given throwable is an instance of
   * {@link UnresolvedAddressException}.</p>
   *
   * @see UnresolvedAddressException
   * @see Fun#findUltimateCause(Throwable)
   */
  public final static Predicate<Throwable> IS_UNRESOLVED_ADDRESS =
      exc -> Fun.findUltimateCause(exc) instanceof UnresolvedAddressException;

  /**
   * Predicate to check if the given throwable indicates a network-unreachable error. This predicate can be used to
   * filter or handle exceptions related to network reachability issues.
   *
   * <p>The predicate checks if the ultimate cause of the given throwable is an instance of {@link SocketException}
   * with the message "Network is unreachable".</p>
   *
   * @see SocketException
   * @see Fun#findUltimateCause(Throwable)
   */
  public final static Predicate<Throwable> IS_NETWORK_UNREACHABLE =
      exc ->
          Fun.findUltimateCause(exc) instanceof SocketException se
              && Objects.equals("Network is unreachable",
                                se.getMessage());


  /**
   * Predicate to check if the given throwable indicates a request timeout. This predicate can be used to filter or
   * handle exceptions related to HTTP request timeouts.
   *
   * <p>The predicate checks if the ultimate cause of the given throwable is an instance of
   * {@link HttpTimeoutException}.</p>
   *
   * @see HttpTimeoutException
   * @see Fun#findUltimateCause(Throwable)
   */
  public static final Predicate<Throwable> IS_REQUEST_TIMEOUT =
      exc -> Fun.findUltimateCause(exc) instanceof HttpTimeoutException;


  /**
   * Predicate to check if the given throwable indicates a connection timeout. This predicate can be used to filter or
   * handle exceptions related to connection timeouts during HTTP requests.
   *
   * <p>The predicate checks if the ultimate cause of the given throwable is an instance of {@link ConnectException}
   * with the message "HTTP connect timed out".</p>
   *
   * @see ConnectException
   * @see Fun#findUltimateCause(Throwable)
   */
  public static final Predicate<Throwable> IS_CONNECTION_TIMEOUT =
      exc -> Fun.findUltimateCause(exc) instanceof ConnectException ce &&
          Objects.equals("HTTP connect timed out",
                         ce.getMessage());

  /**
   * Predicate to check if the given throwable indicates that an OAuth token was not found. This predicate can be used
   * to filter or handle exceptions related to missing OAuth tokens.
   *
   * <p>The predicate checks if the throwable's ultimate cause is an instance of {@link AccessTokenNotFound}.</p>
   *
   * @see AccessTokenNotFound
   * @see Fun#findUltimateCause(Throwable)
   */
  public static final Predicate<Throwable> IS_OAUTH_TOKEN_NOT_FOUND =
      exc -> Fun.findUltimateCause(exc) instanceof AccessTokenNotFound;

  /**
   * Predicate to check if the given throwable indicates a connection reset. This predicate can be used to filter or
   * handle exceptions related to connection resets during IO operations.
   *
   * <p>The predicate checks if the throwable is an instance of {@link IOException} and its ultimate cause
   * is a {@link SocketException} with the message "connection reset".</p>
   *
   * @see IOException
   * @see SocketException
   * @see Fun#findUltimateCause(Throwable)
   */
  public static final Predicate<Throwable> IS_CONNECTION_RESET =
      exc -> {
        if (exc instanceof IOException) {
          if (Fun.findUltimateCause(exc) instanceof SocketException se) {
            return "connection reset".equalsIgnoreCase(se.getMessage());
          }
        }
        return false;
      };
}
