package jio.mongodb;

import static java.util.Objects.requireNonNull;
import static jio.mongodb.Converters.toBson;
import static jio.mongodb.MongoOpEvent.OP.FIND_ONE_AND_REPLACE;

import com.mongodb.client.ClientSession;
import com.mongodb.client.model.FindOneAndReplaceOptions;
import java.util.Objects;
import java.util.function.Supplier;
import jio.IO;
import jsonvalues.JsObj;

/**
 * Represents a MongoDB find one and replace operation to updateCommands a single document in a collection
 * asynchronously using {@link jio.BiLambda lambdas}. This class allows you to specify a filter query criteria and an
 * updateCommands document as {@link jsonvalues.JsObj}, along with options for controlling the replacement behavior,
 * such as sort criteria and projection.
 * <p>
 * The `FindOneAndReplace` class is designed for replacing a single document in a MongoDB collection that matches the
 * specified filter criteria. This operation is performed atomically and asynchronously, allowing you to customize the
 * replacement options and execution behavior. You can create instances of this class with the specified collection
 * supplier, set replacement options, and choose an executor for asynchronous execution.
 * <p>
 * To use this class effectively, you can set the replacement options for the operation, specify an executor for
 * asynchronous execution, and disable the recording of Java Flight Recorder (JFR) events if needed. The find one and
 * replace operation requires a query filter to identify the document to replace and a new document to replace it with.
 *
 * @see CollectionBuilder
 */
public final class FindOneAndReplace extends Op implements MongoLambda<QueryAndDoc, JsObj> {

  private static final FindOneAndReplaceOptions DEFAULT_OPTIONS = new FindOneAndReplaceOptions();
  private FindOneAndReplaceOptions options = DEFAULT_OPTIONS;

  /**
   * Constructs a new `FindOneAndReplace` instance with the specified collection supplier and default replacement
   * options.
   *
   * @param collection The supplier of the MongoDB collection.
   */
  private FindOneAndReplace(final CollectionBuilder collection) {
    super(collection,
          true);
  }

  /**
   * Creates a new instance of `FindOneAndReplace` with the specified MongoDB collection supplier and default
   * replacement options.
   *
   * @param collection The supplier of the MongoDB collection to perform the replacement operation.
   * @return A new `FindOneAndReplace` instance with default replacement options.
   */
  public static FindOneAndReplace of(final CollectionBuilder collection) {
    return new FindOneAndReplace(collection);
  }

  /**
   * Sets the replacement options to be used for the operation.
   *
   * @param options The options to perform the operation.
   * @return This instance with the new options.
   */
  public FindOneAndReplace withOptions(final FindOneAndReplaceOptions options) {
    this.options = requireNonNull(options);
    return this;
  }

  /**
   * Applies the find one and replace operation to the specified MongoDB collection with the provided query and
   * replacement document.
   *
   * @param session     The MongoDB client session, or null if not within a session.
   * @param queryAndDoc The query and replacement document information.
   * @return An IO representing the result of the find one and replace operation.
   */
  @Override
  public IO<JsObj> apply(final ClientSession session,
                         final QueryAndDoc queryAndDoc) {
    Objects.requireNonNull(queryAndDoc);
    Supplier<JsObj> supplier = decorateWithEvent(() -> {
      var collection = requireNonNull(this.collection.get());
      return session == null ? collection
                                         .findOneAndReplace(toBson(queryAndDoc.query()),
                                                            queryAndDoc.newDoc(),
                                                            options
                                         ) : collection
                                                       .findOneAndReplace(session,
                                                                          toBson(queryAndDoc.query()),
                                                                          queryAndDoc.newDoc(),
                                                                          options
                                                       );
    },
                                                 FIND_ONE_AND_REPLACE
    );
    return IO.lazy(supplier);
  }

  /**
   * Disables the recording of Java Flight Recorder (JFR) events. When events recording is disabled, the operation will
   * not generate or log JFR events for its operations.
   *
   * @return This operation instance with JFR event recording disabled.
   */
  public FindOneAndReplace withoutRecordedEvents() {
    this.recordEvents = false;
    return this;
  }
}
