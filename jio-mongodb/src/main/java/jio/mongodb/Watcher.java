package jio.mongodb;

import com.mongodb.client.ChangeStreamIterable;
import com.mongodb.client.MongoCollection;
import jsonvalues.JsObj;

import java.util.function.Consumer;

import static java.util.Objects.requireNonNull;

/**
 * A class for watching changes in a MongoDB collection.
 */
public final class Watcher implements Consumer<MongoCollection<JsObj>> {

    /**
     * The consumer to handle the change stream iterable.
     */
    private final Consumer<ChangeStreamIterable<JsObj>> consumer;


     private Watcher(final Consumer<ChangeStreamIterable<JsObj>> consumer) {
        this.consumer = requireNonNull(consumer);
    }

    /**
     * returns a new Watcher instance with the specified consumer.
     *
     * @param consumer The consumer to handle the change stream iterable.
     *
     * @return a Watcher
     */
    public static Watcher of(final Consumer<ChangeStreamIterable<JsObj>> consumer){
         return new Watcher(consumer);
    }

    /**
     * Accepts a MongoDB collection and starts watching for changes.
     *
     * @param collection The MongoDB collection to watch.
     */
    @Override
    public void accept(final MongoCollection<JsObj> collection) {
        consumer.accept(requireNonNull(collection).watch());
    }
}
