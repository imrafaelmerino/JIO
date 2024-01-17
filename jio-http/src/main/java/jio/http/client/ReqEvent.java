package jio.http.client;

import jdk.jfr.*;

import java.net.URI;

import static java.util.Objects.requireNonNull;

/**
 * Event that is created and written to the Flight Recorder system when a request response is received or an exception
 * happens during the exchange
 */
@Label("jio-httpclient-req")
@Name("jio.httpclient")
@Category("JIO")
@Description("Http request sent by jio-http")
final class ReqEvent extends Event {
    static final String METHOD_LABEL = "method";

    static final String URI_LABEL = "uri";

    static final String STATUS_CODE_LABEL = "statusCode";

    static final String RESULT_LABEL = "result";

    static final String REQ_COUNTER_LABEL = "reqCounter";

    static final String EXCEPTION_LABEL = "exception";


    /**
     * the method of the request
     */
    @Label(METHOD_LABEL)
    public final String method;
    /**
     * the uri of the request
     */
    @Label(URI_LABEL)
    public final String uri;
    /**
     * the status code of the response
     */
    @Label(STATUS_CODE_LABEL)
    public int statusCode;
    /**
     * the result of the exchange: a success if a response is received or an exception
     */
    @Label(RESULT_LABEL)
    public String result;
    /**
     * the exception in case of one happens during the exchange
     */
    @Label(EXCEPTION_LABEL)
    public String exception = "";
    @Label(REQ_COUNTER_LABEL)
    public long reqCounter;

    ReqEvent(final String method,
             final URI uri
            ) {
        this.method = requireNonNull(method);
        this.uri = requireNonNull(uri).toString();
    }

    enum RESULT {
        SUCCESS, FAILURE
    }


}
