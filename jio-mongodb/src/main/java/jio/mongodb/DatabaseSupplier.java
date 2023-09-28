package jio.mongodb;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoDatabase;

import java.util.Objects;
import java.util.function.Supplier;

public class DatabaseSupplier implements Supplier<MongoDatabase> {

    final MongoClient client;
    final String name;
    volatile MongoDatabase database;


    public DatabaseSupplier(final MongoClient client,
                            final String name
                           ) {
        this.client = Objects.requireNonNull(client);
        this.name = Objects.requireNonNull(name);
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
