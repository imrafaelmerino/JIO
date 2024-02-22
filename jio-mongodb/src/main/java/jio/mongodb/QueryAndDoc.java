package jio.mongodb;

import jsonvalues.JsObj;

/**
 * Represents a combination of a query document and a new document for MongoDB replace operations.
 *
 * <p>In MongoDB, a query document is used to specify which documents in a collection should be replaced, and a new
 * document defines the structure and content of the replacement documents. The `QueryReplace` class encapsulates both
 * the query and the new document for use in replace operations, providing a convenient way to express these two key
 * aspects of MongoDB document replacement.</p>
 *
 * @param query  The query document that identifies the documents to be replaced.
 * @param newDoc The new document specifying the structure and content of the replacement documents.
 */
public record QueryAndDoc(JsObj query,
                          JsObj newDoc) {

  /**
   * Creates a query and a new doc
   *
   * @param query  the query
   * @param newDoc the new doc to replace the one found by the query
   */
  public QueryAndDoc {
    if (query == null) {
      throw new IllegalArgumentException("query is null");
    }
    if (newDoc == null) {
      throw new IllegalArgumentException("newDoc is null");
    }
  }
}
