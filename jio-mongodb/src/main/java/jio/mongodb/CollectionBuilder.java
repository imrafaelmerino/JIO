package jio.mongodb;

import com.mongodb.client.MongoCollection;
import jsonvalues.JsObj;

import java.util.Objects;
import java.util.function.Supplier;

/**
 * A builder that provides a MongoDB collection of JSON objects ({@link JsObj}). This builder ensures that the MongoDB
 * collection is lazily initialized and cached for efficient reuse.
 *
 * <p>Instances of this class can be used to obtain a reference to a specific collection within a MongoDB database,
 * which can then be used to perform various database operations.</p>
 *
 * <p><strong>Note:</strong> This class is thread-safe, and its {@link #get()} method ensures safe and efficient
 * lazy initialization of the MongoDB collection.</p>
 * <p>
 * This class is used internally by the MongoLambdas implemented in the package jio.mongodb: {@link InsertOne},
 * {@link FindOne}, etc
 *
 * @see DatabaseBuilder
 */
public final class CollectionBuilder implements Supplier<MongoCollection<JsObj>> {

  final DatabaseBuilder database;
  final String name;
  volatile MongoCollection<JsObj> collection;

  CollectionBuilder(final DatabaseBuilder database,
                    final String name
  ) {
    this.database = Objects.requireNonNull(database);
    this.name = Objects.requireNonNull(name);
  }

  /**
   * Constructs a CollectionBuilder with the given DatabaseBuilder and collection name.
   *
   * @param database The supplier of the MongoDB database.
   * @param name     The name of the MongoDB collection.
   * @return a collection builder
   * @throws NullPointerException if either database or name is null.
   */
  public static CollectionBuilder of(final DatabaseBuilder database,
                                     final String name
  ) {
    return new CollectionBuilder(database,
                                 name);
  }

  /**
   * Gets the MongoDB collection. If the collection has not been initialized, it will be lazily initialized using the
   * associated database supplier.
   *
   * <p>This method is thread-safe, ensuring safe and efficient lazy initialization of the MongoDB collection.</p>
   *
   * @return The MongoDB collection of JSON objects.
   */
  @Override
  public MongoCollection<JsObj> get() {

    var localRef = collection;
    if (localRef == null) {
      synchronized (this) {
        localRef = collection;
        if (localRef == null) {
          collection = localRef = database.get()
                                          .getCollection(name,
                                                         JsObj.class);
        }
      }
    }
    return localRef;
  }
}
