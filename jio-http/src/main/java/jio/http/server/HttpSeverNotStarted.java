package jio.http.server;


final class HttpSeverNotStarted extends RuntimeException {

  HttpSeverNotStarted(final Throwable cause) {
    super(cause);
  }
}
