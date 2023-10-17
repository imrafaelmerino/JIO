package jio.mongodb;

import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.result.UpdateResult;
import jio.BiLambda;
import jio.IO;
import jsonvalues.JsObj;

import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.function.Function;
import java.util.function.Supplier;

import static java.util.Objects.requireNonNull;
import static jio.mongodb.Converters.jsObj2Bson;
import static jio.mongodb.MongoDBEvent.OP.UPDATE_ONE;

/**
 * A class for performing update one operations on a MongoDB collection.
 *
 * @param <O> The type of the result.
 */
public final class UpdateOne<O> extends Op implements BiLambda<JsObj, JsObj, O> {

    private static final UpdateOptions DEFAULT_OPTIONS = new UpdateOptions();
    private final Function<UpdateResult, O> resultConverter;
    private UpdateOptions options = DEFAULT_OPTIONS;

    /**
     * Constructs a new UpdateOne instance.
     *
     * @param collection      The supplier for the MongoDB collection.
     * @param resultConverter The function to convert the update result to the desired type.
     */
    private UpdateOne(final CollectionSupplier collection,
                      final Function<UpdateResult, O> resultConverter
                     ) {
        super(collection, true);
        this.resultConverter = requireNonNull(resultConverter);
    }

    /**
     * Creates an UpdateOne instance with the specified collection supplier and result converter using default options.
     *
     * @param collection      The supplier for the MongoDB collection.
     * @param resultConverter The function to convert the update result to the desired type.
     * @param <O>             The type of the result.
     * @return An UpdateOne instance with default options.
     */
    public static <O> UpdateOne<O> of(final CollectionSupplier collection,
                                      final Function<UpdateResult, O> resultConverter
                                     ) {
        return of(collection, resultConverter);
    }

    /**
     * @param options the options to perform the operation
     * @return this instance with the new options
     */
    public UpdateOne<O> withOptions(final UpdateOptions options) {
        this.options = requireNonNull(options);
        return this;
    }

    /**
     * Specifies an executor to be used for running the update one operation asynchronously.
     *
     * @param executor The executor to use.
     * @return This UpdateOne instance for method chaining.
     */
    public UpdateOne<O> withExecutor(final Executor executor) {
        this.executor = requireNonNull(executor);
        return this;
    }

    /**
     * Performs an update one operation on the MongoDB collection based on the provided filter and update documents.
     *
     * @param filter The filter document to match the document to update.
     * @param update The update document specifying the changes to be made.
     * @return An IO operation representing the result of the update one operation.
     */
    @Override
    public IO<O> apply(final JsObj filter,
                       final JsObj update
                      ) {
        Objects.requireNonNull(filter);
        Objects.requireNonNull(update);
        Supplier<O> supplier =
                jfrEventWrapper(() -> {
                                    var collection = requireNonNull(this.collection.get());
                                    return resultConverter.apply(collection.updateOne(jsObj2Bson.apply(filter),
                                                                                      jsObj2Bson.apply(update),
                                                                                      options
                                                                                     )
                                                                );
                                },
                                UPDATE_ONE
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
    public UpdateOne<O> withoutRecordedEvents() {
        this.recordEvents = false;
        return this;
    }
}
