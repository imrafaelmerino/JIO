package jio.http.client;

import jdk.jfr.Category;
import jdk.jfr.Description;
import jdk.jfr.Event;
import jdk.jfr.Label;
import jdk.jfr.Name;
import jdk.jfr.StackTrace;

/**
 * Event that is created and written to the Flight Recorder system when a request response is received or an exception
 * happens during the exchange
 */
@Label("Http Exchange Data")
@Name("jio.http.client.Req")
@Category({"JIO", "HTTP"})
@Description("Duration, status code or exception, and other info related to a http requests performed by the jio-httpclient")
@StackTrace(value = false)
final class HttpReqEvent extends Event {

  @Label("URI Path")
  String path;
  /**
   * the method of the request
   */
  @Label("HTTP Method")
  String method;
  /**
   * the uri of the request
   */
  @Label("URI Host")
  String host;

  /**
   * the status code of the response
   */
  @Label("Response Status Code")
  int statusCode;
  /**
   * the result of the exchange: a success if a response is received or an exception
   */
  @Label("Exchange Result")
  String result;
  /**
   * the exception in case of one happens during the exchange
   */
  String exception;
  @Label("HTTP Client Request Counter")
  long reqCounter;

  enum RESULT {
    SUCCESS, FAILURE
  }

}
