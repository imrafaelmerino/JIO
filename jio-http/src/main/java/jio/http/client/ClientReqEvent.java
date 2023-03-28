package jio.http.client;

import jdk.jfr.*;

import java.net.URI;

import static java.util.Objects.requireNonNull;

/**
 * Event that is created and written to the Flight Recorder system when
 * a request response is received or an exception during the exchange happens
 */
@Label("httpclient")
@Name("jio.httpclient")
@Category("JIO")
@Description("Http request sent by the JIO http client and it's response.")
final class ClientReqEvent extends Event {

    ClientReqEvent(final String method,
                   final URI uri
                  ) {
        this.method = requireNonNull(method);
        this.uri = requireNonNull(uri).toString();
    }

    enum RESULT {
        SUCCESS, FAILURE
    }

    /**
     * the method of the request
     */
    @Label("method")
    public final String method;

    /**
     * the uri of the request
     */
    @Label("uri")
    public final String uri;

    /**
     * the status code of the response
     */
    @Label("statusCode")
    public int statusCode;

    /**
     * the result of the exchange: a success if a response is received or an exception
     */
    @Label("result")
    public String result;

    /**
     * the exception in case of one happens during the exchange
     */
    @Label("exception")
    public String exception="";

    @Label("reqCounter")
    public long reqCounter;




}
