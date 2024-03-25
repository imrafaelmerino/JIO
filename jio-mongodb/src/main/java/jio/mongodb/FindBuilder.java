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
public final class FindBuilder {

  private final JsObj filter;
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

  private FindBuilder(final JsObj filter) {
    this.filter = requireNonNull(filter);
  }

  /**
   * create a builder to perform a query
   *
   * @param query the query
   * @return a find builder
   */
  public static FindBuilder of(final JsObj query) {
    return new FindBuilder(query);
  }

  /**
   * Sets the sort criteria for ordering the results.
   *
   * @param sort the sort criteria for ordering the results
   * @return this builder instance for method chaining (withSort)
   */
  public FindBuilder withSort(final JsObj sort) {
    this.sort = sort;
    return this;
  }

  /**
   * Sets the projection criteria for specifying which fields to include or exclude.
   *
   * @param projection the projection criteria for specifying which fields to include or exclude
   * @return this builder instance for method chaining (withProjection)
   */
  public FindBuilder withProjection(final JsObj projection) {
    this.projection = projection;
    return this;
  }

  /**
   * Sets the hint criteria for optimizing query performance.
   *
   * @param hint the hint criteria for optimizing query performance
   * @return this builder instance for method chaining (withHint)
   */
  public FindBuilder withHint(final JsObj hint) {
    this.hint = hint;
    return this;
  }

  /**
   * Sets the maximum values for indexed fields in the query.
   *
   * @param max the maximum values for indexed fields in the query
   * @return this builder instance for method chaining (withMax)
   */
  public FindBuilder withMax(final JsObj max) {
    this.max = max;
    return this;
  }

  /**
   * Sets the minimum values for indexed fields in the query.
   *
   * @param min the minimum values for indexed fields in the query
   * @return this builder instance for method chaining (withMin)
   */
  public FindBuilder withMin(final JsObj min) {
    this.min = min;
    return this;
  }

  /**
   * Sets a hint string for the query optimizer to use a specific index.
   *
   * @param hintString the hint string for the query optimizer
   * @return this builder instance for method chaining (withHintString)
   */
  public FindBuilder withHintString(final String hintString) {
    this.hintString = hintString;
    return this;
  }

  /**
   * Sets the number of documents to skip in the query results before returning.
   *
   * @param skip the number of documents to skip
   * @return this builder instance for method chaining (withSkip)
   * @throws IllegalArgumentException if the provided skip output is less than 0
   */
  public FindBuilder withSkip(final int skip) {
    if (skip < 0) {
      throw new IllegalArgumentException("skip is < 0");
    }
    this.skip = skip;
    return this;
  }

  /**
   * Sets the maximum number of documents to return in the query results.
   *
   * @param limit the maximum number of documents to return
   * @return this builder instance for method chaining (withLimit)
   * @throws IllegalArgumentException if the provided limit output is less than 0
   */
  public FindBuilder withLimit(final int limit) {
    if (limit < 0) {
      throw new IllegalArgumentException("limit is < 0");
    }
    this.limit = limit;
    return this;
  }

  /**
   * Sets whether to include the record ID field in the query results.
   *
   * @param showRecordId {@code true} to include the record ID field, {@code false} to exclude it
   * @return this builder instance for method chaining (withShowRecordId)
   */
  public FindBuilder withShowRecordId(final boolean showRecordId) {
    this.showRecordId = showRecordId;
    return this;
  }

  /**
   * Sets whether to return only the keys of the result documents.
   *
   * @param returnKey {@code true} to return only the keys, {@code false} to return full documents
   * @return this builder instance for method chaining (withReturnKey)
   */
  public FindBuilder withReturnKey(final boolean returnKey) {
    this.returnKey = returnKey;
    return this;
  }

  /**
   * Sets a comment associated with the query, which can provide context for the query in the MongoDB logs.
   *
   * @param comment a comment associated with the query
   * @return this builder instance for method chaining (withComment)
   */
  public FindBuilder withComment(final String comment) {
    this.comment = comment;
    return this;
  }

  /**
   * Sets whether to prevent the cursor from timing out automatically.
   *
   * @param noCursorTimeout {@code true} to prevent cursor timeout, {@code false} to allow cursor timeout
   * @return this builder instance for method chaining (withNoCursorTimeout)
   */
  public FindBuilder withNoCursorTimeout(final boolean noCursorTimeout) {
    this.noCursorTimeout = noCursorTimeout;
    return this;
  }

  /**
   * Sets whether to return partial results if some shards are unavailable.
   *
   * @param partial {@code true} to return partial results, {@code false} to require all shards to be available
   * @return this builder instance for method chaining (withPartial)
   */
  public FindBuilder withPartial(final boolean partial) {
    this.partial = partial;
    return this;
  }

  /**
   * Sets the maximum number of documents to retrieve per batch.
   *
   * @param batchSize the maximum number of documents to retrieve per batch
   * @return this builder instance for method chaining (withBatchSize)
   * @throws IllegalArgumentException if the provided batchSize output is less than 0
   */
  public FindBuilder withBatchSize(final int batchSize) {
    if (batchSize < 0) {
      throw new IllegalArgumentException("batchSize is < 0");
    }
    this.batchSize = batchSize;
    return this;
  }

  /**
   * Sets the maximum time, in milliseconds, that the server should allow the query to run.
   *
   * @param maxAwaitTime the maximum time for the server to allow the query to run
   * @param unit         the time unit for the maxAwaitTime output
   * @return this builder instance for method chaining (withMaxAwaitTime)
   * @throws IllegalArgumentException if the provided maxAwaitTime output is less than 0
   */
  public FindBuilder withMaxAwaitTime(final int maxAwaitTime,
                                      final TimeUnit unit) {
    if (maxAwaitTime < 0) {
      throw new IllegalArgumentException("maxAwaitTime is < 0");
    }
    this.maxAwaitTime = requireNonNull(unit).toMillis(maxAwaitTime);
    return this;
  }

  /**
   * Sets the maximum time, in milliseconds, that the server should allow the query to run.
   *
   * @param maxTime the maximum time for the server to allow the query to run
   * @param unit    the time unit for the maxTime output
   * @return this builder instance for method chaining (withMaxTime)
   * @throws IllegalArgumentException if the provided maxTime output is less than 0
   */
  public FindBuilder withMaxTime(final int maxTime,
                                 final TimeUnit unit) {
    if (maxTime < 0) {
      throw new IllegalArgumentException("maxTime is < 0");
    }
    this.maxTime = requireNonNull(unit).toMillis(maxTime);
    return this;
  }

  /**
   * Creates a new {@code FindOptions} instance with the configured query options.
   *
   * @return a new {@code FindOptions} instance with the configured query options
   */
  FindOptions build() {
    return new FindOptions(filter,
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
                           maxTime);
  }
}
