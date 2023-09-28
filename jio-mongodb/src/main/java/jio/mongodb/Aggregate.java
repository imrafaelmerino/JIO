package jio.mongodb;

import com.mongodb.client.AggregateIterable;
import jio.IO;
import jio.Lambda;
import jsonvalues.JsArray;
import jsonvalues.JsObj;

import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.function.Function;
import java.util.function.Supplier;

import static java.util.Objects.requireNonNull;
import static jio.mongodb.Converters.jsArray2ListOfBson;
import static jio.mongodb.MongoDBEvent.OP.AGGREGATE;

/**
 * A class for performing aggregation operations on a MongoDB collection.
 *
 * @param <O> The type of the result after aggregation.
 */
public final class Aggregate<O> implements Lambda<JsArray, O> {

    /**
     * The function to convert the aggregate iterable result to the desired type.
     */
    public final Function<AggregateIterable<JsObj>, O> resultConverter;

    /**
     * The supplier for the MongoDB collection.
     */
    public final CollectionSupplier collection;

    /**
     * The executor to run the aggregation operation.
     */
    private Executor executor;

    /**
     * Constructs a new Aggregate instance with the specified collection supplier and result converter function.
     *
     * @param collection      The supplier for the MongoDB collection.
     * @param resultConverter The function to convert the aggregate iterable result to the desired type.
     */
    public Aggregate(final CollectionSupplier collection,
                     final Function<AggregateIterable<JsObj>, O> resultConverter
                    ) {
        this.resultConverter = requireNonNull(resultConverter);
        this.collection = requireNonNull(collection);
    }

    /**
     * Sets the executor for running the aggregation operation.
     *
     * @param executor The executor for running the aggregation operation.
     * @return This Aggregate instance with the executor set.
     */
    public Aggregate<O> on(final Executor executor) {
        this.executor = requireNonNull(executor);
        return this;
    }

    /**
     * Applies the aggregation stages to the MongoDB collection and returns the result as an IO.
     *
     * @param stages The aggregation stages to apply.
     * @return An IO representing the result of the aggregation operation.
     */
    @Override
    public IO<O> apply(final JsArray stages) {
        Objects.requireNonNull(stages);
        Supplier<O> supplier =
                Fun.jfrEventWrapper(() -> {
                                        var pipeline = jsArray2ListOfBson.apply(stages);
                                        var collection = requireNonNull(this.collection.get());
                                        return resultConverter.apply(collection.aggregate(pipeline));
                                    },
                                    AGGREGATE
                                   );

        return executor == null ?
                IO.managedLazy(supplier) :
                IO.lazy(supplier,
                        executor
                       );
    }
}
