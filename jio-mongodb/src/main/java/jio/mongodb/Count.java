package jio.mongodb;

import com.mongodb.client.ClientSession;
import com.mongodb.client.model.CountOptions;
import jio.IO;
import jsonvalues.JsObj;

import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.function.Supplier;

import static java.util.Objects.requireNonNull;
import static jio.mongodb.MongoEvent.OP.COUNT;

/**
 * A class for performing count operations on a MongoDB collection.
 * <p>
 * This class represents a count operation on a MongoDB collection with specified query criteria. The count operation
 * can be executed within a MongoDB client session if one is provided.
 * <p>
 * To use this class effectively, you can configure it with custom count options and an optional executor for running
 * the operation asynchronously.
 *
 * @see MongoLambda
 */
public final class Count extends Op implements MongoLambda<JsObj, Long> {

    private static final CountOptions DEFAULT_OPTIONS = new CountOptions();
    private CountOptions options;

    /**
     * Constructs a new Count instance.
     *
     * @param collection The supplier for the MongoDB collection.
     */
    private Count(final CollectionBuilder collection) {
        super(collection, true);
        options = DEFAULT_OPTIONS;
    }

    /**
     * Creates a Count instance with the specified collection supplier and default count options.
     *
     * @param collection The supplier for the MongoDB collection.
     * @return A Count instance with default options.
     */
    public static Count of(final CollectionBuilder collection) {
        return new Count(collection);
    }

    /**
     * adds the specified mongodb options to this instance
     *
     * @param options the options to perform the operation
     * @return this instance with the new options
     */
    public Count withOptions(final CountOptions options) {
        this.options = requireNonNull(options);
        return this;
    }

    /**
     * Specifies an executor to be used for running the count operation asynchronously.
     *
     * @param executor The executor to use.
     * @return This Count instance for method chaining.
     */
    public Count withExecutor(final Executor executor) {
        this.executor = requireNonNull(executor);
        return this;
    }


    @Override
    public IO<Long> apply(final ClientSession session,
                          final JsObj query
                         ) {
        Objects.requireNonNull(query);
        Supplier<Long> supplier =
                eventWrapper(() -> {
                    var queryBson = Converters.toBson(requireNonNull(query));
                    var collection = requireNonNull(this.collection.get());
                    return session == null ?
                            collection.countDocuments(queryBson, options) :
                            collection.countDocuments(session, queryBson, options);
                }, COUNT);
        return executor == null ?
                IO.managedLazy(supplier) :
                IO.lazy(supplier, executor);
    }


    /**
     * Disables the recording of Java Flight Recorder (JFR) events. When events recording is disabled, the operation
     * will not generate or log JFR events for its operations.
     *
     * @return This operation instance with JFR event recording disabled.
     */
    public Count withoutRecordedEvents() {
        this.recordEvents = false;
        return this;
    }
}
