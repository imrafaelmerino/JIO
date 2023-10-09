package jio.mongodb;

import com.mongodb.client.model.InsertOneOptions;
import com.mongodb.client.result.InsertOneResult;
import jio.IO;
import jio.Lambda;
import jsonvalues.JsObj;

import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.function.Function;
import java.util.function.Supplier;

import static java.util.Objects.requireNonNull;
import static jio.mongodb.MongoDBEvent.OP.INSERT_ONE;

/**
 * A class for performing insert one operations on a MongoDB collection.
 *
 * @param <R> The type of the result.
 */
public final class InsertOne<R> extends Op implements Lambda<JsObj, R> {

    private static final InsertOneOptions DEFAULT_OPTIONS = new InsertOneOptions();
    private final InsertOneOptions options;
    private final Function<InsertOneResult, R> resultConverter;


    /**
     * Constructs a new InsertOne instance.
     *
     * @param collection      The supplier for the MongoDB collection.
     * @param resultConverter The function to convert the insert result to the desired type.
     * @param options         The insert one options.
     */
    public InsertOne(final CollectionSupplier collection,
                     final Function<InsertOneResult, R> resultConverter,
                     final InsertOneOptions options
                    ) {
        super(collection, true);
        this.options = requireNonNull(options);
        this.resultConverter = requireNonNull(resultConverter);
    }

    /**
     * Creates an InsertOne instance with the specified collection supplier, result converter, and options.
     *
     * @param collection      The supplier for the MongoDB collection.
     * @param resultConverter The function to convert the insert result to the desired type.
     * @param options         The insert one options.
     * @param <R>             The type of the result.
     * @return An InsertOne instance.
     */
    public static <R> InsertOne<R> of(final CollectionSupplier collection,
                                      final Function<InsertOneResult, R> resultConverter,
                                      final InsertOneOptions options
                                     ) {
        return new InsertOne<>(collection, resultConverter, options);
    }

    /**
     * Creates an InsertOne instance with the specified collection supplier and result converter using default options.
     *
     * @param collection      The supplier for the MongoDB collection.
     * @param resultConverter The function to convert the insert result to the desired type.
     * @param <R>             The type of the result.
     * @return An InsertOne instance with default options.
     */
    public static <R> InsertOne<R> of(final CollectionSupplier collection,
                                      final Function<InsertOneResult, R> resultConverter
                                     ) {
        return new InsertOne<>(collection, resultConverter, DEFAULT_OPTIONS);
    }

    /**
     * Creates an InsertOne instance for inserting a single MongoDB document.
     *
     * @param collection The supplier for the MongoDB collection.
     * @return An InsertOne instance for inserting a single document.
     */
    public static InsertOne<JsObj> of(final CollectionSupplier collection) {
        return new InsertOne<>(collection, Converters.insertOneResult2JsObj, DEFAULT_OPTIONS);
    }

    /**
     * Specifies an executor to be used for running the insert one operation asynchronously.
     *
     * @param executor The executor to use.
     * @return This InsertOne instance for method chaining.
     */
    public InsertOne<R> on(final Executor executor) {
        this.executor = requireNonNull(executor);
        return this;
    }

    /**
     * Performs an insert one operation on the MongoDB collection based on the provided document.
     *
     * @param message The document to insert.
     * @return An IO operation representing the result of the insert one operation.
     */
    @Override
    public IO<R> apply(final JsObj message) {
        Objects.requireNonNull(message);
        Supplier<R> supplier =
                jfrEventWrapper(() -> {
                                    var collection = requireNonNull(this.collection.get());
                                    return resultConverter.apply(collection.insertOne(message, options));
                                },
                                INSERT_ONE
                               );
        return executor == null ?
                IO.managedLazy(supplier) :
                IO.lazy(supplier, executor);
    }

    /**
     * Disables the recording of Java Flight Recorder (JFR) events. When events recording is disabled,
     * the operation will not generate or log JFR events for its operations.
     *
     * @return This operation instance with JFR event recording disabled.
     */
    public InsertOne<R> disableRecordEvents(){
        this.recordEvents = false;
        return this;
    }
}
