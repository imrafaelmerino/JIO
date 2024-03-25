package jio.mongodb;

import com.mongodb.client.ClientSession;
import com.mongodb.client.FindIterable;
import jio.IO;
import jsonvalues.JsObj;

/**
 * Represents a MongoDB find operation to retrieve all documents in a collection using {@link jio.Lambda lambdas}. This
 * class is a specific implementation of the {@link Find} class for querying all documents and converting the result to
 * a {@link jsonvalues.JsArray}.
 * <p>
 * The `FindAll` class is designed for querying all documents in a MongoDB collection and converting the result to a
 * {@link jsonvalues.JsArray}. This class simplifies the process of performing find operations and provides a convenient
 * way to retrieve all documents in a collection asynchronously.
 * <p>
 * To use this class effectively, you can set an executor for asynchronous execution and disable the recording of Java
 * Flight Recorder (JFR) events if needed. Additionally, you can use the functionality provided by the parent class
 * {@link Find} for more advanced querying options and customization.
 *
 * @see CollectionBuilder
 */
public final class FindAll extends Find implements MongoLambda<FindBuilder, FindIterable<JsObj>> {

  /**
   * Constructs a new `FindAll` instance with the specified collection supplier.
   *
   * @param collection The supplier of the MongoDB collection.
   */
  private FindAll(final CollectionBuilder collection) {
    super(collection,
          true);
  }

  /**
   * Creates a new instance of `FindAll` with the specified MongoDB collection supplier.
   *
   * @param collection The supplier of the MongoDB collection to query.
   * @return A new `FindAll` instance for querying all documents in the collection.
   */
  public static FindAll of(final CollectionBuilder collection) {
    return new FindAll(collection);
  }

  /**
   * Disables the recording of Java Flight Recorder (JFR) events. When events recording is disabled, the operation will
   * not generate or log JFR events for its operations.
   *
   * @return This operation instance with JFR event recording disabled.
   */
  public FindAll withoutRecordedEvents() {
    this.recordEvents = false;
    return this;
  }

  /**
   * Applies the find all operation to the specified MongoDB collection using the provided query builder.
   *
   * @param session     The MongoDB client session, or null if not within a session.
   * @param findBuilder The query builder for customizing the find operation.
   * @return An IO representing the result of the find all operation as a {@link FindIterable<JsObj>}.
   */
  @Override
  public IO<FindIterable<JsObj>> apply(final ClientSession session,
                                       final FindBuilder findBuilder) {
    return query(session,
                 findBuilder);
  }

}
