package jio.mongodb;

import com.mongodb.client.model.FindOneAndUpdateOptions;
import jio.BiLambda;
import jio.IO;
import jsonvalues.JsObj;

import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.function.Supplier;

import static java.util.Objects.requireNonNull;
import static jio.mongodb.Converters.jsObj2Bson;
import static jio.mongodb.MongoDBEvent.OP.FIND_ONE_AND_UPDATE;

/**
 * Represents a MongoDB find one and update operation to atomically update a single document in a collection
 * asynchronously using {@link jio.BiLambda lambdas}. This class allows you to specify a filter query criteria and an
 * update document as {@link jsonvalues.JsObj}, along with options for controlling the update behavior, such as sort
 * criteria and projection.
 *
 * @see CollectionSupplier
 */
public final class FindOneAndUpdate extends Op implements BiLambda<JsObj, JsObj, JsObj> {

    private static final FindOneAndUpdateOptions DEFAULT_OPTIONS = new FindOneAndUpdateOptions();
    private FindOneAndUpdateOptions options = DEFAULT_OPTIONS;


    private FindOneAndUpdate(final CollectionSupplier collectionSupplier
                            ) {
        super(collectionSupplier, true);
    }

    /**
     * Creates a new instance of {@code FindOneAndUpdate} with the specified MongoDB collection supplier and default
     * update options.
     *
     * @param collectionSupplier the supplier of the MongoDB collection to perform the update operation
     * @return a new {@code FindOneAndUpdate} instance with default update options
     */
    public static FindOneAndUpdate of(final CollectionSupplier collectionSupplier) {
        return new FindOneAndUpdate(collectionSupplier);
    }

    /**
     * @param options the options to perform the operation
     * @return this instance with the new options
     */
    public FindOneAndUpdate withOptions(final FindOneAndUpdateOptions options) {
        this.options = requireNonNull(options);
        return this;
    }

    /**
     * Sets the executor to use for performing the find one and update operation asynchronously.
     *
     * @param executor the executor for asynchronous execution
     * @return this {@code FindOneAndUpdate} instance
     */
    public FindOneAndUpdate withExecutor(final Executor executor) {
        this.executor = requireNonNull(executor);
        return this;
    }

    @Override
    public IO<JsObj> apply(final JsObj filter,
                           final JsObj update
                          ) {
        Objects.requireNonNull(filter);
        Objects.requireNonNull(update);

        Supplier<JsObj> supplier =
                jfrEventWrapper(() -> {
                                    var collection = requireNonNull(this.collection.get());
                                    return collection
                                            .findOneAndUpdate(jsObj2Bson.apply(filter),
                                                              jsObj2Bson.apply(update),
                                                              options
                                                             );
                                },
                                FIND_ONE_AND_UPDATE
                               );
        return executor == null ?
                IO.managedLazy(supplier) :
                IO.lazy(supplier,
                        executor
                       );

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
