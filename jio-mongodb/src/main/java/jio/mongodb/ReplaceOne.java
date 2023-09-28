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


public final class ReplaceOne<O> implements BiLambda<JsObj, JsObj, O> {
    public static final ReplaceOptions DEFAULT_OPTIONS = new ReplaceOptions();
    private final Function<UpdateResult, O> resultConverter;
    private final CollectionSupplier collection;
    private final ReplaceOptions options;
    private Executor executor;


    private ReplaceOne(final CollectionSupplier collection,
                       final Function<UpdateResult, O> resultConverter,
                       final ReplaceOptions options
                      ) {
        this.resultConverter = requireNonNull(resultConverter);
        this.collection = requireNonNull(collection);
        this.options = requireNonNull(options);
    }

    public static <O> ReplaceOne<O> of(final CollectionSupplier collection,
                                       final Function<UpdateResult, O> resultConverter,
                                       final ReplaceOptions options
                                      ) {
        return new ReplaceOne<>(collection, resultConverter, options);
    }

    public static <O> ReplaceOne<O> of(final CollectionSupplier collection,
                                       final Function<UpdateResult, O> resultConverter
                                      ) {
        return new ReplaceOne<>(collection, resultConverter, DEFAULT_OPTIONS);
    }

    public static ReplaceOne<JsObj> of(final CollectionSupplier collection
                                      ) {
        return new ReplaceOne<>(collection, Converters.updateResult2JsObj, DEFAULT_OPTIONS);
    }

    public ReplaceOne<O> on(final Executor executor) {
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
                                        return resultConverter
                                                .apply(collection.replaceOne(jsObj2Bson.apply(filter),
                                                                             update,
                                                                             options
                                                                            )
                                                      );

                                    },
                                    REPLACE_ONE
                                   );
        return executor == null ?
                IO.managedLazy(supplier) :
                IO.lazy(supplier,
                        executor
                       );

    }
}
