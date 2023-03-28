package jio.mongodb;

import jsonvalues.JsObj;

import java.util.concurrent.TimeUnit;

import static java.util.Objects.requireNonNull;


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


    public FindOptionsBuilder filter(final JsObj filter) {
        this.filter = filter;
        return this;
    }

    public FindOptionsBuilder sort(final JsObj sort) {
        this.sort = sort;
        return this;
    }

    public FindOptionsBuilder projection(final JsObj projection) {
        this.projection = projection;
        return this;
    }

    public FindOptionsBuilder hint(final JsObj hint) {
        this.hint = hint;
        return this;
    }

    public FindOptionsBuilder max(final JsObj max) {
        this.max = max;
        return this;
    }

    public FindOptionsBuilder min(final JsObj min) {
        this.min = min;
        return this;
    }

    public FindOptionsBuilder hintString(final String hintString) {
        this.hintString = hintString;
        return this;
    }

    public FindOptionsBuilder skip(final int skip) {
        if (skip < 0) throw new IllegalArgumentException("skip is < 0");
        this.skip = skip;
        return this;
    }

    public FindOptionsBuilder limit(final int limit) {
        if (limit < 0) throw new IllegalArgumentException("limit is < 0");
        this.limit = limit;
        return this;
    }

    public FindOptionsBuilder showRecordId(final boolean showRecordId) {
        this.showRecordId = showRecordId;
        return this;
    }

    public FindOptionsBuilder returnKey(final boolean returnKey) {
        this.returnKey = returnKey;
        return this;
    }

    public FindOptionsBuilder comment(final String comment) {
        this.comment = comment;
        return this;
    }

    public FindOptionsBuilder noCursorTimeout(final boolean noCursorTimeout) {
        this.noCursorTimeout = noCursorTimeout;
        return this;
    }

    public FindOptionsBuilder partial(final boolean partial) {
        this.partial = partial;
        return this;
    }

    public FindOptionsBuilder batchSize(final int batchSize) {
        if (batchSize < 0) throw new IllegalArgumentException("batchSize is < 0");
        this.batchSize = batchSize;
        return this;
    }

    public FindOptionsBuilder maxAwaitTime(final int maxAwaitTime,
                                           final TimeUnit unit
                                          ) {
        if (maxAwaitTime < 0) throw new IllegalArgumentException("maxAwaitTime is < 0");
        this.maxAwaitTime = requireNonNull(unit).toMillis(maxAwaitTime);
        return this;
    }

    public FindOptionsBuilder maxTime(final int maxTime,
                                      final TimeUnit unit
                                     ) {
        if (maxTime < 0) throw new IllegalArgumentException("maxTime is < 0");
        this.maxTime = requireNonNull(unit).toMillis(maxTime);
        return this;
    }

    public FindOptions create() {

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
                               maxTime
        );
    }
}