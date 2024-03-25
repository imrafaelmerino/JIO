package jio.mongodb;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoDatabase;

import java.util.Objects;
import java.util.function.Supplier;

/**
 * A thread-safe builder for building a MongoDB database instance.
 * <p><strong>Note:</strong>. This builder ensures that the MongoDB
 * database is lazily initialized and cached for efficient reuse.
 * <p>
 * This class is used internally by the {@link CollectionBuilder} to get the {@link MongoDatabase} instance
 *
 * @see CollectionBuilder
 */
public final class DatabaseBuilder implements Supplier<MongoDatabase> {

  final MongoClient client;
  final String name;
  volatile MongoDatabase database;

  DatabaseBuilder(final MongoClient client,
                  final String name) {
    this.client = Objects.requireNonNull(client);
    this.name = Objects.requireNonNull(name);
  }

  /**
   * Constructs a new DatabaseBuilder.
   *
   * @param client The MongoDB client.
   * @param name   The name of the MongoDB database to obtain.
   * @return a database builder
   */
  public static DatabaseBuilder of(final MongoClient client,
                                   final String name) {
    return new DatabaseBuilder(client,
                               name);
  }

  @Override
  public MongoDatabase get() {

    MongoDatabase localRef = database;
    if (localRef == null) {
      synchronized (this) {
        localRef = database;
        if (localRef == null) {
          database = localRef = client.getDatabase(name);
        }
      }
    }
    return localRef;
  }
}
