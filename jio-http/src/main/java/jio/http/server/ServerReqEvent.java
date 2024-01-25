package jio.http.server;

import jdk.jfr.*;


@Label("jio-httpserver-req")
@Name("jio.http.server.Req")
@Category({"JIO", "HTTP"})
@Description("Http request received by the JIO http server and it's response.")
@StackTrace(value = false)
final class ServerReqEvent extends Event {

  @Label("remoteHostAddress")
  String remoteHostAddress;
  @Label("remoteHostPort")
  int remoteHostPort;
  @Label("protocol")
  String protocol;
  @Label("method")
  String method;
  @Label("uri")
  String uri;
  @Label("reqHeaders")
  String reqHeaders = "";
  @Label("statusCode")
  int statusCode;
  @Label("result")
  String result;
  @Label("exception")
  String exception;
  @Label("reqCounter")
  long reqCounter;

  enum RESULT {
    SUCCESS, FAILURE
  }

}
