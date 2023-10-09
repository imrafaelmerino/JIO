package jio.mongodb;

import com.mongodb.client.model.DeleteOptions;
import com.mongodb.client.result.DeleteResult;
import jio.IO;
import jio.Lambda;
import jsonvalues.JsObj;

import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.function.Function;
import java.util.function.Supplier;

import static java.util.Objects.requireNonNull;
import static jio.mongodb.Converters.jsObj2Bson;
import static jio.mongodb.MongoDBEvent.OP.DELETE_MANY;

/**
 * Represents an operation to delete multiple documents from a MongoDB collection. This class provides flexibility in
 * handling the result and allows you to specify various options for the delete operation.
 *
 * @param <O> The type of result expected from the delete operation.
 */
public final class DeleteMany<O> extends Op implements Lambda<JsObj, O> {

    private static final DeleteOptions DEFAULT_OPTIONS = new DeleteOptions();
    private final Function<DeleteResult, O> resultConverter;
    private final DeleteOptions options;

    /**
     * Constructs a {@code DeleteMany} instance with the specified collection, result converter, and delete options.
     *
     * @param collection      The {@code CollectionSupplier} to obtain the MongoDB collection.
     * @param resultConverter A {@code Function} to convert the delete result into the desired type.
     * @param options         The delete options to customize the delete operation.
     */
    private DeleteMany(final CollectionSupplier collection,
                       final Function<DeleteResult, O> resultConverter,
                       final DeleteOptions options
                      ) {
        super(collection, true);
        this.resultConverter = requireNonNull(resultConverter);
        this.options = requireNonNull(options);
    }

    /**
     * Creates a new {@code DeleteMany} instance with the specified collection, result converter, and delete options.
     *
     * @param <O>             The type of result expected from the delete operation.
     * @param collection      The {@code CollectionSupplier} to obtain the MongoDB collection.
     * @param resultConverter A {@code Function} to convert the delete result into the desired type.
     * @param options         The delete options to customize the delete operation.
     * @return A new {@code DeleteMany} instance.
     */
    public static <O> DeleteMany<O> of(final CollectionSupplier collection,
                                       final Function<DeleteResult, O> resultConverter,
                                       final DeleteOptions options
                                      ) {
        return new DeleteMany<>(collection, resultConverter, options);
    }

    /**
     * Creates a new {@code DeleteMany} instance with the specified collection and result converter, using default
     * delete options.
     *
     * @param <O>             The type of result expected from the delete operation.
     * @param collection      The {@code CollectionSupplier} to obtain the MongoDB collection.
     * @param resultConverter A {@code Function} to convert the delete result into the desired type.
     * @return A new {@code DeleteMany} instance with default delete options.
     */
    public static <O> DeleteMany<O> of(final CollectionSupplier collection,
                                       final Function<DeleteResult, O> resultConverter
                                      ) {
        return new DeleteMany<>(collection, resultConverter, DEFAULT_OPTIONS);
    }

    /**
     * Creates a new {@code DeleteMany} instance with the specified collection, using default delete options and a
     * result converter for {@code JsObj} result type.
     *
     * @param collection The {@code CollectionSupplier} to obtain the MongoDB collection.
     * @return A new {@code DeleteMany} instance for {@code JsObj} result type.
     */
    public static DeleteMany<JsObj> of(final CollectionSupplier collection) {
        return new DeleteMany<>(collection, Converters.deleteResult2JsObj, DEFAULT_OPTIONS);
    }

    /**
     * Specifies an {@code Executor} on which the delete operation should be executed asynchronously.
     *
     * @param executor The {@code Executor} to use for asynchronous execution.
     * @return This {@code DeleteMany} instance for method chaining.
     */
    public DeleteMany<O> on(final Executor executor) {
        this.executor = requireNonNull(executor);
        return this;
    }

    /**
     * Deletes multiple documents from the MongoDB collection based on the provided query.
     *
     * @param query The query (as a {@code JsObj}) specifying the documents to delete.
     * @return An {@code IO<O>} representing the result of the delete operation.
     * @throws NullPointerException if the query is null.
     */
    @Override
    public IO<O> apply(final JsObj query) {
        Objects.requireNonNull(query);
        Supplier<O> supplier =
                jfrEventWrapper(() -> {
                                    var collection = requireNonNull(this.collection.get());
                                    final DeleteResult result =
                                            collection.deleteMany(jsObj2Bson.apply(query),
                                                                  options
                                                                 );
                                    return resultConverter.apply(result);
                                },
                                DELETE_MANY
                               );
        return executor == null ?
                IO.managedLazy(supplier) :
                IO.lazy(supplier,
                        executor
                       );
    }

    /**
     * Disables the recording of Java Flight Recorder (JFR) events. When events recording is disabled,
     * the operation will not generate or log JFR events for its operations.
     *
     * @return This operation instance with JFR event recording disabled.
     */
    public DeleteMany<O> disableRecordEvents(){
        this.recordEvents = false;
        return this;
    }
}
