package jio.mongodb;

import com.mongodb.client.model.FindOneAndDeleteOptions;
import com.mongodb.client.model.FindOneAndReplaceOptions;
import jio.BiLambda;
import jio.IO;
import jsonvalues.JsObj;

import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.function.Supplier;

import static java.util.Objects.requireNonNull;
import static jio.mongodb.MongoDBEvent.OP.FIND_ONE_AND_REPLACE;

/**
 * Represents a MongoDB find one and replace operation to update a single document in a collection asynchronously using
 * {@link jio.BiLambda lambdas}. This class allows you to specify a filter query criteria and an update document as
 * {@link jsonvalues.JsObj}, along with options for controlling the replacement behavior, such as sort criteria and
 * projection.
 *
 * @see CollectionSupplier
 */
public final class FindOneAndReplace extends Op implements BiLambda<JsObj, JsObj, JsObj> {

    private static final FindOneAndReplaceOptions DEFAULT_OPTIONS = new FindOneAndReplaceOptions();
    private  FindOneAndReplaceOptions options = DEFAULT_OPTIONS;


    private FindOneAndReplace(final CollectionSupplier collection
                             ) {
        super(collection, true);
    }



    /**
     * @param options the options to perform the operation
     * @return this instance with the new options
     */
    public FindOneAndReplace withOptions(final FindOneAndReplaceOptions options) {
        this.options = requireNonNull(options);
        return this;
    }

    /**
     * Creates a new instance of {@code FindOneAndReplace} with the specified MongoDB collection supplier and default
     * replacement options.
     *
     * @param collection the supplier of the MongoDB collection to perform the replacement operation
     * @return a new {@code FindOneAndReplace} instance with default replacement options
     */
    public static FindOneAndReplace of(final CollectionSupplier collection) {
        return new FindOneAndReplace(collection);
    }

    /**
     * Sets the executor to use for performing the find one and replace operation asynchronously.
     *
     * @param executor the executor for asynchronous execution
     * @return this {@code FindOneAndReplace} instance
     */
    public FindOneAndReplace withExecutor(final Executor executor) {
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
                                            .findOneAndReplace(Converters.jsObj2Bson.apply(filter),
                                                               update,
                                                               options
                                                              );
                                },
                                FIND_ONE_AND_REPLACE
                               );
        return executor == null ?
                IO.managedLazy(supplier) :
                IO.lazy(supplier, executor);

    }

    /**
     * Disables the recording of Java Flight Recorder (JFR) events. When events recording is disabled,
     * the operation will not generate or log JFR events for its operations.
     *
     * @return This operation instance with JFR event recording disabled.
     */
    public FindOneAndReplace withoutRecordedEvents(){
        this.recordEvents = false;
        return this;
    }
}
