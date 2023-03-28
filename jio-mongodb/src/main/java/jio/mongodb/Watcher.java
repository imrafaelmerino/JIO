package jio.mongodb;

import com.mongodb.client.ChangeStreamIterable;
import com.mongodb.client.MongoCollection;
import jsonvalues.JsObj;

import java.util.function.Consumer;

import static java.util.Objects.requireNonNull;

public final class Watcher implements Consumer<MongoCollection<JsObj>> {

    public final Consumer<ChangeStreamIterable<JsObj>> consumer;

    public Watcher(final Consumer<ChangeStreamIterable<JsObj>> consumer) {
        this.consumer = requireNonNull(consumer);
    }

    @Override
    public void accept(final MongoCollection<JsObj> collection) {
        consumer.accept(requireNonNull(collection).watch());
    }
}
