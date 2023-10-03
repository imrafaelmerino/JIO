package jio.mongodb;

import com.mongodb.MongoSocketReadTimeoutException;
import com.mongodb.MongoTimeoutException;

import java.util.function.Predicate;

/**
 * Utility class for handling exceptions related to MongoDB operations
 */
public final class Failures {


    /**
     * This predicate checks if the given Throwable is an instance of MongoSocketReadTimeoutException. It returns true
     * if the exception is a read timeout exception and false otherwise. Read timeout exceptions typically occur when a
     * read operation (e.g., reading data from the database) takes longer than the specified timeout
     */
    public static final Predicate<Throwable> READ_TIMEOUT =
            exc -> exc instanceof MongoSocketReadTimeoutException;

    /**
     * This predicate checks if the given Throwable is an instance of MongoTimeoutException. It returns true if the
     * exception is a connection timeout exception and false otherwise. Connection timeout exceptions usually happen
     * when there's a timeout while attempting to establish a connection to the MongoDB server.
     */
    public static final Predicate<Throwable> CONNECTION_TIMEOUT =
            exc -> exc instanceof MongoTimeoutException;

}
