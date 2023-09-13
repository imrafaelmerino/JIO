package jio.http;

import jio.http.client.oauth.AccessTokenNotFound;

import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketException;
import java.net.http.HttpConnectTimeoutException;
import java.net.http.HttpTimeoutException;
import java.nio.channels.UnresolvedAddressException;
import java.util.Objects;
import java.util.function.Predicate;

/**
 * Class with different predicates to identify specify exceptions that comes up when
 * connecting to a server: timeouts, unresolved hosts etc
 */
public final class HttpExceptions {

    /**
     * true when an attempt is made to invoke a network operation upon an unresolved
     * socket address.
     */
    public final static Predicate<Throwable> UNRESOLVED_SOCKET_ADDRESS =
            exc -> exc instanceof ConnectException c
                    && c.getCause() instanceof UnresolvedAddressException;


    /**
     * Your connection to the network in question (presumably your Internet connection in this case) is not working,
     * either because a cable is unplugged, a router is turned off or misconfigured, the last mile connection to your
     * ISP is down, etc
     */
    public final static Predicate<Throwable> NETWORK_UNREACHABLE =
            exc ->
                    exc instanceof ConnectException c
                            && c.getCause() instanceof SocketException
                            && Objects.equals("Network is unreachable", exc.getMessage());

    /**
     * true when a response is not received within a specified time period.
     */
    public static final Predicate<Throwable> REQUEST_TIMEOUT =
            exc -> exc instanceof HttpTimeoutException;

    /**
     * true when a connection, over which an HttpRequest is intended to be sent, is not
     * successfully established within a specified time period.
     */
    public static final Predicate<Throwable> CONNECTION_TIMEOUT =
            exc -> exc instanceof HttpConnectTimeoutException;
    /**
     * true when the authorization token is not found in the server response
     */
    public static final Predicate<Throwable> OAUTH_TOKEN_NOT_FOUND =
            exc -> exc instanceof AccessTokenNotFound;


    /**
     * true if the server reset the tcp connection
     */
    public static final Predicate<Throwable> CONNECTION_RESET =
            exc -> {
                if (exc instanceof IOException) {
                    if (exc.getCause() != null && exc.getCause() instanceof SocketException se) {
                        return "connection reset".equalsIgnoreCase(se.getMessage());
                    }
                }
                return false;
            };



}
