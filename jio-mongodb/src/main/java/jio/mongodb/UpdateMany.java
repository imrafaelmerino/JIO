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
import static jio.mongodb.MongoDBEvent.OP.UPDATE_MANY;

/**
 * A class for performing update many operations on a MongoDB collection.
 *
 * @param <O> The type of the result.
 */
public final class UpdateMany<O> extends Op implements BiLambda<JsObj, JsObj, O> {

    private static final UpdateOptions DEFAULT_OPTIONS = new UpdateOptions();
    private final UpdateOptions options;
    private final Function<UpdateResult, O> resultConverter;

    /**
     * Constructs a new UpdateMany instance.
     *
     * @param collection      The supplier for the MongoDB collection.
     * @param resultConverter The function to convert the update result to the desired type.
     * @param options         The update options.
     */
    private UpdateMany(final CollectionSupplier collection,
                       final Function<UpdateResult, O> resultConverter,
                       final UpdateOptions options
                      ) {
        super(collection, true);
        this.options = requireNonNull(options);
        this.resultConverter = requireNonNull(resultConverter);
    }

    /**
     * Creates an UpdateMany instance with the specified collection supplier and result converter using default
     * options.
     *
     * @param collection      The supplier for the MongoDB collection.
     * @param resultConverter The function to convert the update result to the desired type.
     * @param <O>             The type of the result.
     * @return An UpdateMany instance with default options.
     */
    public static <O> UpdateMany<O> of(final CollectionSupplier collection,
                                       final Function<UpdateResult, O> resultConverter
                                      ) {
        return of(collection, resultConverter, DEFAULT_OPTIONS);
    }

    /**
     * Creates an UpdateMany instance with the specified collection supplier, result converter, and options.
     *
     * @param collection      The supplier for the MongoDB collection.
     * @param resultConverter The function to convert the update result to the desired type.
     * @param options         The update options.
     * @param <O>             The type of the result.
     * @return An UpdateMany instance.
     */
    public static <O> UpdateMany<O> of(final CollectionSupplier collection,
                                       final Function<UpdateResult, O> resultConverter,
                                       final UpdateOptions options
                                      ) {
        return new UpdateMany<>(collection, resultConverter, options);
    }

    /**
     * Specifies an executor to be used for running the update many operation asynchronously.
     *
     * @param executor The executor to use.
     * @return This UpdateMany instance for method chaining.
     */
    public UpdateMany<O> on(final Executor executor) {
        this.executor = requireNonNull(executor);
        return this;
    }

    /**
     * Performs an update many operation on the MongoDB collection based on the provided filter and update documents.
     *
     * @param filter The filter document to match the documents to update.
     * @param update The update document specifying the changes to be made.
     * @return An IO operation representing the result of the update many operation.
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
                                    return resultConverter.apply(collection.updateMany(jsObj2Bson.apply(filter),
                                                                                       jsObj2Bson.apply(update),
                                                                                       options
                                                                                      ));
                                },
                                UPDATE_MANY
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
    public UpdateMany<O> disableRecordEvents(){
        this.recordEvents = false;
        return this;
    }
}
