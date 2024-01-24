package jio.http.client;

import jdk.jfr.*;


/**
 * Event that is created and written to the Flight Recorder system when a request response is received or an exception
 * happens during the exchange
 */
@Label("Http Exchange Info")
@Name("jio.http.client.Req")
@Category({"JIO", "HTTP"})
@Description("Duration, status code or exceptions and other info related to a http req performed by the  jio-httpclient")
@StackTrace(value = false)
final class HttpReqEvent extends Event {

  /**
   * the method of the request
   */
  String method;
  /**
   * the uri of the request
   */
  String uri;
  /**
   * the status code of the response
   */
  int statusCode;
  /**
   * the result of the exchange: a success if a response is received or an exception
   */
  String result;
  /**
   * the exception in case of one happens during the exchange
   */
  String exception;
  long reqCounter;

  enum RESULT {
    SUCCESS, FAILURE
  }


}
