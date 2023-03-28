package jio.mongodb;

import com.mongodb.client.model.CountOptions;
import jio.IO;
import jio.Lambda;
import jsonvalues.JsObj;

import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.function.Supplier;

import static java.util.Objects.requireNonNull;
import static jio.mongodb.Converters.jsObj2Bson;
import static jio.mongodb.MongoDBEvent.OP.COUNT;


public final class Count implements Lambda<JsObj, Long> {

    private final CountOptions options;
    private final CollectionSupplier collection;
    private static final CountOptions DEFAULT_OPTIONS = new CountOptions();

    private Count(final CollectionSupplier collection,
                  final CountOptions options
                 ) {
        this.options = requireNonNull(options);
        this.collection = requireNonNull(collection);
    }


    private Executor executor;

    public static Count of(final CollectionSupplier collection,
                           final CountOptions options
                          ) {
        return new Count(collection, options);
    }

    public static Count of(final CollectionSupplier collection) {
        return new Count(collection, DEFAULT_OPTIONS);
    }

    public Count on(final Executor executor) {
        this.executor = requireNonNull(executor);
        return this;
    }


    @Override
    public IO<Long> apply(final JsObj query) {
        Objects.requireNonNull(query);
        Supplier<Long> supplier =
                Fun.jfrEventWrapper(() -> {
                              var queryBson = jsObj2Bson.apply(requireNonNull(query));
                              var collection = requireNonNull(this.collection.get());
                              return collection.countDocuments(queryBson,
                                                               options
                                                              );
                          }, COUNT
                                   );
        return executor == null ?
                IO.blockingSupply(supplier) :
                IO.supplyOn(supplier,
                            executor
                           );

    }
}
