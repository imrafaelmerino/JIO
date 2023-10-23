package jio.mongodb;

import jsonvalues.JsObj;

/**
 * Represents a combination of a query document and an update document for MongoDB operations.
 *
 * <p>In MongoDB, a query document is used to specify which documents in a collection should be updated, and an update document defines how the updates should be performed. The `QueryUpdate` class encapsulates both the query and update documents for use in update operations, making it a convenient way to express these two key aspects of MongoDB updates.</p>
 *
 * @param query  The query document that identifies the documents to be updated.
 * @param update The update document specifying the changes to be made to the matched documents.
 */
public record QueryUpdate(JsObj query, JsObj update) {
    public QueryUpdate {
        if(query == null) throw new IllegalArgumentException("query is null");
        if(update == null) throw new IllegalArgumentException("update is null");
    }
}
