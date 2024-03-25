package jio.mongodb;

import jsonvalues.JsObj;

/**
 * Represents a set of options for configuring MongoDB find operations. These options allow you to specify various
 * criteria for filtering, sorting, projecting, and controlling the behavior of find queries.
 *
 * <p>This class is typically used in conjunction with MongoDB find operations to customize the query behavior.</p>
 *
 * @param filter          The filter criteria to apply to the find operation. This determines which documents match the
 *                        query.
 * @param sort            The sort criteria for ordering the results of the find operation. If not specified, the
 *                        results are returned in the order they appear in the collection.
 * @param projection      The projection criteria for specifying which fields to include or exclude in the query
 *                        results. If not specified, all fields are included.
 * @param hint            The hint criteria for optimizing query performance. If not specified, MongoDB will choose the
 *                        index to use.
 * @param max             The maximum values for indexed fields in the query. If not specified, there is no maximum
 *                        limit.
 * @param min             The minimum values for indexed fields in the query. If not specified, there is no minimum
 *                        limit.
 * @param hintString      A hint string for the query optimizer to use a specific index. If not specified, MongoDB will
 *                        choose the index.
 * @param skip            The number of documents to skip in the query results before returning. If not specified, no
 *                        documents are skipped.
 * @param limit           The maximum number of documents to return in the query results. If not specified, all matching
 *                        documents are returned.
 * @param showRecordId    Indicates whether to include the record ID field in the query results. If set to {@code true},
 *                        the record ID is included.
 * @param returnKey       Indicates whether to return only the keys of the result documents. If set to {@code true},
 *                        only the keys are returned.
 * @param comment         A comment associated with the query, which can provide context for the query in the MongoDB
 *                        logs.
 * @param noCursorTimeout Indicates whether to prevent the cursor from timing out automatically. If set to {@code true},
 *                        the cursor does not time out.
 * @param partial         Indicates whether to return partial results if some shards are unavailable. If set to
 *                        {@code true}, partial results are returned.
 * @param batchSize       The maximum number of documents to retrieve per batch. If not specified, the server default is
 *                        used.
 * @param maxAwaitTime    The maximum time, in milliseconds, that the server should allow the query to run. If not
 *                        specified, there is no time limit.
 * @param maxTime         The maximum time, in milliseconds, that the server should allow the query to execute. If not
 *                        specified, there is no time limit.
 * @see FindOne
 * @see FindAll
 */
record FindOptions(JsObj filter,
                   JsObj sort,
                   JsObj projection,
                   JsObj hint,
                   JsObj max,
                   JsObj min,
                   String hintString,
                   int skip,
                   int limit,
                   boolean showRecordId,
                   boolean returnKey,
                   String comment,
                   boolean noCursorTimeout,
                   boolean partial,
                   int batchSize,
                   long maxAwaitTime,
                   long maxTime) {

}
