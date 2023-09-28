package jio.mongodb;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoDatabase;

import java.util.Objects;
import java.util.function.Supplier;

/**
 * A thread-safe supplier for obtaining a MongoDB database instance.
 */
public class DatabaseSupplier implements Supplier<MongoDatabase> {

    final MongoClient client;
    final String name;
    volatile MongoDatabase database;

    /**
     * Constructs a new DatabaseSupplier.
     *
     * @param client The MongoDB client.
     * @param name   The name of the MongoDB database to obtain.
     */
    public DatabaseSupplier(final MongoClient client, final String name) {
        this.client = Objects.requireNonNull(client);
        this.name = Objects.requireNonNull(name);
    }

    /**
     * Gets the MongoDB database instance. This method ensures thread safety.
     *
     * @return The MongoDB database instance.
     */
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
