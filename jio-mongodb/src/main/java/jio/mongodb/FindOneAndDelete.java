package jio.mongodb;

import com.mongodb.client.model.FindOneAndDeleteOptions;
import jio.IO;
import jio.Lambda;
import jsonvalues.JsObj;

import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.function.Supplier;

import static java.util.Objects.requireNonNull;
import static jio.mongodb.Converters.jsObj2Bson;
import static jio.mongodb.MongoDBEvent.OP.FIND_ONE_AND_DELETE;

/**
 * Represents a MongoDB find one and delete operation to remove a single document from a collection asynchronously using
 * {@link jio.Lambda lambdas}. This class allows you to specify query criteria as a {@link jsonvalues.JsObj} and
 * provides options for controlling the behavior of the deletion operation, such as sort criteria and projection.
 *
 * @see CollectionSupplier
 */
public final class FindOneAndDelete extends Op implements Lambda<JsObj, JsObj> {

    private static final FindOneAndDeleteOptions DEFAULT_OPTIONS = new FindOneAndDeleteOptions();
    private final FindOneAndDeleteOptions options;

    private FindOneAndDelete(final CollectionSupplier collection,
                             final FindOneAndDeleteOptions options
                            ) {
        super(collection, true);
        this.options = requireNonNull(options);
    }

    /**
     * Creates a new instance of {@code FindOneAndDelete} with the specified MongoDB collection supplier and default
     * deletion options.
     *
     * @param collection the supplier of the MongoDB collection to perform the deletion operation
     * @param options the options to control the operation
     * @return a new {@code FindOneAndDelete} instance with default deletion options
     */
    public static FindOneAndDelete of(final CollectionSupplier collection,
                                      final FindOneAndDeleteOptions options
                                     ) {
        return new FindOneAndDelete(collection, options);
    }

    /**
     * Creates a new instance of {@code FindOneAndDelete} with the specified MongoDB collection supplier and default
     * deletion options.
     *
     * @param collection the supplier of the MongoDB collection to perform the deletion operation
     * @return a new {@code FindOneAndDelete} instance with default deletion options
     */
    public static FindOneAndDelete of(final CollectionSupplier collection) {
        return new FindOneAndDelete(collection, DEFAULT_OPTIONS);
    }

    /**
     * Sets the executor to use for performing the find one and delete operation asynchronously.
     *
     * @param executor the executor for asynchronous execution
     * @return this {@code FindOneAndDelete} instance
     */
    public FindOneAndDelete on(final Executor executor) {
        this.executor = Objects.requireNonNull(executor);
        return this;
    }

    @Override
    public IO<JsObj> apply(final JsObj query) {
        Objects.requireNonNull(query);
        Supplier<JsObj> supplier =
                jfrEventWrapper(() -> {
                                    var collection = requireNonNull(this.collection.get());
                                    return collection.findOneAndDelete(jsObj2Bson.apply(query),
                                                                       options
                                                                      );
                                },
                                FIND_ONE_AND_DELETE
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
    public FindOneAndDelete disableRecordEvents(){
        this.recordEvents = false;
        return this;
    }
}
