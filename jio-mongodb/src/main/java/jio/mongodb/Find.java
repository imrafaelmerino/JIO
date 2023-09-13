package jio.mongodb;


import com.mongodb.client.FindIterable;
import jio.IO;
import jio.Lambda;
import jsonvalues.JsObj;

import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.function.Function;
import java.util.function.Supplier;

import static java.util.Objects.requireNonNull;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static jio.mongodb.Converters.jsObj2Bson;
import static jio.mongodb.MongoDBEvent.OP.FIND;


sealed public class Find<O> implements Lambda<FindOptions, O> permits FindOne, FindAll {

    private final CollectionSupplier collection;
    private final Function<FindIterable<JsObj>, O> converter;

    public Find(final CollectionSupplier collection,
                final Function<FindIterable<JsObj>, O> converter
               ) {
        this.collection = requireNonNull(collection);
        this.converter = requireNonNull(converter);
    }

    private Executor executor;

    public Find<O> on(final Executor executor) {
        this.executor = requireNonNull(executor);
        return this;
    }

    @Override
    public IO<O> apply(final FindOptions options) {
        Objects.requireNonNull(options);
        Supplier<O> supplier =
                Fun.jfrEventWrapper(() -> {
                              var hint = options.hint != null ?
                                      jsObj2Bson.apply(options.hint) :
                                      null;
                              var max = options.max != null ?
                                      jsObj2Bson.apply(options.max) :
                                      null;
                              var projection = options.projection != null ?
                                      jsObj2Bson.apply(options.projection) :
                                      null;
                              var sort = options.sort != null ?
                                      jsObj2Bson.apply(options.sort) :
                                      null;
                              var min = options.min != null ?
                                      jsObj2Bson.apply(options.min) :
                                      null;
                              var collection = requireNonNull(this.collection.get());
                              return converter.apply(collection.find(jsObj2Bson.apply(options.filter))
                                                               .hint(hint)
                                                               .max(max)
                                                               .projection(projection)
                                                               .sort(sort)
                                                               .min(min)
                                                               .batchSize(options.batchSize)
                                                               .comment(options.comment)
                                                               .hintString(options.hintString)
                                                               .limit(options.limit)
                                                               .skip(options.skip)
                                                               .maxTime(options.maxTime,
                                                                        MILLISECONDS
                                                                       )
                                                               .maxAwaitTime(options.maxAwaitTime,
                                                                             MILLISECONDS
                                                                            )
                                                               .partial(options.partial)
                                                               .showRecordId(options.showRecordId)
                                                               .noCursorTimeout(options.noCursorTimeout)

                                                    );
                          },
                                    FIND
                                   );
        return executor == null ?
                IO.managedLazy(supplier) :
                IO.lazy(supplier,
                                executor
                               );
    }

}
