package jio.mongodb;

import com.mongodb.client.model.ReplaceOptions;
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
import static jio.mongodb.MongoDBEvent.OP.REPLACE_ONE;

/**
 * A class for performing replace one operations on a MongoDB collection.
 *
 * @param <O> The type of the result.
 */
public final class ReplaceOne<O> implements BiLambda<JsObj, JsObj, O> {
    public static final ReplaceOptions DEFAULT_OPTIONS = new ReplaceOptions();
    private final Function<UpdateResult, O> resultConverter;
    private final CollectionSupplier collection;
    private final ReplaceOptions options;
    private Executor executor;

    /**
     * Constructs a new ReplaceOne instance.
     *
     * @param collection      The supplier for the MongoDB collection.
     * @param resultConverter The function to convert the update result to the desired type.
     * @param options         The replace options.
     */
    private ReplaceOne(final CollectionSupplier collection,
                       final Function<UpdateResult, O> resultConverter,
                       final ReplaceOptions options
                      ) {
        this.resultConverter = requireNonNull(resultConverter);
        this.collection = requireNonNull(collection);
        this.options = requireNonNull(options);
    }

    /**
     * Creates a ReplaceOne instance with the specified collection supplier, result converter, and options.
     *
     * @param collection      The supplier for the MongoDB collection.
     * @param resultConverter The function to convert the update result to the desired type.
     * @param options         The replace options.
     * @param <O>             The type of the result.
     * @return A ReplaceOne instance.
     */
    public static <O> ReplaceOne<O> of(final CollectionSupplier collection,
                                       final Function<UpdateResult, O> resultConverter,
                                       final ReplaceOptions options
                                      ) {
        return new ReplaceOne<>(collection, resultConverter, options);
    }

    /**
     * Creates a ReplaceOne instance with the specified collection supplier and result converter using default options.
     *
     * @param collection      The supplier for the MongoDB collection.
     * @param resultConverter The function to convert the update result to the desired type.
     * @param <O>             The type of the result.
     * @return A ReplaceOne instance with default options.
     */
    public static <O> ReplaceOne<O> of(final CollectionSupplier collection,
                                       final Function<UpdateResult, O> resultConverter
                                      ) {
        return new ReplaceOne<>(collection, resultConverter, DEFAULT_OPTIONS);
    }

    /**
     * Creates a ReplaceOne instance for performing replace one operations on a MongoDB collection with the result as a JsObj.
     *
     * @param collection The supplier for the MongoDB collection.
     * @return A ReplaceOne instance for performing replace one operations with a JsObj result.
     */
    public static ReplaceOne<JsObj> of(final CollectionSupplier collection) {
        return new ReplaceOne<>(collection, Converters.updateResult2JsObj, DEFAULT_OPTIONS);
    }

    /**
     * Specifies an executor to be used for running the replace one operation asynchronously.
     *
     * @param executor The executor to use.
     * @return This ReplaceOne instance for method chaining.
     */
    public ReplaceOne<O> on(final Executor executor) {
        this.executor = requireNonNull(executor);
        return this;
    }

    /**
     * Performs a replace one operation on the MongoDB collection based on the provided filter and update documents.
     *
     * @param filter The filter document to match the document to replace.
     * @param update The replacement document.
     * @return An IO operation representing the result of the replace one operation.
     */
    @Override
    public IO<O> apply(final JsObj filter,
                       final JsObj update
                      ) {
        Objects.requireNonNull(filter);
        Objects.requireNonNull(update);

        Supplier<O> supplier =
                Fun.jfrEventWrapper(() -> {
                                        var collection = requireNonNull(this.collection.get());
                                        return resultConverter.apply(collection.replaceOne(jsObj2Bson.apply(filter),
                                                                                           update,
                                                                                           options
                                                                                          ));
                                    },
                                    REPLACE_ONE
                                   );
        return executor == null ?
                IO.managedLazy(supplier) :
                IO.lazy(supplier, executor);
    }
}
