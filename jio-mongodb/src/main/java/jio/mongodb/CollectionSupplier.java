package jio.mongodb;

import com.mongodb.client.MongoCollection;
import jsonvalues.JsObj;

import java.util.Objects;
import java.util.function.Supplier;

public class CollectionSupplier implements Supplier<MongoCollection<JsObj>> {

    final DatabaseSupplier database;
    final String name;
    volatile MongoCollection<JsObj> collection;

    public CollectionSupplier(final DatabaseSupplier database,
                              final String name
                             ) {
        this.database = Objects.requireNonNull(database);
        this.name = Objects.requireNonNull(name);
    }


    @Override
    public MongoCollection<JsObj> get() {

        var localRef = collection;
        if (localRef == null) {
            synchronized (this) {
                localRef = collection;
                if (localRef == null) {
                    collection = localRef = database.get().getCollection(name, JsObj.class);
                }
            }
        }
        return localRef;
    }
}
