package jio.mongodb;

import static java.util.Objects.requireNonNull;
import static jio.mongodb.MongoOpEvent.OP.INSERT_ONE;

import com.mongodb.client.ClientSession;
import com.mongodb.client.model.InsertOneOptions;
import com.mongodb.client.result.InsertOneResult;
import java.util.Objects;
import java.util.function.Supplier;
import jio.IO;
import jsonvalues.JsObj;

/**
 * A class for performing insert one operation on a MongoDB collection.
 * <p>
 * The `InsertOne` class is designed for inserting a single document into a MongoDB collection. It provides flexibility
 * in handling the result and allows you to specify various options for the insert operation. You can create instances
 * of this class with the specified collection supplier, and customize the behavior using options such as insert
 * options, executors, and more.
 * <p>
 * To use this class effectively, you can set the insert options for the operation, specify an executor for asynchronous
 * execution, and disable the recording of Java Flight Recorder (JFR) events if needed. You can insert a single document
 * represented as a `JsObj` into the MongoDB collection.
 *
 * @see CollectionBuilder
 */
public final class InsertOne extends Op implements MongoLambda<JsObj, InsertOneResult> {

  private static final InsertOneOptions DEFAULT_OPTIONS = new InsertOneOptions();
  private InsertOneOptions options = DEFAULT_OPTIONS;

  /**
   * Constructs a new `InsertOne` instance with the specified collection supplier and default insert options.
   *
   * @param collection The supplier for the MongoDB collection.
   */
  private InsertOne(final CollectionBuilder collection) {
    super(collection,
          true);
  }

  /**
   * Creates an `InsertOne` instance with the specified collection supplier and result converter using default options.
   *
   * @param collection The supplier for the MongoDB collection.
   * @return An `InsertOne` instance with default options.
   */
  public static InsertOne of(final CollectionBuilder collection) {
    return new InsertOne(collection);
  }

  /**
   * Sets the insert options to be used for the operation.
   *
   * @param options The options to perform the operation.
   * @return This instance with the new options.
   */
  public InsertOne withOptions(final InsertOneOptions options) {
    this.options = requireNonNull(options);
    return this;
  }

  /**
   * Applies the insert one operation to the specified MongoDB collection with a `JsObj` document.
   *
   * @param session The MongoDB client session, or null if not within a session.
   * @param message The `JsObj` document to insert.
   * @return An IO representing the result of the insert one operation.
   */
  @Override
  public IO<InsertOneResult> apply(final ClientSession session,
                                   final JsObj message) {
    Objects.requireNonNull(message);
    Supplier<InsertOneResult> supplier = decorateWithEvent(() -> {
      var collection = requireNonNull(this.collection.get());
      return session == null ? collection.insertOne(message,
                                                    options) : collection.insertOne(session,
                                                                                    message,
                                                                                    options);
    },
                                                           INSERT_ONE
    );
    return IO.lazy(supplier);
  }

  /**
   * Disables the recording of Java Flight Recorder (JFR) events. When events recording is disabled, the operation will
   * not generate or log JFR events for its operations.
   *
   * @return This operation instance with JFR event recording disabled.
   */
  public InsertOne withoutRecordedEvents() {
    this.recordEvents = false;
    return this;
  }
}
