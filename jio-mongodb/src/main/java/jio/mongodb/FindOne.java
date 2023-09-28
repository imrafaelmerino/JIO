package jio.mongodb;

import jsonvalues.JsObj;

/**
 * Represents a MongoDB find operation to retrieve a single document from a collection asynchronously using {@link jio.Lambda lambdas}.
 * This class is a specific implementation of the {@link Find} class for querying a single document and converting the result
 * to a {@link jsonvalues.JsObj}.
 *
 * @see Find
 * @see CollectionSupplier
 */
public final class FindOne extends Find<JsObj> {

    private FindOne(final CollectionSupplier collection) {
        super(collection,
              Converters.iterableFirst
             );
    }
    /**
     * Creates a new instance of {@code FindOne} with the specified MongoDB collection supplier.
     *
     * @param collection the supplier of the MongoDB collection to query
     * @return a new {@code FindOne} instance for querying a single document in the collection
     */
    public static FindOne of(final CollectionSupplier collection) {
        return new FindOne(collection);
    }
}
