package jio.mongodb;

import com.mongodb.client.model.FindOneAndUpdateOptions;
import jio.BiLambda;
import jio.IO;
import jsonvalues.JsObj;

import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.function.Supplier;

import static java.util.Objects.requireNonNull;
import static jio.mongodb.Converters.jsObj2Bson;
import static jio.mongodb.MongoDBEvent.OP.FIND_ONE_AND_UPDATE;

public final class FindOneAndUpdate implements BiLambda<JsObj, JsObj, JsObj> {

    private final FindOneAndUpdateOptions options;
    private final CollectionSupplier collectionSupplier;
    private static final FindOneAndUpdateOptions DEFAULT_OPTIONS = new FindOneAndUpdateOptions();

    private FindOneAndUpdate(final CollectionSupplier collectionSupplier,
                             final FindOneAndUpdateOptions options
                            ) {
        this.collectionSupplier = requireNonNull(collectionSupplier);
        this.options = requireNonNull(options);
    }


    private Executor executor;

    public static FindOneAndUpdate of(final CollectionSupplier collectionSupplier,
                                      final FindOneAndUpdateOptions options
                                     ) {
        return new FindOneAndUpdate(collectionSupplier, options);
    }

    public static FindOneAndUpdate of(final CollectionSupplier collectionSupplier) {
        return new FindOneAndUpdate(collectionSupplier, DEFAULT_OPTIONS);
    }

    public FindOneAndUpdate on(final Executor executor) {
        this.executor = requireNonNull(executor);
        return this;
    }

    @Override
    public IO<JsObj> apply(final JsObj filter,
                           final JsObj update
                          ) {
        Objects.requireNonNull(filter);
        Objects.requireNonNull(update);

        Supplier<JsObj> supplier =
                Fun.jfrEventWrapper(() -> {
                              var collection = requireNonNull(this.collectionSupplier.get());
                              return collection
                                      .findOneAndUpdate(jsObj2Bson.apply(filter),
                                                        jsObj2Bson.apply(update),
                                                        options
                                                       );
                          },
                                    FIND_ONE_AND_UPDATE
                                   );
        return executor == null ?
                IO.fromManagedSupplier(supplier) :
                IO.fromSupplier(supplier,
                                executor
                               );

    }


}
