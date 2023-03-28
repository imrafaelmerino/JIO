package jio.mongodb;

import com.mongodb.client.model.UpdateOptions;
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
import static jio.mongodb.MongoDBEvent.OP.UPDATE_MANY;


public final class UpdateMany<O> implements BiLambda<JsObj, JsObj, O> {

    private final UpdateOptions options;
    private final CollectionSupplier collection;
    private final Function<UpdateResult, O> resultConverter;
    private static final UpdateOptions DEFAULT_OPTIONS = new UpdateOptions();


    private UpdateMany(final CollectionSupplier collection,
                       final Function<UpdateResult, O> resultConverter,
                       final UpdateOptions options
                      ) {
        this.options = requireNonNull(options);
        this.collection = requireNonNull(collection);
        this.resultConverter = requireNonNull(resultConverter);
    }

    private Executor executor;

    public static <O> UpdateMany<O> of(final CollectionSupplier collection,
                                       final Function<UpdateResult, O> resultConverter
                                      ) {
        return of(collection, resultConverter, DEFAULT_OPTIONS);
    }

    public static <O> UpdateMany<O> of(final CollectionSupplier collection,
                                       final Function<UpdateResult, O> resultConverter,
                                       final UpdateOptions options
                                      ) {
        return new UpdateMany<>(collection, resultConverter, options);
    }

    public UpdateMany<O> on(final Executor executor) {
        this.executor = requireNonNull(executor);
        return this;
    }

    @Override
    public IO<O> apply(final JsObj filter,
                       final JsObj update
                      ) {
        Objects.requireNonNull(filter);
        Objects.requireNonNull(update);

        Supplier<O> supplier =
                Fun.jfrEventWrapper(() -> {
                              var collection = requireNonNull(this.collection.get());
                              return resultConverter.apply(collection.updateMany(jsObj2Bson.apply(filter),
                                                                                 jsObj2Bson.apply(update),
                                                                                 options
                                                                                )
                                                          );
                          },
                                    UPDATE_MANY
                                   );
        return executor == null ?
                IO.blockingSupply(supplier) :
                IO.supplyOn(supplier, executor);

    }
}
