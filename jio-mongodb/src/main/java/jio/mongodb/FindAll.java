package jio.mongodb;

import jsonvalues.JsArray;

/**
 * Represents a MongoDB find operation to retrieve all documents in a collection asynchronously using
 * {@link jio.Lambda lambdas}. This class is a specific implementation of the {@link Find} class for querying all
 * documents and converting the result to a {@link jsonvalues.JsArray}.
 *
 * @see Find
 * @see CollectionSupplier
 */

public final class FindAll extends Find<JsArray> {

    private FindAll(final CollectionSupplier collection) {
        super(collection,
              Converters.iterable2JsArray
             );
    }

    /**
     * Creates a new instance of {@code FindAll} with the specified MongoDB collection supplier.
     *
     * @param collection the supplier of the MongoDB collection to query
     * @return a new {@code FindAll} instance for querying all documents in the collection
     */
    public static FindAll of(final CollectionSupplier collection) {
        return new FindAll(collection);
    }

    /**
     * Disables the recording of Java Flight Recorder (JFR) events. When events recording is disabled,
     * the operation will not generate or log JFR events for its operations.
     *
     * @return This operation instance with JFR event recording disabled.
     */
    public FindAll withoutRecordedEvents(){
        this.recordEvents = false;
        return this;
    }
}
