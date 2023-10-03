package jio.mongodb;

import com.mongodb.client.model.DeleteOptions;
import com.mongodb.client.result.DeleteResult;
import jio.IO;
import jio.Lambda;
import jsonvalues.JsObj;
import org.bson.conversions.Bson;

import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.function.Function;
import java.util.function.Supplier;

import static java.util.Objects.requireNonNull;
import static jio.mongodb.Converters.jsObj2Bson;
import static jio.mongodb.MongoDBEvent.OP.DELETE_ONE;


/**
 * Represents an operation to delete a single document from a MongoDB collection. This class provides flexibility in
 * handling the result and allows you to specify various options for the delete operation.
 *
 * @param <O> The type of result expected from the delete operation.
 */
public final class DeleteOne<O> implements Lambda<JsObj, O> {

    private static final DeleteOptions DEFAULT_OPTIONS = new DeleteOptions();
    private final CollectionSupplier collection;
    private final Function<DeleteResult, O> resultConverter;
    private final DeleteOptions options;
    private Executor executor;

    /**
     * Constructs a {@code DeleteOne} instance with the specified collection, result converter, and delete options.
     *
     * @param collection      The {@code CollectionSupplier} to obtain the MongoDB collection.
     * @param resultConverter A {@code Function} to convert the delete result into the desired type.
     * @param options         The delete options to customize the delete operation.
     */
    private DeleteOne(final CollectionSupplier collection,
                      final Function<DeleteResult, O> resultConverter,
                      final DeleteOptions options
                     ) {
        this.collection = requireNonNull(collection);
        this.resultConverter = requireNonNull(resultConverter);
        this.options = requireNonNull(options);
    }

    /**
     * Creates a new {@code DeleteOne} instance with the specified collection, result converter, and delete options.
     *
     * @param <O>             The type of result expected from the delete operation.
     * @param collection      The {@code CollectionSupplier} to obtain the MongoDB collection.
     * @param resultConverter A {@code Function} to convert the delete result into the desired type.
     * @param options         The delete options to customize the delete operation.
     * @return A new {@code DeleteOne} instance.
     */
    public static <O> DeleteOne<O> of(final CollectionSupplier collection,
                                      final Function<DeleteResult, O> resultConverter,
                                      final DeleteOptions options
                                     ) {
        return new DeleteOne<>(collection, resultConverter, options);
    }

    /**
     * Creates a new {@code DeleteOne} instance with the specified collection and result converter, using default delete
     * options.
     *
     * @param <O>             The type of result expected from the delete operation.
     * @param collection      The {@code CollectionSupplier} to obtain the MongoDB collection.
     * @param resultConverter A {@code Function} to convert the delete result into the desired type.
     * @return A new {@code DeleteOne} instance with default delete options.
     */
    public static <O> DeleteOne<O> of(final CollectionSupplier collection,
                                      final Function<DeleteResult, O> resultConverter
                                     ) {
        return new DeleteOne<>(collection, resultConverter, DEFAULT_OPTIONS);
    }

    /**
     * Creates a new {@code DeleteOne} instance with the specified collection, using default delete options and a result
     * converter for {@code JsObj} result type.
     *
     * @param collection The {@code CollectionSupplier} to obtain the MongoDB collection.
     * @return A new {@code DeleteOne} instance for {@code JsObj} result type.
     */
    public static DeleteOne<JsObj> of(final CollectionSupplier collection) {
        return new DeleteOne<>(collection, Converters.deleteResult2JsObj, DEFAULT_OPTIONS);
    }

    /**
     * Specifies an {@code Executor} on which the delete operation should be executed asynchronously.
     *
     * @param executor The {@code Executor} to use for asynchronous execution.
     * @return This {@code DeleteOne} instance for method chaining.
     */
    public DeleteOne<O> on(final Executor executor) {
        this.executor = requireNonNull(executor);
        return this;
    }

    /**
     * Deletes a single document from the MongoDB collection based on the provided query.
     *
     * @param query The query (as a {@code JsObj}) specifying the document to delete.
     * @return An {@code IO<O>} representing the result of the delete operation.
     * @throws NullPointerException if the query is null.
     */
    @Override
    public IO<O> apply(final JsObj query) {
        Objects.requireNonNull(query);
        Supplier<O> supplier =
                Fun.jfrEventWrapper(() -> {
                                        var collection = requireNonNull(this.collection.get());
                                        final Bson result = jsObj2Bson.apply(requireNonNull(query));
                                        return resultConverter.apply(
                                                collection.deleteOne(result,
                                                                     options
                                                                    )
                                                                    );
                                    }, DELETE_ONE
                                   );
        return executor == null ?
                IO.managedLazy(supplier) :
                IO.lazy(supplier,
                        executor
                       );
    }
}
