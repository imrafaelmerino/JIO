package jio.mongodb;

import com.mongodb.MongoSocketReadTimeoutException;
import com.mongodb.MongoTimeoutException;

import java.util.function.Predicate;

public final class Failures {


    public static final Predicate<Throwable> READ_TIMEOUT =
            exc -> exc instanceof MongoSocketReadTimeoutException;

    public static final Predicate<Throwable> CONNECTION_TIMEOUT =
            exc -> exc instanceof MongoTimeoutException;

}
