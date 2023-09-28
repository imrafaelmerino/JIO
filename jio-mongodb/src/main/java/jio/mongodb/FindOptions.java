package jio.mongodb;

import jsonvalues.JsObj;

import java.util.Objects;

import static java.util.Objects.requireNonNull;
/**
 * Represents a set of options for configuring MongoDB find operations. These options allow you to specify various
 * criteria for filtering, sorting, projecting, and controlling the behavior of find queries.
 *
 * <p>This class is typically used in conjunction with MongoDB find operations to customize the query behavior.</p>
 *
 * @see FindOne
 * @see FindAll
 */
public final class FindOptions {

    /**
     * The filter criteria to apply to the find operation. This determines which documents match the query.
     */
    public final JsObj filter;
    /**
     * The sort criteria for ordering the results of the find operation. If not specified, the results are returned in
     * the order they appear in the collection.
     */
    public final JsObj sort;
    /**
     * The projection criteria for specifying which fields to include or exclude in the query results. If not specified,
     * all fields are included.
     */
    public final JsObj projection;
    /**
     * The hint criteria for optimizing query performance. If not specified, MongoDB will choose the index to use.
     */
    public final JsObj hint;
    /**
     * The maximum values for indexed fields in the query. If not specified, there is no maximum limit.
     */

    public final JsObj max;
    /**
     * The minimum values for indexed fields in the query. If not specified, there is no minimum limit.
     */
    public final JsObj min;
    /**
     * A hint string for the query optimizer to use a specific index. If not specified, MongoDB will choose the index.
     */
    public final String hintString;
    /**
     * The number of documents to skip in the query results before returning. If not specified, no documents are skipped.
     */
    public final int skip;
    /**
     * The maximum number of documents to return in the query results. If not specified, all matching documents are returned.
     */
    public final int limit;
    /**
     * Indicates whether to include the record ID field in the query results. If set to {@code true}, the record ID is included.
     */
    public final boolean showRecordId;
    /**
     * Indicates whether to return only the keys of the result documents. If set to {@code true}, only the keys are returned.
     */
    public final boolean returnKey;
    /**
     * A comment associated with the query, which can provide context for the query in the MongoDB logs.
     */
    public final String comment;
    /**
     * Indicates whether to prevent the cursor from timing out automatically. If set to {@code true}, the cursor does not time out.
     */
    public final boolean noCursorTimeout;
    /**
     * Indicates whether to return partial results if some shards are unavailable. If set to {@code true}, partial results are returned.
     */
    public final boolean partial;
    /**
     * The maximum number of documents to retrieve per batch. If not specified, the server default is used.
     */
    public final int batchSize;
    /**
     * The maximum time, in milliseconds, that the server should allow the query to run. If not specified, there is no time limit.
     */
    public final long maxAwaitTime;
    /**
     * The maximum time, in milliseconds, that the server should allow the query to execute. If not specified, there is no time limit.
     */
    public final long maxTime;

    @SuppressWarnings({"squid:S107"})
        //it's private, needed to create a builder. End user will never have to deal with it
    FindOptions(final JsObj filter,
                final JsObj sort,
                final JsObj projection,
                final JsObj hint,
                final JsObj max,
                final JsObj min,
                final String hintString,
                final int skip,
                final int limit,
                final boolean showRecordId,
                final boolean returnKey,
                final String comment,
                final boolean noCursorTimeout,
                final boolean partial,
                final int batchSize,
                final long maxAwaitTime,
                final long maxTime
               ) {
        this.filter = requireNonNull(filter);
        this.sort = sort;
        this.projection = projection;
        this.hint = hint;
        this.max = max;
        this.min = min;
        this.hintString = hintString;
        this.skip = skip;
        this.limit = limit;
        this.showRecordId = showRecordId;
        this.returnKey = returnKey;
        this.comment = comment;
        this.noCursorTimeout = noCursorTimeout;
        this.partial = partial;
        this.batchSize = batchSize;
        this.maxAwaitTime = maxAwaitTime;
        this.maxTime = maxTime;
    }
    /**
     * Creates a new {@code FindOptions} instance with the specified filter criteria.
     *
     * @param filter the filter criteria for the query
     * @return a new {@code FindOptions} instance with the specified filter criteria
     */
    public static FindOptions ofFilter(final JsObj filter) {
        return new FindOptionsBuilder().filter(requireNonNull(filter))
                                       .create();
    }
    /**
     * Creates a new {@code FindOptions} instance with the specified filter and projection criteria.
     *
     * @param filter     the filter criteria for the query
     * @param projection the projection criteria for specifying which fields to include or exclude
     * @return a new {@code FindOptions} instance with the specified filter and projection criteria
     */
    public static FindOptions ofFilter(final JsObj filter,
                                       final JsObj projection
                                      ) {
        return new FindOptionsBuilder().filter(requireNonNull(filter))
                                       .projection(requireNonNull(projection))
                                       .create();
    }
    /**
     * Creates a new {@code FindOptions} instance with the specified filter, projection, and sort criteria.
     *
     * @param filter     the filter criteria for the query
     * @param projection the projection criteria for specifying which fields to include or exclude
     * @param sort       the sort criteria for ordering the results
     * @return a new {@code FindOptions} instance with the specified filter, projection, and sort criteria
     */
    public static FindOptions ofFilter(final JsObj filter,
                                       final JsObj projection,
                                       final JsObj sort
                                      ) {
        return new FindOptionsBuilder().filter(requireNonNull(filter))
                                       .projection(requireNonNull(projection))
                                       .sort(requireNonNull(sort))
                                       .create();
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        var that = (FindOptions) o;
        return skip == that.skip &&
                limit == that.limit &&
                showRecordId == that.showRecordId &&
                returnKey == that.returnKey &&
                noCursorTimeout == that.noCursorTimeout &&
                partial == that.partial &&
                batchSize == that.batchSize &&
                maxAwaitTime == that.maxAwaitTime &&
                maxTime == that.maxTime &&
                filter.equals(that.filter) &&
                Objects.equals(sort,
                               that.sort
                              ) &&
                Objects.equals(projection,
                               that.projection
                              ) &&
                Objects.equals(hint,
                               that.hint
                              ) &&
                Objects.equals(max,
                               that.max
                              ) &&
                Objects.equals(min,
                               that.min
                              ) &&
                Objects.equals(hintString,
                               that.hintString
                              ) &&
                Objects.equals(comment,
                               that.comment
                              );
    }

    @Override
    public int hashCode() {
        return Objects.hash(filter,
                            sort,
                            projection,
                            hint,
                            max,
                            min,
                            hintString,
                            skip,
                            limit,
                            showRecordId,
                            returnKey,
                            comment,
                            noCursorTimeout,
                            partial,
                            batchSize,
                            maxAwaitTime,
                            maxTime
                           );
    }


}
