package jio.mongodb;

import com.mongodb.client.model.InsertManyOptions;
import com.mongodb.client.result.InsertManyResult;
import jio.IO;
import jio.Lambda;
import jsonvalues.JsArray;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.function.Function;
import java.util.function.Supplier;

import static java.util.Objects.requireNonNull;
import static jio.mongodb.Converters.jsArray2ListOfJsObj;
import static jio.mongodb.MongoDBEvent.OP.INSERT_MANY;

/**
 * A class for performing insert many operations on a MongoDB collection.
 *
 * @param <R> The type of the result.
 */
public final class InsertMany<R> extends Op implements Lambda<JsArray, R> {

    private static final InsertManyOptions DEFAULT_OPTIONS = new InsertManyOptions();
    private final Function<InsertManyResult, R> resultConverter;
    private InsertManyOptions options = DEFAULT_OPTIONS;

    /**
     * Constructs a new InsertMany instance.
     *
     * @param collection      The supplier for the MongoDB collection.
     * @param resultConverter The function to convert the insert result to the desired type.
     */
    private InsertMany(final CollectionSupplier collection,
                       final Function<InsertManyResult, R> resultConverter
                      ) {
        super(collection, true);
        this.resultConverter = requireNonNull(resultConverter);
    }

    /**
     * Creates an InsertMany instance with the specified collection supplier and result converter using default
     * options.
     *
     * @param collection      The supplier for the MongoDB collection.
     * @param resultConverter The function to convert the insert result to the desired type.
     * @param <R>             The type of the result.
     * @return An InsertMany instance with default options.
     */
    public static <R> InsertMany<R> of(final CollectionSupplier collection,
                                       final Function<InsertManyResult, R> resultConverter
                                      ) {
        return new InsertMany<>(collection, resultConverter);
    }

    /**
     * Creates an InsertMany instance for inserting arrays of MongoDB document IDs (hexadecimal strings).
     *
     * @param collection The supplier for the MongoDB collection.
     * @return An InsertMany instance for inserting arrays of MongoDB document IDs.
     */
    public static InsertMany<List<String>> of(final CollectionSupplier collection) {
        return new InsertMany<>(collection, Converters.insertManyResult2ListOfHexIds);
    }

    /**
     * @param options the options to perform the operation
     * @return this instance with the new options
     */
    public InsertMany<R> withOptions(final InsertManyOptions options) {
        this.options = requireNonNull(options);
        return this;
    }

    /**
     * Specifies an executor to be used for running the insert many operation asynchronously.
     *
     * @param executor The executor to use.
     * @return This InsertMany instance for method chaining.
     */
    public InsertMany<R> withExecutor(final Executor executor) {
        this.executor = Objects.requireNonNull(executor);
        return this;
    }

    /**
     * Performs an insert many operation on the MongoDB collection based on the provided documents.
     *
     * @param message The array of documents to insert.
     * @return An IO operation representing the result of the insert many operation.
     */
    @Override
    public IO<R> apply(final JsArray message) {
        Objects.requireNonNull(message);
        Supplier<R> supplier =
                jfrEventWrapper(() -> {
                                    var docs = jsArray2ListOfJsObj.apply(message);
                                    var col = requireNonNull(collection.get());
                                    return resultConverter.apply(col
                                                                         .insertMany(docs, options)
                                                                );
                                },
                                INSERT_MANY
                               );
        return executor == null ?
                IO.managedLazy(supplier) :
                IO.lazy(supplier, executor);
    }

    /**
     * Disables the recording of Java Flight Recorder (JFR) events. When events recording is disabled, the operation
     * will not generate or log JFR events for its operations.
     *
     * @return This operation instance with JFR event recording disabled.
     */
    public InsertMany withoutRecordedEvents() {
        this.recordEvents = false;
        return this;
    }
}
