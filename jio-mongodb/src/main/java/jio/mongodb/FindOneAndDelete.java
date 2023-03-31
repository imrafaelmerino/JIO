package jio.mongodb;

import com.mongodb.client.model.FindOneAndDeleteOptions;
import jio.IO;
import jio.Lambda;
import jsonvalues.JsObj;

import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.function.Supplier;

import static java.util.Objects.requireNonNull;
import static jio.mongodb.Converters.jsObj2Bson;
import static jio.mongodb.MongoDBEvent.OP.FIND_ONE_AND_DELETE;


public final class FindOneAndDelete implements Lambda<JsObj, JsObj> {

    private final CollectionSupplier collection;
    private final FindOneAndDeleteOptions options;
    private static final FindOneAndDeleteOptions DEFAULT_OPTIONS = new FindOneAndDeleteOptions();


    private FindOneAndDelete(final CollectionSupplier collection,
                             final FindOneAndDeleteOptions options
                            ) {
        this.options = requireNonNull(options);
        this.collection = requireNonNull(collection);
    }

    private Executor executor;

    public static FindOneAndDelete of(final CollectionSupplier collection,
                                      final FindOneAndDeleteOptions options
                                     ) {
        return new FindOneAndDelete(collection, options);
    }

    public static FindOneAndDelete of(final CollectionSupplier collection) {
        return new FindOneAndDelete(collection, DEFAULT_OPTIONS);
    }

    public FindOneAndDelete on(final Executor executor) {
        this.executor = Objects.requireNonNull(executor);
        return this;
    }

    @Override
    public IO<JsObj> apply(final JsObj query) {
        Objects.requireNonNull(query);
        Supplier<JsObj> supplier =
                Fun.jfrEventWrapper(() -> {
                              var collection = requireNonNull(this.collection.get());
                              return collection.findOneAndDelete(jsObj2Bson.apply(query),
                                                                 options
                                                                );
                          },
                                    FIND_ONE_AND_DELETE
                                   );
        return executor == null ?
                IO.fromManagedSupplier(supplier) :
                IO.fromSupplier(supplier, executor);

    }
}
