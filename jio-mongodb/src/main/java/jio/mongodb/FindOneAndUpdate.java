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
 * Represents a MongoDB find one and update operation to atomically update a single document in a collection asynchronously using {@link jio.BiLambda lambdas}.
 * This class allows you to specify a filter query criteria and an update document as {@link jsonvalues.JsObj}, along with options
 * for controlling the update behavior, such as sort criteria and projection.
 *
 * @see CollectionSupplier
 */
public final class FindOneAndUpdate implements BiLambda<JsObj, JsObj, JsObj> {

    private static final FindOneAndUpdateOptions DEFAULT_OPTIONS = new FindOneAndUpdateOptions();
    private final FindOneAndUpdateOptions options;
    private final CollectionSupplier collectionSupplier;
    private Executor executor;


    private FindOneAndUpdate(final CollectionSupplier collectionSupplier,
                             final FindOneAndUpdateOptions options
                            ) {
        this.collectionSupplier = requireNonNull(collectionSupplier);
        this.options = requireNonNull(options);
    }
    /**
     * Creates a new instance of {@code FindOneAndUpdate} with the specified MongoDB collection supplier and update options.
     *
     * @param collectionSupplier the supplier of the MongoDB collection to perform the update operation
     * @param options            the options to control the update operation
     * @return a new {@code FindOneAndUpdate} instance with the specified options
     */
    public static FindOneAndUpdate of(final CollectionSupplier collectionSupplier,
                                      final FindOneAndUpdateOptions options
                                     ) {
        return new FindOneAndUpdate(collectionSupplier, options);
    }
    /**
     * Creates a new instance of {@code FindOneAndUpdate} with the specified MongoDB collection supplier and default update options.
     *
     * @param collectionSupplier the supplier of the MongoDB collection to perform the update operation
     * @return a new {@code FindOneAndUpdate} instance with default update options
     */
    public static FindOneAndUpdate of(final CollectionSupplier collectionSupplier) {
        return new FindOneAndUpdate(collectionSupplier, DEFAULT_OPTIONS);
    }

    /**
     * Sets the executor to use for performing the find one and update operation asynchronously.
     *
     * @param executor the executor for asynchronous execution
     * @return this {@code FindOneAndUpdate} instance
     */
    public FindOneAndUpdate on(final Executor executor) {
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
                Fun.jfrEventWrapper(() -> {
                                        var collection = requireNonNull(this.collectionSupplier.get());
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


}
