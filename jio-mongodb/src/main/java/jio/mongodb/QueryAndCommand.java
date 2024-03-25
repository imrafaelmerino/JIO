package jio.mongodb;

import jsonvalues.JsObj;

/**
 * Represents a combination of a query document and an updateCommands document for MongoDB operations.
 *
 * <p>In MongoDB, a query document is used to specify which documents in a collection should be updated, and an
 * updateCommands document defines how the updates should be performed. The `QueryUpdate` class encapsulates both the
 * query and updateCommands documents for use in updateCommands operations, making it a convenient way to express these
 * two key aspects of MongoDB updates.</p>
 *
 * @param query          The query document that identifies the documents to be updated.
 * @param updateCommands The updateCommands document specifying the changes to be made to the matched documents.
 */
public record QueryAndCommand(JsObj query,
                              JsObj updateCommands) {

  /**
   * Creates a query and a new obj with the update commands
   *
   * @param query          the query
   * @param updateCommands the new doc to replace the one found by the query
   */
  public QueryAndCommand {
    if (query == null) {
      throw new IllegalArgumentException("query is null");
    }
    if (updateCommands == null) {
      throw new IllegalArgumentException("updateCommands is null");
    }
  }
}
