package jio.mongodb;

import jsonvalues.JsObj;

import java.util.Objects;

import static java.util.Objects.requireNonNull;

public final class FindOptions {


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


    public final JsObj filter;

    public final JsObj sort;

    public final JsObj projection;

    public final JsObj hint;

    public final JsObj max;

    public final JsObj min;

    public final String hintString;

    public final int skip;

    public final int limit;

    public final boolean showRecordId;

    public final boolean returnKey;

    public final String comment;

    public final boolean noCursorTimeout;

    public final boolean partial;

    public final int batchSize;

    public final long maxAwaitTime;

    public final long maxTime;

    public static FindOptions ofFilter(final JsObj filter) {
        return new FindOptionsBuilder().filter(requireNonNull(filter))
                                       .create();
    }

    public static FindOptions ofFilter(final JsObj filter,
                                       final JsObj projection
                                      ) {
        return new FindOptionsBuilder().filter(requireNonNull(filter))
                                       .projection(requireNonNull(projection))
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

    public static FindOptions ofFilter(final JsObj filter,
                                       final JsObj projection,
                                       final JsObj sort
                                      ) {
        return new FindOptionsBuilder().filter(requireNonNull(filter))
                                       .projection(requireNonNull(projection))
                                       .sort(requireNonNull(sort))
                                       .create();
    }


}
