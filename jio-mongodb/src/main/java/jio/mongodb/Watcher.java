package jio.mongodb;

import com.mongodb.client.ChangeStreamIterable;
import jsonvalues.JsObj;

import java.util.function.Consumer;

import static java.util.Objects.requireNonNull;

/**
 * A class for watching changes in a MongoDB collection.
 */
public final class Watcher implements Consumer<CollectionBuilder> {

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
   * @return a Watcher
   */
  public static Watcher of(final Consumer<ChangeStreamIterable<JsObj>> consumer) {
    return new Watcher(consumer);
  }

  /**
   * Accepts a MongoDB collection and starts watching for changes.
   *
   * @param collection The MongoDB collection to watch.
   */
  @Override
  public void accept(final CollectionBuilder collection) {
    consumer.accept(requireNonNull(collection).get()
                                              .watch());
  }
}
