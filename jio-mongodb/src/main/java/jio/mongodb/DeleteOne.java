package jio.mongodb;

import com.mongodb.client.model.DeleteOptions;
import com.mongodb.client.result.DeleteResult;
import jio.IO;

import jio.Lambda;
import jsonvalues.JsObj;
import org.bson.conversions.Bson;

import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.function.Function;
import java.util.function.Supplier;

import static java.util.Objects.requireNonNull;
import static jio.mongodb.Converters.jsObj2Bson;
import static jio.mongodb.MongoDBEvent.OP.DELETE_ONE;


public final class DeleteOne<O> implements Lambda<JsObj, O> {
    private final CollectionSupplier collection;
    private final Function<DeleteResult, O> resultConverter;
    private final DeleteOptions options;
    private static final DeleteOptions DEFAULT_OPTIONS = new DeleteOptions();


    private DeleteOne(final CollectionSupplier collection,
                      final Function<DeleteResult, O> resultConverter,
                      final DeleteOptions options
                     ) {
        this.collection = requireNonNull(collection);
        this.resultConverter = requireNonNull(resultConverter);
        this.options = requireNonNull(options);
    }


    private Executor executor;

    public static <O> DeleteOne<O> of(final CollectionSupplier collection,
                                      final Function<DeleteResult, O> resultConverter,
                                      final DeleteOptions options
                                     ) {
        return new DeleteOne<>(collection, resultConverter, options);
    }

    public static <O> DeleteOne<O> of(final CollectionSupplier collection,
                                      final Function<DeleteResult, O> resultConverter
                                     ) {
        return new DeleteOne<>(collection, resultConverter, DEFAULT_OPTIONS);
    }

    public static DeleteOne<JsObj> of(final CollectionSupplier collection) {
        return new DeleteOne<>(collection, Converters.deleteResult2JsObj, DEFAULT_OPTIONS);
    }

    public DeleteOne<O> on(final Executor executor) {
        this.executor = requireNonNull(executor);
        return this;
    }

    @Override
    public IO<O> apply(final JsObj query) {
        Objects.requireNonNull(query);
        Supplier<O> supplier =
                Fun.jfrEventWrapper(() -> {
                              var collection = requireNonNull(this.collection.get());
                              final Bson result = jsObj2Bson.apply(requireNonNull(query));
                              return resultConverter.apply(
                                      collection.deleteOne(result,
                                                           options
                                                          )
                                                          );
                          }, DELETE_ONE
                                   );
        return executor == null ?
                IO.managedLazy(supplier) :
                IO.lazy(supplier,
                                executor
                               );

    }
}
