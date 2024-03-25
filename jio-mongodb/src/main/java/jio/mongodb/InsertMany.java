package jio.mongodb;

import static java.util.Objects.requireNonNull;
import static jio.mongodb.MongoOpEvent.OP.INSERT_MANY;

import com.mongodb.client.ClientSession;
import com.mongodb.client.model.InsertManyOptions;
import com.mongodb.client.result.InsertManyResult;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;
import jio.IO;
import jsonvalues.JsObj;

/**
 * A class for performing insert many operations on a MongoDB collection.
 * <p>
 * The `InsertMany` class is designed for inserting multiple documents into a MongoDB collection. It provides
 * flexibility in handling the result and allows you to specify various options for the insert operation. You can create
 * instances of this class with the specified collection supplier, and customize the behavior using options such as
 * insert options, executors, and more.
 * <p>
 * To use this class effectively, you can set the insert options for the operation, specify an executor for asynchronous
 * execution, and disable the recording of Java Flight Recorder (JFR) events if needed. You can insert a list of
 * documents represented as `JsObj` objects into the MongoDB collection.
 *
 * @see CollectionBuilder
 */
public final class InsertMany extends Op implements MongoLambda<List<JsObj>, InsertManyResult> {

  private static final InsertManyOptions DEFAULT_OPTIONS = new InsertManyOptions();
  private InsertManyOptions options = DEFAULT_OPTIONS;

  /**
   * Constructs a new `InsertMany` instance with the specified collection supplier and default insert options.
   *
   * @param collection The supplier for the MongoDB collection.
   */
  private InsertMany(final CollectionBuilder collection) {
    super(collection,
          true);
  }

  /**
   * Creates an `InsertMany` instance with the specified collection supplier and result converter using default
   * options.
   *
   * @param collection The supplier for the MongoDB collection.
   * @return An `InsertMany` instance with default options.
   */
  public static InsertMany of(final CollectionBuilder collection) {
    return new InsertMany(collection);
  }

  /**
   * Sets the insert options to be used for the operation.
   *
   * @param options The options to perform the operation.
   * @return This instance with the new options.
   */
  public InsertMany withOptions(final InsertManyOptions options) {
    this.options = requireNonNull(options);
    return this;
  }

  /**
   * Applies the insert many operation to the specified MongoDB collection with a list of `JsObj` documents.
   *
   * @param session The MongoDB client session, or null if not within a session.
   * @param docs    The list of `JsObj` documents to insert.
   * @return An IO representing the result of the insert many operation.
   */
  @Override
  public IO<InsertManyResult> apply(final ClientSession session,
                                    final List<JsObj> docs) {
    Objects.requireNonNull(docs);
    Supplier<InsertManyResult> supplier = decorateWithEvent(() -> {
      var col = requireNonNull(collection.get());
      return session == null ? col.insertMany(docs,
                                              options) : col.insertMany(session,
                                                                        docs,
                                                                        options);
    },
                                                            INSERT_MANY
    );
    return IO.lazy(supplier);
  }

  /**
   * Disables the recording of Java Flight Recorder (JFR) events. When events recording is disabled, the operation will
   * not generate or log JFR events for its operations.
   *
   * @return This operation instance with JFR event recording disabled.
   */
  public InsertMany withoutRecordedEvents() {
    this.recordEvents = false;
    return this;
  }
}
