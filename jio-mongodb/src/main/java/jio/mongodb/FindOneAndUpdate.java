package jio.mongodb;

import com.mongodb.client.ClientSession;
import com.mongodb.client.model.FindOneAndUpdateOptions;
import jio.IO;
import jsonvalues.JsObj;

import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.function.Supplier;

import static java.util.Objects.requireNonNull;
import static jio.mongodb.Converters.toBson;
import static jio.mongodb.MongoEvent.OP.FIND_ONE_AND_UPDATE;

/**
 * Represents a MongoDB find one and update operation to atomically update a single document in a collection
 * asynchronously using {@link jio.BiLambda lambdas}. This class allows you to specify a filter query criteria and an
 * update document as {@link jsonvalues.JsObj}, along with options for controlling the update behavior, such as sort
 * criteria and projection.
 * <p>
 * The `FindOneAndUpdate` class is designed for updating a single document in a MongoDB collection that matches the
 * specified filter criteria. This operation is performed atomically and asynchronously, allowing you to customize the
 * update options and execution behavior. You can create instances of this class with the specified collection supplier,
 * set update options, and choose an executor for asynchronous execution.
 * <p>
 * To use this class effectively, you can set the update options for the operation, specify an executor for asynchronous
 * execution, and disable the recording of Java Flight Recorder (JFR) events if needed. The find one and update
 * operation requires a query filter as well as an update document to modify the matched document.
 *
 * @see CollectionBuilder
 */
public final class FindOneAndUpdate extends Op implements MongoLambda<QueryUpdate, JsObj> {

    private static final FindOneAndUpdateOptions DEFAULT_OPTIONS = new FindOneAndUpdateOptions();
    private FindOneAndUpdateOptions options = DEFAULT_OPTIONS;

    /**
     * Constructs a new `FindOneAndUpdate` instance with the specified collection supplier and default update options.
     *
     * @param collectionBuilder The supplier of the MongoDB collection.
     */
    private FindOneAndUpdate(final CollectionBuilder collectionBuilder) {
        super(collectionBuilder, true);
    }

    /**
     * Creates a new instance of `FindOneAndUpdate` with the specified MongoDB collection supplier and default update
     * options.
     *
     * @param collectionBuilder The supplier of the MongoDB collection to perform the update operation.
     * @return A new `FindOneAndUpdate` instance with default update options.
     */
    public static FindOneAndUpdate of(final CollectionBuilder collectionBuilder) {
        return new FindOneAndUpdate(collectionBuilder);
    }

    /**
     * Sets the update options to be used for the operation.
     *
     * @param options The options to perform the operation.
     * @return This instance with the new options.
     */
    public FindOneAndUpdate withOptions(final FindOneAndUpdateOptions options) {
        this.options = requireNonNull(options);
        return this;
    }

    /**
     * Specifies an executor to be used for running the find one and update operation asynchronously.
     *
     * @param executor The executor for asynchronous execution.
     * @return This `FindOneAndUpdate` instance.
     */
    public FindOneAndUpdate withExecutor(final Executor executor) {
        this.executor = requireNonNull(executor);
        return this;
    }

    /**
     * Applies the find one and update operation to the specified MongoDB collection with the provided query and
     * update.
     *
     * @param session     The MongoDB client session, or null if not within a session.
     * @param queryUpdate The query and update information.
     * @return An IO representing the result of the find one and update operation.
     */
    @Override
    public IO<JsObj> apply(final ClientSession session, final QueryUpdate queryUpdate) {
        Objects.requireNonNull(queryUpdate);
        Supplier<JsObj> supplier =
                eventWrapper(() -> {
                                 var collection = requireNonNull(this.collection.build());
                                 return session == null ?
                                         collection
                                                 .findOneAndUpdate(toBson(queryUpdate.query()),
                                                                   toBson(queryUpdate.update()),
                                                                   options
                                                                  ) :
                                         collection
                                                 .findOneAndUpdate(session,
                                                                   toBson(queryUpdate.query()),
                                                                   toBson(queryUpdate.update()),
                                                                   options
                                                                  );
                             }, FIND_ONE_AND_UPDATE
                            );
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
    public FindOneAndUpdate withoutRecordedEvents() {
        this.recordEvents = false;
        return this;
    }
}
