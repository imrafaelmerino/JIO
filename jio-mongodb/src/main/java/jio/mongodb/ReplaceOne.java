package jio.mongodb;

import static java.util.Objects.requireNonNull;
import static jio.mongodb.Converters.toBson;
import static jio.mongodb.MongoOpEvent.OP.REPLACE_ONE;

import com.mongodb.client.ClientSession;
import com.mongodb.client.model.ReplaceOptions;
import com.mongodb.client.result.UpdateResult;
import java.util.Objects;
import java.util.function.Supplier;
import jio.IO;

/**
 * A class for performing replace one operation on a MongoDB collection.
 * <p>
 * The `ReplaceOne` class is designed for performing replace operations to updateCommands a single document within a
 * MongoDB collection. It provides flexibility in handling the result and allows you to specify various options for the
 * replace operation. You can create instances of this class with the specified collection supplier, and customize the
 * behavior using options such as replace options, executors, and more.
 * <p>
 * To use this class effectively, you can set the replace options for the operation, specify an executor for
 * asynchronous execution, and disable the recording of Java Flight Recorder (JFR) events if needed. Additionally, you
 * can use the provided `QueryReplace` object to define the query and the new document for the operation.
 *
 * @see CollectionBuilder
 * @see QueryAndDoc
 */
public final class ReplaceOne extends Op implements MongoLambda<QueryAndDoc, UpdateResult> {

  static final ReplaceOptions DEFAULT_OPTIONS = new ReplaceOptions();
  private ReplaceOptions options = DEFAULT_OPTIONS;

  /**
   * Constructs a new `ReplaceOne` instance with the specified collection supplier and default replace options.
   *
   * @param collection The supplier for the MongoDB collection.
   */
  private ReplaceOne(final CollectionBuilder collection) {
    super(collection,
          true);
  }

  /**
   * Creates a `ReplaceOne` instance for performing replace one operations on a MongoDB collection with the result as a
   * JsObj.
   *
   * @param collection The supplier for the MongoDB collection.
   * @return A `ReplaceOne` instance for performing replace one operations with a JsObj result.
   */
  public static ReplaceOne of(final CollectionBuilder collection) {
    return new ReplaceOne(collection);
  }

  /**
   * Sets the replace options to be used for the operation.
   *
   * @param options The options to perform the operation.
   * @return This instance with the new options.
   */
  public ReplaceOne withOptions(final ReplaceOptions options) {
    this.options = requireNonNull(options);
    return this;
  }

  /**
   * Applies the replace one operation to the specified MongoDB collection with a query and a new document.
   *
   * @param session     The MongoDB client session, or null if not within a session.
   * @param queryAndDoc The query and new document criteria for the operation.
   * @return An IO representing the result of the replace one operation.
   */
  @Override
  public IO<UpdateResult> apply(final ClientSession session,
                                final QueryAndDoc queryAndDoc) {
    Objects.requireNonNull(queryAndDoc);

    Supplier<UpdateResult> supplier = decorateWithEvent(() -> {
      var collection = requireNonNull(this.collection.get());
      return session == null ? collection.replaceOne(toBson(queryAndDoc.query()),
                                                     queryAndDoc.newDoc(),
                                                     options
      ) : collection.replaceOne(session,
                                toBson(queryAndDoc.query()),
                                queryAndDoc.newDoc(),
                                options
      );
    },
                                                        REPLACE_ONE);
    return IO.lazy(supplier);
  }

  /**
   * Disables the recording of Java Flight Recorder (JFR) events. When events recording is disabled, the operation will
   * not generate or log JFR events for its operations.
   *
   * @return This operation instance with JFR event recording disabled.
   */
  public ReplaceOne withoutRecordedEvents() {
    this.recordEvents = false;
    return this;
  }
}
