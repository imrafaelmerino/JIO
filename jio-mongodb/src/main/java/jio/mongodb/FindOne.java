package jio.mongodb;

import com.mongodb.client.ClientSession;
import com.mongodb.client.MongoIterable;
import jio.IO;
import jsonvalues.JsObj;

/**
 * Represents a MongoDB find operation to retrieve a single document from a collection asynchronously using
 * {@link jio.Lambda lambdas}. This class is a specific implementation of the {@link Find} class for querying a single
 * document and converting the result to a {@link jsonvalues.JsObj}.
 * <p>
 * The `FindOne` class is designed for querying and retrieving a single document from a MongoDB collection that matches
 * the specified query criteria. This operation is performed asynchronously, allowing you to customize the query
 * options, execution behavior, and result conversion. You can create instances of this class with the specified
 * collection supplier, set an executor for asynchronous execution, and disable the recording of Java Flight Recorder
 * (JFR) events if needed.
 * <p>
 * To use this class effectively, you can set the query options for the operation, specify an executor for asynchronous
 * execution, and disable the recording of JFR events if necessary. The find one operation retrieves a single document
 * from the collection based on the provided query criteria and converts the result to a {@link jsonvalues.JsObj} for
 * further processing.
 *
 * @see Find
 * @see CollectionBuilder
 */
public final class FindOne extends Find implements MongoLambda<FindBuilder, JsObj> {

  /**
   * Constructs a new `FindOne` instance with the specified collection supplier.
   *
   * @param collection The supplier of the MongoDB collection.
   */
  private FindOne(final CollectionBuilder collection) {
    super(collection,
          true);
  }

  /**
   * Creates a new instance of `FindOne` with the specified MongoDB collection supplier.
   *
   * @param collection The supplier of the MongoDB collection to query.
   * @return A new `FindOne` instance for querying a single document in the collection.
   */
  public static FindOne of(final CollectionBuilder collection) {
    return new FindOne(collection);
  }

  /**
   * Disables the recording of Java Flight Recorder (JFR) events. When events recording is disabled, the operation will
   * not generate or log JFR events for its operations.
   *
   * @return This operation instance with JFR event recording disabled.
   */
  public FindOne withoutRecordedEvents() {
    this.recordEvents = false;
    return this;
  }

  /**
   * Applies the find one operation to the specified MongoDB collection using the provided query builder.
   *
   * @param session The MongoDB client session, or null if not within a session.
   * @param builder The query builder for customizing the find operation.
   * @return An IO representing the result of the find one operation as a {@link jsonvalues.JsObj}.
   */
  @Override
  public IO<JsObj> apply(final ClientSession session,
                         final FindBuilder builder) {
    return query(session,
                 builder).map(MongoIterable::first);
  }

}
