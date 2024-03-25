package jio.mongodb;

import static java.util.Objects.requireNonNull;
import static jio.mongodb.Converters.toBson;
import static jio.mongodb.MongoOpEvent.OP.FIND_ONE_AND_DELETE;

import com.mongodb.client.ClientSession;
import com.mongodb.client.model.FindOneAndDeleteOptions;
import java.util.Objects;
import java.util.function.Supplier;
import jio.IO;
import jsonvalues.JsObj;

/**
 * Represents a MongoDB find one and delete operation to remove a single document from a collection asynchronously using
 * {@link jio.Lambda lambdas}. This class allows you to specify query criteria as a {@link jsonvalues.JsObj} and
 * provides options for controlling the behavior of the deletion operation, such as sort criteria and projection.
 * <p>
 * The `FindOneAndDelete` class is designed for deleting a single document from a MongoDB collection that matches the
 * specified query criteria. This operation is performed atomically and asynchronously, allowing you to customize the
 * deletion options and execution behavior. You can create instances of this class with the specified collection
 * supplier, set deletion options, and choose an executor for asynchronous execution.
 * <p>
 * To use this class effectively, you can set the deletion options for the operation, specify an executor for
 * asynchronous execution, and disable the recording of Java Flight Recorder (JFR) events if needed. The find one and
 * delete operation requires a query criteria, and it will delete a single document that matches the provided criteria
 * from the collection.
 *
 * @see CollectionBuilder
 */
public final class FindOneAndDelete extends Op implements MongoLambda<JsObj, JsObj> {

  private static final FindOneAndDeleteOptions DEFAULT_OPTIONS = new FindOneAndDeleteOptions();
  private FindOneAndDeleteOptions options = DEFAULT_OPTIONS;

  /**
   * Constructs a new `FindOneAndDelete` instance with the specified collection supplier and default deletion options.
   *
   * @param collection The supplier of the MongoDB collection.
   */
  private FindOneAndDelete(final CollectionBuilder collection) {
    super(collection,
          true);
  }

  /**
   * Creates a new instance of `FindOneAndDelete` with the specified MongoDB collection supplier and default deletion
   * options.
   *
   * @param collection The supplier of the MongoDB collection to perform the deletion operation.
   * @return A new `FindOneAndDelete` instance with default deletion options.
   */
  public static FindOneAndDelete of(final CollectionBuilder collection) {
    return new FindOneAndDelete(collection);
  }

  /**
   * Sets the deletion options to be used for the operation.
   *
   * @param options The options to perform the operation.
   * @return This instance with the new options.
   */
  public FindOneAndDelete withOptions(final FindOneAndDeleteOptions options) {
    this.options = requireNonNull(options);
    return this;
  }

  /**
   * Applies the find one and delete operation to the specified MongoDB collection with the provided query criteria.
   *
   * @param session The MongoDB client session, or null if not within a session.
   * @param query   The query criteria for identifying the document to delete.
   * @return An IO representing the result of the find one and delete operation.
   */
  @Override
  public IO<JsObj> apply(final ClientSession session,
                         final JsObj query) {
    Objects.requireNonNull(query);
    Supplier<JsObj> supplier = decorateWithEvent(() -> {
      var collection = requireNonNull(this.collection.get());
      return session == null ? collection.findOneAndDelete(toBson(query),
                                                           options) : collection.findOneAndDelete(session,
                                                                                                  toBson(query),
                                                                                                  options);
    },
                                                 FIND_ONE_AND_DELETE
    );
    return IO.lazy(supplier);
  }

  /**
   * Disables the recording of Java Flight Recorder (JFR) events. When events recording is disabled, the operation will
   * not generate or log JFR events for its operations.
   *
   * @return This operation instance with JFR event recording disabled.
   */
  public FindOneAndDelete withoutRecordedEvents() {
    this.recordEvents = false;
    return this;
  }

}
