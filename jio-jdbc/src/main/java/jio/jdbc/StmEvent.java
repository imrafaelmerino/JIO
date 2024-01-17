package jio.jdbc;

import jdk.jfr.*;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Event that is created and written to the Flight Recorder system when a request response is received or an exception
 * happens during the exchange
 */
@Label("jio-jdbc-statement")
@Name("jio.jdbc")
@Category("JIO")
@Description("Database statements performed by jio-jdbc")
final class StmEvent extends Event {

    public static final AtomicLong counter = new AtomicLong(0);

    /**
     * the method of the request
     */
    @Label("sql")
    public final String sql;

    /**
     * the result of the exchange: a success if a response is received or an exception
     */
    @Label("result")
    public String result;
    /**
     * the exception in case of one happens during the exchange
     */
    @Label("exception")
    public String exception = "";

    @Label("opCounter")
    public long opCounter = counter.incrementAndGet();

    public StmEvent(String sql) {
        this.sql = sql;
    }

    enum RESULT {
        SUCCESS, FAILURE
    }


}
