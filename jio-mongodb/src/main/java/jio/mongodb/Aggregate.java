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


public final class Aggregate<O> implements Lambda<JsArray, O> {

    public final Function<AggregateIterable<JsObj>, O> resultConverter;
    public final CollectionSupplier collection;
    private Executor executor;

    public Aggregate(final CollectionSupplier collection,
                     final Function<AggregateIterable<JsObj>, O> resultConverter
                    ) {
        this.resultConverter = requireNonNull(resultConverter);
        this.collection = requireNonNull(collection);
    }

    public Aggregate<O> on(final Executor executor) {
        this.executor = requireNonNull(executor);
        return this;
    }

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
