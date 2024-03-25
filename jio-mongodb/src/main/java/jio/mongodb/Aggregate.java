package jio.mongodb;

import static java.util.Objects.requireNonNull;
import static jio.mongodb.MongoOpEvent.OP.AGGREGATE;

import com.mongodb.client.AggregateIterable;
import com.mongodb.client.ClientSession;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;
import jio.IO;
import jsonvalues.JsObj;
import org.bson.conversions.Bson;

/**
 * A class for performing aggregation operations on a MongoDB collection.
 * <p>
 * This class represents an aggregation operation, using a list of Bson pipeline stages, to be applied to a MongoDB
 * collection. The aggregation operation can be executed within a MongoDB client session if one is provided.
 * <p>
 * To use this class effectively, it can be configured with an optional executor for running the aggregation operation.
 *
 * @see MongoLambda
 * @see Converters
 */
public final class Aggregate extends Op implements MongoLambda<List<Bson>, AggregateIterable<JsObj>> {

  /**
   * Constructs a new Aggregate instance with the specified collection supplier and result converter function.
   *
   * @param collection The supplier for the MongoDB collection.
   */
  private Aggregate(final CollectionBuilder collection) {
    super(collection,
          true);
  }

  /**
   * Returns a new Aggregate instance with the specified collection supplier and result converter function.
   *
   * @param collection The supplier for the MongoDB collection.
   * @return a new Aggregate instance
   */
  public static Aggregate of(final CollectionBuilder collection) {
    return new Aggregate(collection);
  }

  /**
   * Applies the aggregation operation to the specified collection with a list of pipeline stages.
   *
   * @param session The MongoDB client session, or null if not within a session.
   * @param stages  The list of Bson pipeline stages for aggregation.
   * @return An IO representing the result of the aggregation operation.
   */
  @Override
  public IO<AggregateIterable<JsObj>> apply(final ClientSession session,
                                            final List<Bson> stages) {
    Objects.requireNonNull(stages);
    Supplier<AggregateIterable<JsObj>> supplier = decorateWithEvent(() -> {
      var collection = requireNonNull(this.collection.get());
      return session == null ? collection.aggregate(stages) : collection.aggregate(session,
                                                                                   stages);
    },
                                                                    AGGREGATE
    );

    return IO.lazy(supplier);
  }

  /**
   * Disables the recording of Java Flight Recorder (JFR) events. When events recording is disabled, the operation will
   * not generate or log JFR events for its operations.
   *
   * @return This operation instance with JFR event recording disabled.
   */
  public Aggregate withoutRecordedEvents() {
    this.recordEvents = false;
    return this;
  }

}
