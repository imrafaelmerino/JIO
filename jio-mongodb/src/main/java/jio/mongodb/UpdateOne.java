package jio.mongodb;

import com.mongodb.client.ClientSession;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.result.UpdateResult;
import jio.IO;

import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.function.Supplier;

import static java.util.Objects.requireNonNull;
import static jio.mongodb.Converters.toBson;
import static jio.mongodb.MongoEvent.OP.UPDATE_ONE;

/**
 * A class for performing update one operations on a MongoDB collection.
 * <p>
 * The `UpdateOne` class is designed for performing update operations to modify a single document within a MongoDB
 * collection. It provides flexibility in handling the result and allows you to specify various options for the update
 * operation. You can create instances of this class with the specified collection supplier, and customize the behavior
 * using options such as update options, executors, and more.
 * <p>
 * To use this class effectively, you can set the update options for the operation, specify an executor for asynchronous
 * execution, and disable the recording of Java Flight Recorder (JFR) events if needed. Additionally, you can use the
 * provided `QueryUpdate` object to define the query and update criteria for the operation.
 *
 * @see CollectionBuilder
 * @see QueryUpdate
 */
public final class UpdateOne extends Op implements MongoLambda<QueryUpdate, UpdateResult> {

    private static final UpdateOptions DEFAULT_OPTIONS = new UpdateOptions();
    private UpdateOptions options = DEFAULT_OPTIONS;

    /**
     * Constructs a new UpdateOne instance with the specified collection supplier and default update options.
     *
     * @param collection The supplier for the MongoDB collection.
     */
    private UpdateOne(final CollectionBuilder collection) {
        super(collection, true);
    }

    /**
     * Creates an UpdateOne instance with the specified collection supplier using default options.
     *
     * @param collection The supplier for the MongoDB collection.
     * @return An UpdateOne instance with default options.
     */
    public static UpdateOne of(final CollectionBuilder collection) {
        return new UpdateOne(collection);
    }

    /**
     * Sets the update options to be used for the operation.
     *
     * @param options The options to perform the operation.
     * @return This instance with the new options.
     */
    public UpdateOne withOptions(final UpdateOptions options) {
        this.options = requireNonNull(options);
        return this;
    }

    /**
     * Specifies an executor to be used for running the update one operation asynchronously.
     *
     * @param executor The executor to use for asynchronous execution.
     * @return This UpdateOne instance for method chaining.
     */
    public UpdateOne withExecutor(final Executor executor) {
        this.executor = requireNonNull(executor);
        return this;
    }

    /**
     * Applies the update one operation to the specified MongoDB collection with a query and an update.
     *
     * @param session     The MongoDB client session, or null if not within a session.
     * @param queryUpdate The query and update criteria for the operation.
     * @return An IO representing the result of the update one operation.
     */
    @Override
    public IO<UpdateResult> apply(final ClientSession session, final QueryUpdate queryUpdate) {
        Objects.requireNonNull(queryUpdate);
        Supplier<UpdateResult> supplier = eventWrapper(() -> {
            var collection = requireNonNull(this.collection.build());
            return session == null ?
                    collection.updateOne(toBson(queryUpdate.query()),
                                         toBson(queryUpdate.update()),
                                         options
                                        ) :
                    collection.updateOne(session,
                                         toBson(queryUpdate.query()),
                                         toBson(queryUpdate.update()),
                                         options
                                        );
        }, UPDATE_ONE);
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
    public UpdateOne withoutRecordedEvents() {
        this.recordEvents = false;
        return this;
    }
}
