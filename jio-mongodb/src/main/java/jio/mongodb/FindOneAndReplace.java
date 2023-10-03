package jio.mongodb;

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
public final class FindOneAndReplace implements BiLambda<JsObj, JsObj, JsObj> {

    private static final FindOneAndReplaceOptions DEFAULT_OPTIONS = new FindOneAndReplaceOptions();
    private final FindOneAndReplaceOptions options;
    private final CollectionSupplier collection;
    private Executor executor;

    private FindOneAndReplace(final CollectionSupplier collection,
                              final FindOneAndReplaceOptions options
                             ) {
        this.collection = requireNonNull(collection);
        this.options = requireNonNull(options);
    }

    /**
     * Creates a new instance of {@code FindOneAndReplace} with the specified MongoDB collection supplier and
     * replacement options.
     *
     * @param collection the supplier of the MongoDB collection to perform the replacement operation
     * @param options    the options to control the replacement operation
     * @return a new {@code FindOneAndReplace} instance with the specified options
     */
    public static FindOneAndReplace of(final CollectionSupplier collection,
                                       final FindOneAndReplaceOptions options
                                      ) {
        return new FindOneAndReplace(collection, options);
    }

    /**
     * Creates a new instance of {@code FindOneAndReplace} with the specified MongoDB collection supplier and default
     * replacement options.
     *
     * @param collection the supplier of the MongoDB collection to perform the replacement operation
     * @return a new {@code FindOneAndReplace} instance with default replacement options
     */
    public static FindOneAndReplace of(final CollectionSupplier collection) {
        return new FindOneAndReplace(collection, DEFAULT_OPTIONS);
    }

    /**
     * Sets the executor to use for performing the find one and replace operation asynchronously.
     *
     * @param executor the executor for asynchronous execution
     * @return this {@code FindOneAndReplace} instance
     */
    public FindOneAndReplace on(final Executor executor) {
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
}
