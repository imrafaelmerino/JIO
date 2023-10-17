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

/**
 * Represents a MongoDB find operation for querying a collection asynchronously using {@link jio.Lambda lambdas}. This
 * class is part of a sealed hierarchy, which includes {@link FindOne} and {@link FindAll} for specific find
 * operations.
 *
 * <p>The find operation allows you to specify various query options like filtering, sorting, and limiting results.
 * The result of the find operation is converted to a specified type using a converter function.</p>
 *
 * @param <O> the type of the result produced by the find operation
 * @see FindOne
 * @see FindAll
 * @see FindOptions
 */
sealed public class Find<O> extends Op implements Lambda<FindBuilder, O> permits FindOne, FindAll {

    private final Function<FindIterable<JsObj>, O> converter;

    /**
     * Constructs a {@code Find} instance for querying a MongoDB collection.
     *
     * @param collection the supplier of the MongoDB collection
     * @param converter  the function to convert the query result to the desired type
     */
    Find(final CollectionSupplier collection,
         final Function<FindIterable<JsObj>, O> converter
        ) {
        super(collection, true);
        this.converter = requireNonNull(converter);
    }

    /**
     * Sets the executor to be used for asynchronous execution of the find operation. If not set, the operation will run
     * in the current thread.
     *
     * @param executor the executor for asynchronous execution
     * @return this {@code Find} instance with the executor set
     */
    public Find<O> withExecutor(final Executor executor) {
        this.executor = requireNonNull(executor);
        return this;
    }



    /**
     * Executes the find operation with the specified query options.
     *
     * @param builder the builder to create the query options for the find operation
     * @return an {@link IO} representing the asynchronous result of the find operation
     */
    @Override
    public IO<O> apply(final FindBuilder builder) {
        Objects.requireNonNull(builder);
        FindOptions options = builder.build();
        Supplier<O> supplier =
                jfrEventWrapper(() -> {
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
