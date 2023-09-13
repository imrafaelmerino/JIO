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


public final class FindOneAndReplace implements BiLambda<JsObj, JsObj, JsObj> {

    private final FindOneAndReplaceOptions options;
    private final CollectionSupplier collection;
    private static final FindOneAndReplaceOptions DEFAULT_OPTIONS = new FindOneAndReplaceOptions();

    private Executor executor;

    public static FindOneAndReplace of(final CollectionSupplier collection,
                                       final FindOneAndReplaceOptions options
                                      ) {
        return new FindOneAndReplace(collection, options);
    }

    public static FindOneAndReplace of(final CollectionSupplier collection) {
        return new FindOneAndReplace(collection, DEFAULT_OPTIONS);
    }

    public FindOneAndReplace on(final Executor executor) {
        this.executor = requireNonNull(executor);
        return this;
    }

    private FindOneAndReplace(final CollectionSupplier collection,
                              final FindOneAndReplaceOptions options
                             ) {
        this.collection = requireNonNull(collection);
        this.options = requireNonNull(options);
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
