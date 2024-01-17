package jio.jdbc;

import jdk.jfr.*;

import java.util.Objects;
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

    static final AtomicLong counter = new AtomicLong(0);
    static final String OP_COUNTER_LABEL = "opCounter";
    static final String RESULT_LABEL = "result";
    static final String SQL_LABEL = "sql";
    static final String EXCEPTION_LABEL = "exception";

    /**
     * the method of the request
     */
    @Label(SQL_LABEL)
    public final String sql;

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

    @Label(OP_COUNTER_LABEL)
    public long opCounter = counter.incrementAndGet();

    public StmEvent(String sql) {
        this.sql = Objects.requireNonNull(sql);
    }

    enum RESULT {
        SUCCESS, FAILURE
    }


}
