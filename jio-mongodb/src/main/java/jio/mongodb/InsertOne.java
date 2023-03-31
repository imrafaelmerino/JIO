package jio.mongodb;

import com.mongodb.client.model.InsertOneOptions;
import com.mongodb.client.result.InsertOneResult;
import jio.IO;

import jio.Lambda;
import jsonvalues.JsObj;

import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.function.Function;
import java.util.function.Supplier;

import static java.util.Objects.requireNonNull;
import static jio.mongodb.MongoDBEvent.OP.INSERT_ONE;

public final class InsertOne<R> implements Lambda<JsObj, R> {
    private final CollectionSupplier collection;
    private final InsertOneOptions options;
    private final Function<InsertOneResult, R> resultConverter;
    private static final InsertOneOptions DEFAULT_OPTIONS = new InsertOneOptions();


    private Executor executor;

    public static <R> InsertOne<R> of(final CollectionSupplier collection,
                                      final Function<InsertOneResult, R> resultConverter,
                                      final InsertOneOptions options
                                     ) {
        return new InsertOne<>(collection, resultConverter, options);
    }

    public static <R> InsertOne<R> of(final CollectionSupplier collection,
                                      final Function<InsertOneResult, R> resultConverter
                                     ) {
        return new InsertOne<>(collection,
                               resultConverter,
                               DEFAULT_OPTIONS
        );
    }

    public static InsertOne<JsObj> of(final CollectionSupplier collection
                                     ) {
        return new InsertOne<>(collection,
                               Converters.insertOneResult2JsObj,
                               DEFAULT_OPTIONS
        );
    }


    public InsertOne<R> on(final Executor executor) {
        this.executor = requireNonNull(executor);
        return this;
    }

    public InsertOne(final CollectionSupplier collection,
                     final Function<InsertOneResult, R> resultConverter,
                     final InsertOneOptions options
                    ) {
        this.collection = requireNonNull(collection);
        this.options = requireNonNull(options);
        this.resultConverter = requireNonNull(resultConverter);
    }

    @Override
    public IO<R> apply(final JsObj message) {
        Objects.requireNonNull(message);
        Supplier<R> supplier =
                Fun.jfrEventWrapper(() -> {
                                        var collection = requireNonNull(this.collection.get());
                                        return resultConverter.apply(collection.insertOne(message,
                                                                                          options
                                                                                         )
                                                                    );
                                    },
                                    INSERT_ONE
                                   );
        return executor == null ?
                IO.fromManagedSupplier(supplier) :
                IO.fromSupplier(supplier,
                                executor
                               );


    }
}
