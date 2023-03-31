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
import static jio.mongodb.MongoDBEvent.OP.UPDATE_ONE;


public final class UpdateOne<O> implements BiLambda<JsObj, JsObj, O> {

    public final CollectionSupplier collection;
    public final Function<UpdateResult, O> resultConverter;

    public final UpdateOptions options;
    private static final UpdateOptions DEFAULT_OPTIONS = new UpdateOptions();


    private UpdateOne(final CollectionSupplier collection,
                      final Function<UpdateResult, O> resultConverter,
                      final UpdateOptions options
                     ) {
        this.collection = requireNonNull(collection);
        this.resultConverter = requireNonNull(resultConverter);
        this.options = requireNonNull(options);
    }

    private Executor executor;

    public static <O> UpdateOne<O> of(final CollectionSupplier collection,
                                      final Function<UpdateResult, O> resultConverter,
                                      final UpdateOptions options
                                     ) {
        return new UpdateOne<>(collection, resultConverter, options);
    }

    public static <O> UpdateOne<O> of(final CollectionSupplier collection,
                                      final Function<UpdateResult, O> resultConverter
                                     ) {
        return new UpdateOne<>(collection, resultConverter, DEFAULT_OPTIONS);
    }

    public static UpdateOne<JsObj> of(final CollectionSupplier collection
                                     ) {
        return new UpdateOne<>(collection, Converters.updateResult2JsObj, DEFAULT_OPTIONS);
    }

    public UpdateOne<O> on(final Executor executor) {
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
                              return resultConverter.apply(collection.updateOne(jsObj2Bson.apply(filter),
                                                                                jsObj2Bson.apply(update),
                                                                                options
                                                                               )
                                                          );
                          },
                                    UPDATE_ONE
                                   );
        return executor == null ?
                IO.fromManagedSupplier(supplier) :
                IO.fromSupplier(supplier,
                                executor
                               );


    }
}
