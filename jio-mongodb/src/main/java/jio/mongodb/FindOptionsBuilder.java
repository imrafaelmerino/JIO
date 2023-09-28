package jio.mongodb;

import jsonvalues.JsObj;

import java.util.concurrent.TimeUnit;

import static java.util.Objects.requireNonNull;
/**
 * Builder class for creating {@link FindOptions} objects with customizable query options for MongoDB find operations.
 * Use this builder to configure and create instances of {@code FindOptions} with specific filter criteria, sort order,
 * projection, hints, and other options.
 *
 * <p>This builder provides a convenient way to construct {@code FindOptions} objects with various query criteria
 * while ensuring that the created options are valid for use in MongoDB find operations.</p>
 *
 * @see FindOptions
 */
public final class FindOptionsBuilder {

    private JsObj filter;
    private JsObj sort;
    private JsObj projection;
    private JsObj hint;
    private JsObj max;
    private JsObj min;
    private String hintString;
    private int skip = 0;
    private int limit = 0;
    private boolean showRecordId;
    private boolean returnKey;
    private String comment;
    private boolean noCursorTimeout;
    private boolean partial;
    private int batchSize = 100;
    private long maxAwaitTime = 0L;
    private long maxTime = 0L;

    /**
     * Sets the filter criteria for the query.
     *
     * @param filter the filter criteria for the query
     * @return this builder instance for method chaining
     */
    public FindOptionsBuilder filter(final JsObj filter) {
        this.filter = filter;
        return this;
    }

    /**
     * Sets the sort criteria for ordering the results.
     *
     * @param sort the sort criteria for ordering the results
     * @return this builder instance for method chaining
     */
    public FindOptionsBuilder sort(final JsObj sort) {
        this.sort = sort;
        return this;
    }

    /**
     * Sets the projection criteria for specifying which fields to include or exclude.
     *
     * @param projection the projection criteria for specifying which fields to include or exclude
     * @return this builder instance for method chaining
     */
    public FindOptionsBuilder projection(final JsObj projection) {
        this.projection = projection;
        return this;
    }

    /**
     * Sets the hint criteria for optimizing query performance.
     *
     * @param hint the hint criteria for optimizing query performance
     * @return this builder instance for method chaining
     */
    public FindOptionsBuilder hint(final JsObj hint) {
        this.hint = hint;
        return this;
    }

    /**
     * Sets the maximum values for indexed fields in the query.
     *
     * @param max the maximum values for indexed fields in the query
     * @return this builder instance for method chaining
     */
    public FindOptionsBuilder max(final JsObj max) {
        this.max = max;
        return this;
    }

    /**
     * Sets the minimum values for indexed fields in the query.
     *
     * @param min the minimum values for indexed fields in the query
     * @return this builder instance for method chaining
     */
    public FindOptionsBuilder min(final JsObj min) {
        this.min = min;
        return this;
    }

    /**
     * Sets a hint string for the query optimizer to use a specific index.
     *
     * @param hintString the hint string for the query optimizer
     * @return this builder instance for method chaining
     */
    public FindOptionsBuilder hintString(final String hintString) {
        this.hintString = hintString;
        return this;
    }

    /**
     * Sets the number of documents to skip in the query results before returning.
     *
     * @param skip the number of documents to skip
     * @return this builder instance for method chaining
     * @throws IllegalArgumentException if the provided skip value is less than 0
     */
    public FindOptionsBuilder skip(final int skip) {
        if (skip < 0) throw new IllegalArgumentException("skip is < 0");
        this.skip = skip;
        return this;
    }

    /**
     * Sets the maximum number of documents to return in the query results.
     *
     * @param limit the maximum number of documents to return
     * @return this builder instance for method chaining
     * @throws IllegalArgumentException if the provided limit value is less than 0
     */
    public FindOptionsBuilder limit(final int limit) {
        if (limit < 0) throw new IllegalArgumentException("limit is < 0");
        this.limit = limit;
        return this;
    }

    /**
     * Sets whether to include the record ID field in the query results.
     *
     * @param showRecordId {@code true} to include the record ID field, {@code false} to exclude it
     * @return this builder instance for method chaining
     */
    public FindOptionsBuilder showRecordId(final boolean showRecordId) {
        this.showRecordId = showRecordId;
        return this;
    }

    /**
     * Sets whether to return only the keys of the result documents.
     *
     * @param returnKey {@code true} to return only the keys, {@code false} to return full documents
     * @return this builder instance for method chaining
     */
    public FindOptionsBuilder returnKey(final boolean returnKey) {
        this.returnKey = returnKey;
        return this;
    }

    /**
     * Sets a comment associated with the query, which can provide context for the query in the MongoDB logs.
     *
     * @param comment a comment associated with the query
     * @return this builder instance for method chaining
     */
    public FindOptionsBuilder comment(final String comment) {
        this.comment = comment;
        return this;
    }

    /**
     * Sets whether to prevent the cursor from timing out automatically.
     *
     * @param noCursorTimeout {@code true} to prevent cursor timeout, {@code false} to allow cursor timeout
     * @return this builder instance for method chaining
     */
    public FindOptionsBuilder noCursorTimeout(final boolean noCursorTimeout) {
        this.noCursorTimeout = noCursorTimeout;
        return this;
    }

    /**
     * Sets whether to return partial results if some shards are unavailable.
     *
     * @param partial {@code true} to return partial results, {@code false} to require all shards to be available
     * @return this builder instance for method chaining
     */
    public FindOptionsBuilder partial(final boolean partial) {
        this.partial = partial;
        return this;
    }

    /**
     * Sets the maximum number of documents to retrieve per batch.
     *
     * @param batchSize the maximum number of documents to retrieve per batch
     * @return this builder instance for method chaining
     * @throws IllegalArgumentException if the provided batchSize value is less than 0
     */
    public FindOptionsBuilder batchSize(final int batchSize) {
        if (batchSize < 0) throw new IllegalArgumentException("batchSize is < 0");
        this.batchSize = batchSize;
        return this;
    }

    /**
     * Sets the maximum time, in milliseconds, that the server should allow the query to run.
     *
     * @param maxAwaitTime the maximum time for the server to allow the query to run
     * @param unit         the time unit for the maxAwaitTime value
     * @return this builder instance for method chaining
     * @throws IllegalArgumentException if the provided maxAwaitTime value is less than 0
     */
    public FindOptionsBuilder maxAwaitTime(final int maxAwaitTime, final TimeUnit unit) {
        if (maxAwaitTime < 0) throw new IllegalArgumentException("maxAwaitTime is < 0");
        this.maxAwaitTime = requireNonNull(unit).toMillis(maxAwaitTime);
        return this;
    }

    /**
     * Sets the maximum time, in milliseconds, that the server should allow the query to run.
     *
     * @param maxTime the maximum time for the server to allow the query to run
     * @param unit    the time unit for the maxTime value
     * @return this builder instance for method chaining
     * @throws IllegalArgumentException if the provided maxTime value is less than 0
     */
    public FindOptionsBuilder maxTime(final int maxTime, final TimeUnit unit) {
        if (maxTime < 0) throw new IllegalArgumentException("maxTime is < 0");
        this.maxTime = requireNonNull(unit).toMillis(maxTime);
        return this;
    }

    /**
     * Creates a new {@code FindOptions} instance with the configured query options.
     *
     * @return a new {@code FindOptions} instance with the configured query options
     */
    public FindOptions create() {
        return new FindOptions(filter, sort, projection, hint, max, min, hintString, skip, limit,
                               showRecordId, returnKey, comment, noCursorTimeout, partial, batchSize, maxAwaitTime, maxTime);
    }
}
