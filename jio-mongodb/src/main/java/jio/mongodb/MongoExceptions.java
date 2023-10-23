package jio.mongodb;

import com.mongodb.MongoException;
import com.mongodb.MongoSocketReadTimeoutException;
import com.mongodb.MongoTimeoutException;
import fun.optic.Prism;

import java.util.Optional;
import java.util.function.Predicate;

/**
 * Utility class for handling exceptions related to MongoDB operations
 */
public final class MongoExceptions {


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

    /**
     * A Prism for capturing and handling MongoDB exceptions. This Prism allows you to extract a {@link MongoException}
     * from a {@link Throwable}.
     *
     * <pre>
     *  {@code
     *
     *  // Create a Predicate that returns true in case of Duplicate Key Error (Error Code 11000)
     *   Predicate<Throwable> isDuplicatedKey = MONGO_EXCEPTION_PRISM.exists.apply(exc -> exc.getCode() == 11000);
     *
     *  }
     *
     * </pre>
     *
     * @see <a
     * href="https://github.com/mongodb/mongo/blob/34228dcee8b2961fb3f5d84e726210d6faf2ef4f/src/mongo/base/error_codes.yml">MongoDB
     * Error Codes</a>
     * @see fun.optic.Prism
     */
    public static final Prism<Throwable, MongoException> MONGO_EXCEPTION_PRISM =
            new Prism<>(e -> e instanceof MongoException m ?
                    Optional.of(m) :
                    Optional.empty(), m -> m);

}
