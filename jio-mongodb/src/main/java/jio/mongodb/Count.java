package jio.mongodb;

import com.mongodb.client.model.CountOptions;
import jio.IO;
import jio.Lambda;
import jsonvalues.JsObj;

import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.function.Supplier;

import static java.util.Objects.requireNonNull;
import static jio.mongodb.Converters.jsObj2Bson;
import static jio.mongodb.MongoDBEvent.OP.COUNT;

/**
 * A class for performing count operations on a MongoDB collection.
 */
public final class Count implements Lambda<JsObj, Long> {

    private static final CountOptions DEFAULT_OPTIONS = new CountOptions();
    private final CountOptions options;
    private final CollectionSupplier collection;
    private Executor executor;

    /**
     * Constructs a new Count instance.
     *
     * @param collection The supplier for the MongoDB collection.
     * @param options    The count options.
     */
    private Count(final CollectionSupplier collection, final CountOptions options) {
        this.options = requireNonNull(options);
        this.collection = requireNonNull(collection);
    }

    /**
     * Creates a Count instance with the specified collection supplier and count options.
     *
     * @param collection The supplier for the MongoDB collection.
     * @param options    The count options.
     * @return A Count instance.
     */
    public static Count of(final CollectionSupplier collection, final CountOptions options) {
        return new Count(collection, options);
    }

    /**
     * Creates a Count instance with the specified collection supplier and default count options.
     *
     * @param collection The supplier for the MongoDB collection.
     * @return A Count instance with default options.
     */
    public static Count of(final CollectionSupplier collection) {
        return new Count(collection, DEFAULT_OPTIONS);
    }

    /**
     * Specifies an executor to be used for running the count operation asynchronously.
     *
     * @param executor The executor to use.
     * @return This Count instance for method chaining.
     */
    public Count on(final Executor executor) {
        this.executor = requireNonNull(executor);
        return this;
    }

    /**
     * Performs a count operation on the MongoDB collection based on the provided query.
     *
     * @param query The query for which to count documents.
     * @return An IO operation representing the count result.
     */
    @Override
    public IO<Long> apply(final JsObj query) {
        Objects.requireNonNull(query);
        Supplier<Long> supplier =
                Fun.jfrEventWrapper(() -> {
                    var queryBson = jsObj2Bson.apply(requireNonNull(query));
                    var collection = requireNonNull(this.collection.get());
                    return collection.countDocuments(queryBson, options);
                }, COUNT);
        return executor == null ?
                IO.managedLazy(supplier) :
                IO.lazy(supplier, executor);
    }
}
