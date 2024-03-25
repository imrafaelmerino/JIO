package jio.mongodb;

import static java.util.Objects.requireNonNull;
import static jio.mongodb.Converters.toBson;
import static jio.mongodb.MongoOpEvent.OP.DELETE_ONE;

import com.mongodb.client.ClientSession;
import com.mongodb.client.model.DeleteOptions;
import com.mongodb.client.result.DeleteResult;
import java.util.Objects;
import java.util.function.Supplier;
import jio.IO;
import jsonvalues.JsObj;
import org.bson.conversions.Bson;

/**
 * Represents an operation to delete a single document from a MongoDB collection. This class provides flexibility in
 * handling the result and allows you to specify various options for the delete operation.
 * <p>
 * The `DeleteOne` class allows you to delete a single document from a MongoDB collection with specified query criteria.
 * The result of the operation is represented as a `DeleteResult`, which includes information about whether the document
 * was deleted and other details. You can also customize the behavior of the delete operation by configuring options and
 * specifying an executor for asynchronous execution.
 * <p>
 * To use this class effectively, you can set custom delete options and an optional executor. The operation can also be
 * executed within a MongoDB client session if one is provided.
 *
 * @see MongoLambda
 * @see Converters
 */
public final class DeleteOne extends Op implements MongoLambda<JsObj, DeleteResult> {

  private static final DeleteOptions DEFAULT_OPTIONS = new DeleteOptions();
  private DeleteOptions options = DEFAULT_OPTIONS;

  /**
   * Constructs a {@code DeleteOne} instance with the specified collection, result converter, and delete options.
   *
   * @param collection The {@code CollectionBuilder} to obtain the MongoDB collection.
   */
  private DeleteOne(final CollectionBuilder collection) {
    super(collection,
          true);
  }

  /**
   * Creates a new {@code DeleteOne} instance with the specified collection, using default delete options and a result
   * converter for {@code JsObj} result type.
   *
   * @param collection The {@code CollectionBuilder} to obtain the MongoDB collection.
   * @return A new {@code DeleteOne} instance for {@code JsObj} result type.
   */
  public static DeleteOne of(final CollectionBuilder collection) {
    return new DeleteOne(collection);
  }

  /**
   * Sets custom options for the delete operation.
   *
   * @param options The options to perform the operation.
   * @return This instance with the new options.
   */
  public DeleteOne withOptions(final DeleteOptions options) {
    this.options = requireNonNull(options);
    return this;
  }

  /**
   * Applies the delete one operation to the specified MongoDB collection using the provided query and options.
   *
   * @param session The MongoDB client session, or null if not within a session.
   * @param query   The query criteria to determine which document to delete.
   * @return An IO representing the result of the delete one operation as a {@link DeleteResult}.
   */
  @Override
  public IO<DeleteResult> apply(final ClientSession session,
                                final JsObj query) {
    Objects.requireNonNull(query);
    Supplier<DeleteResult> supplier = decorateWithEvent(() -> {
      var collection = requireNonNull(this.collection.get());
      final Bson result = toBson(requireNonNull(query));
      return session == null ? collection.deleteOne(result,
                                                    options) : collection.deleteOne(session,
                                                                                    result,
                                                                                    options);
    },
                                                        DELETE_ONE
    );
    return IO.lazy(supplier);
  }

  /**
   * Disables the recording of Java Flight Recorder (JFR) events. When events recording is disabled, the operation will
   * not generate or log JFR events for its operations.
   *
   * @return This operation instance with JFR event recording disabled.
   */
  public DeleteOne withoutRecordedEvents() {
    this.recordEvents = false;
    return this;
  }

}
