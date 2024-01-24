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
   * Predicate that returns true when an attempt to invoke a network operation is made upon an unresolved socket
   * address.
   */
  public final static Predicate<Throwable> UNRESOLVED_ADDRESS =
      exc -> Fun.findUltimateCause(exc) instanceof UnresolvedAddressException;

  /**
   * Predicate that returns true when the network connection is unreachable. This could happen due to various reasons,
   * such as disconnected cables, router issues, or problems with the ISP's last-mile connection.
   */
  public final static Predicate<Throwable> NETWORK_UNREACHABLE =
      exc ->
          Fun.findUltimateCause(exc) instanceof SocketException se
              && Objects.equals("Network is unreachable",
                                se.getMessage());

  /**
   * Predicate that returns true when a response is not received from the server within a specified time period.
   */
  public static final Predicate<Throwable> REQUEST_TIMEOUT =
      exc -> exc instanceof HttpTimeoutException;

  /**
   * Predicate that returns true when a connection, over which an HttpRequest is intended to be sent, is not
   * successfully established within a specified time period.
   */
  public static final Predicate<Throwable> CONNECTION_TIMEOUT =
      exc -> Fun.findUltimateCause(exc) instanceof ConnectException ce &&
          Objects.equals("HTTP connect timed out",
                         ce.getMessage());

  /**
   * Predicate that returns true when the authorization token is not found in the server's response.
   */
  public static final Predicate<Throwable> OAUTH_TOKEN_NOT_FOUND =
      exc -> Fun.findUltimateCause(exc) instanceof AccessTokenNotFound;

  /**
   * Predicate that returns true if the server resets the TCP connection during the operation.
   */
  public static final Predicate<Throwable> CONNECTION_RESET =
      exc -> {
        if (exc instanceof IOException) {
          if (Fun.findUltimateCause(exc) instanceof SocketException se) {
            return "connection reset".equalsIgnoreCase(se.getMessage());
          }
        }
        return false;
      };
}
