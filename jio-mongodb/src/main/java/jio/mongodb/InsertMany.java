package jio.mongodb;

import com.mongodb.client.model.InsertManyOptions;
import com.mongodb.client.result.InsertManyResult;
import jio.IO;
import jio.Lambda;
import jsonvalues.JsArray;


import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.function.Function;
import java.util.function.Supplier;

import static java.util.Objects.requireNonNull;
import static jio.mongodb.Converters.jsArray2ListOfJsObj;
import static jio.mongodb.MongoDBEvent.OP.INSERT_MANY;


public final class InsertMany<R> implements Lambda<JsArray, R> {

    private final CollectionSupplier collection;
    private final InsertManyOptions options;
    private final Function<InsertManyResult, R> resultConverter;

    private static final InsertManyOptions DEFAULT_OPTIONS = new InsertManyOptions();


    private InsertMany(final CollectionSupplier collection,
                       final Function<InsertManyResult, R> resultConverter,
                       final InsertManyOptions options
                      ) {
        this.collection = requireNonNull(collection);
        this.options = requireNonNull(options);
        this.resultConverter = requireNonNull(resultConverter);
    }

    private Executor executor;

    public static <R> InsertMany<R> of(final CollectionSupplier collection,
                                       final Function<InsertManyResult, R> resultConverter,
                                       final InsertManyOptions options
                                      ) {
        return new InsertMany<>(collection, resultConverter, options);
    }

    public static <R> InsertMany<R> of(final CollectionSupplier collection,
                                       final Function<InsertManyResult, R> resultConverter
                                      ) {
        return new InsertMany<>(collection, resultConverter, DEFAULT_OPTIONS);
    }

    public static InsertMany<JsArray> of(final CollectionSupplier collection
                                        ) {
        return new InsertMany<>(collection, Converters.insertManyResult2JsArrayOfHexIds, DEFAULT_OPTIONS);
    }

    public InsertMany<R> on(final Executor executor) {
        this.executor = Objects.requireNonNull(executor);
        return this;
    }

    @Override
    public IO<R> apply(final JsArray message) {
        Objects.requireNonNull(message);
        var event = new MongoDBEvent(INSERT_MANY);
        event.begin();
        Supplier<R> supplier =
                Fun.jfrEventWrapper(() -> {
                              var docs = jsArray2ListOfJsObj.apply(message);
                              var col = requireNonNull(collection.get());
                              return resultConverter.apply(col
                                                                   .insertMany(docs,
                                                                               options
                                                                              )
                                                          );
                          },
                                    INSERT_MANY
                                   );
        return executor == null ?
                IO.blockingSupply(supplier) :
                IO.supplyOn(supplier,
                            executor
                           );

    }
}
