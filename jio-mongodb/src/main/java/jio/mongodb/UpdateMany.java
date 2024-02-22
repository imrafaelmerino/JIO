package jio.mongodb;

import static java.util.Objects.requireNonNull;
import static jio.mongodb.Converters.toBson;
import static jio.mongodb.MongoOpEvent.OP.UPDATE_MANY;

import com.mongodb.client.ClientSession;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.result.UpdateResult;
import java.util.Objects;
import java.util.function.Supplier;
import jio.IO;

/**
 * A class for performing updateCommands many operations on a MongoDB collection.
 * <p>
 * The `UpdateMany` class is designed for performing updateCommands operations to modify multiple documents within a
 * MongoDB collection. It provides flexibility in handling the result and allows you to specify various options for the
 * updateCommands operation. You can create instances of this class with the specified collection supplier, and
 * customize the behavior using options such as updateCommands options, executors, and more.
 * <p>
 * To use this class effectively, you can set the updateCommands options for the operation, specify an executor for
 * asynchronous execution, and disable the recording of Java Flight Recorder (JFR) events if needed. Additionally, you
 * can use the provided `QueryUpdate` object to define the query and updateCommands criteria for the operation.
 *
 * @see CollectionBuilder
 * @see QueryAndCommand
 */
public final class UpdateMany extends Op implements MongoLambda<QueryAndCommand, UpdateResult> {

  private static final UpdateOptions DEFAULT_OPTIONS = new UpdateOptions();
  private UpdateOptions options = DEFAULT_OPTIONS;

  /**
   * Constructs a new UpdateMany instance with the specified collection supplier and default updateCommands options.
   *
   * @param collection The supplier for the MongoDB collection.
   */
  private UpdateMany(final CollectionBuilder collection) {
    super(collection,
          true);
  }

  /**
   * Creates an UpdateMany instance with the specified collection supplier using default options.
   *
   * @param collection The supplier for the MongoDB collection.
   * @return An UpdateMany instance with default options.
   */
  public static UpdateMany of(final CollectionBuilder collection) {
    return new UpdateMany(collection);
  }

  /**
   * Sets the updateCommands options to be used for the operation.
   *
   * @param options The options to perform the operation.
   * @return This instance with the new options.
   */
  public UpdateMany withOptions(final UpdateOptions options) {
    this.options = requireNonNull(options);
    return this;
  }

  /**
   * Applies the updateCommands many operation to the specified MongoDB collection with a query and an updateCommands.
   *
   * @param session     The MongoDB client session, or null if not within a session.
   * @param queryUpdate The query and updateCommands criteria for the operation.
   * @return An IO representing the result of the updateCommands many operation.
   */
  @Override
  public IO<UpdateResult> apply(final ClientSession session,
                                final QueryAndCommand queryUpdate) {
    Objects.requireNonNull(queryUpdate);

    Supplier<UpdateResult> supplier =
        decorateWithEvent(() -> {
                            var collection = requireNonNull(this.collection.get());
                            return session == null ? collection.updateMany(toBson(queryUpdate.query()),
                                                                           toBson(queryUpdate.updateCommands()),
                                                                           options
                                                                          ) : collection.updateMany(session,
                                                                                                    toBson(queryUpdate.query()),
                                                                                                    toBson(queryUpdate.updateCommands()),
                                                                                                    options
                                                                                                   );
                          },
                          UPDATE_MANY);
    return IO.managedLazy(supplier);
  }

  /**
   * Disables the recording of Java Flight Recorder (JFR) events. When events recording is disabled, the operation will
   * not generate or log JFR events for its operations.
   *
   * @return This operation instance with JFR event recording disabled.
   */
  public UpdateMany withoutRecordedEvents() {
    this.recordEvents = false;
    return this;
  }
}
