package jio.mongodb;

import com.mongodb.client.ClientSession;
import com.mongodb.client.model.DeleteOptions;
import com.mongodb.client.result.DeleteResult;
import jio.IO;
import jsonvalues.JsObj;

import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.function.Supplier;

import static java.util.Objects.requireNonNull;
import static jio.mongodb.Converters.toBson;
import static jio.mongodb.MongoEvent.OP.DELETE_MANY;

/**
 * Represents an operation to delete multiple documents from a MongoDB collection. This class provides flexibility in
 * handling the result and allows you to specify various options for the delete operation.
 * <p>
 * The `DeleteMany` class allows you to delete multiple documents from a MongoDB collection with specified query
 * criteria. The result of the operation is represented as a `DeleteResult`, which includes information about the number
 * of documents deleted and other details. You can also customize the behavior of the delete operation by configuring
 * options and specifying an executor for its execution.
 * <p>
 * The operation can also be executed within a MongoDB client session if one is provided.
 *
 * @see MongoLambda
 * @see Converters
 */
public final class DeleteMany extends Op implements MongoLambda<JsObj, DeleteResult> {


    private static final DeleteOptions DEFAULT_OPTIONS = new DeleteOptions();
    private DeleteOptions options = DEFAULT_OPTIONS;

    /**
     * Constructs a {@code DeleteMany} instance with the specified collection, result converter, and delete options.
     *
     * @param collection The {@code CollectionBuilder} to obtain the MongoDB collection.
     */
    private DeleteMany(final CollectionBuilder collection) {
        super(collection, true);
    }


    /**
     * Creates a new {@code DeleteMany} instance with the specified collection, using default delete options and a
     * result converter for {@code JsObj} result type.
     *
     * @param collection The {@code CollectionBuilder} to obtain the MongoDB collection.
     * @return A new {@code DeleteMany} instance for {@code JsObj} result type.
     */
    public static DeleteMany of(final CollectionBuilder collection) {
        return new DeleteMany(collection);
    }

    /**
     * adds the specified mongodb options to this instance
     * @param options the options to perform the operation
     * @return this instance with the new options
     *
     */
    public DeleteMany withOptions(final DeleteOptions options) {
        this.options = requireNonNull(options);
        return this;
    }

    /**
     * Specifies an {@code Executor} on which the delete operation should be executed asynchronously.
     *
     * @param executor The {@code Executor} to use for asynchronous execution.
     * @return This {@code DeleteMany} instance for method chaining.
     */
    public DeleteMany withExecutor(final Executor executor) {
        this.executor = requireNonNull(executor);
        return this;
    }


    @Override
    public IO<DeleteResult> apply(final ClientSession session,
                                  final JsObj query
                                 ) {
        Objects.requireNonNull(query);
        Supplier<DeleteResult> supplier =
                eventWrapper(() -> {
                                 var collection = requireNonNull(this.collection.get());
                                 return
                                         session == null ?
                                                 collection.deleteMany(toBson(query),
                                                                       options
                                                                      ) :
                                                 collection.deleteMany(session,
                                                                       toBson(query),
                                                                       options
                                                                      );
                             },
                             DELETE_MANY
                            );
        return executor == null ?
                IO.managedLazy(supplier) :
                IO.lazy(supplier,
                        executor
                       );
    }

    /**
     * Disables the recording of Java Flight Recorder (JFR) events. When events recording is disabled, the operation
     * will not generate or log JFR events for its operations.
     *
     * @return This operation instance with JFR event recording disabled.
     */
    public DeleteMany withoutRecordedEvents() {
        this.recordEvents = false;
        return this;
    }
}
