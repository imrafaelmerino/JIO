package jio.mongodb;

import com.mongodb.client.model.DeleteOptions;
import com.mongodb.client.result.DeleteResult;
import jio.IO;
import jio.Lambda;

import jsonvalues.JsObj;

import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.function.Function;
import java.util.function.Supplier;

import static java.util.Objects.requireNonNull;
import static jio.mongodb.Converters.jsObj2Bson;
import static jio.mongodb.MongoDBEvent.OP.DELETE_MANY;


public final class DeleteMany<O> implements Lambda<JsObj, O> {

    private final CollectionSupplier collection;
    private final Function<DeleteResult, O> resultConverter;
    private final DeleteOptions options;
    private static final DeleteOptions DEFAULT_OPTIONS = new DeleteOptions();


    private DeleteMany(final CollectionSupplier collection,
                       final Function<DeleteResult, O> resultConverter,
                       final DeleteOptions options
                      ) {
        this.collection = requireNonNull(collection);
        this.resultConverter = requireNonNull(resultConverter);
        this.options = requireNonNull(options);
    }


    private Executor executor;

    public static <O> DeleteMany<O> of(final CollectionSupplier collection,
                                       final Function<DeleteResult, O> resultConverter,
                                       final DeleteOptions options
                                      ) {
        return new DeleteMany<>(collection,
                                resultConverter,
                                options
        );
    }

    public static <O> DeleteMany<O> of(final CollectionSupplier collection,
                                       final Function<DeleteResult, O> resultConverter
                                      ) {
        return new DeleteMany<>(collection,
                                resultConverter,
                                DEFAULT_OPTIONS
        );
    }

    public static DeleteMany<JsObj> of(final CollectionSupplier collection) {
        return new DeleteMany<>(collection,
                                Converters.deleteResult2JsObj,
                                DEFAULT_OPTIONS
        );
    }

    public DeleteMany<O> on(final Executor executor) {
        this.executor = requireNonNull(executor);
        return this;
    }

    @Override
    public IO<O> apply(final JsObj query) {
        Objects.requireNonNull(query);
        Supplier<O> supplier =
                Fun.jfrEventWrapper(() -> {
                              var collection = requireNonNull(this.collection.get());
                              final DeleteResult result =
                                      collection.deleteMany(jsObj2Bson.apply(query),
                                                            options
                                                           );
                              return resultConverter.apply(result);
                          },
                                    DELETE_MANY
                                   );
        return executor == null ?
                IO.fromManagedSupplier(supplier) :
                IO.fromSupplier(supplier,
                                executor
                               );

    }
}
