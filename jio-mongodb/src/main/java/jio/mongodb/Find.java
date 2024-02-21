package jio.mongodb;

import static java.util.Objects.requireNonNull;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static jio.mongodb.Converters.toBson;
import static jio.mongodb.MongoOpEvent.OP.FIND;

import com.mongodb.client.ClientSession;
import com.mongodb.client.FindIterable;
import java.util.Objects;
import java.util.function.Supplier;
import jio.IO;
import jsonvalues.JsObj;

abstract class Find extends Op {

  Find(CollectionBuilder collection,
       boolean recordEvents) {
    super(collection,
          recordEvents);
  }

  IO<FindIterable<JsObj>> query(final ClientSession session,
                                final FindBuilder builder
  ) {
    Objects.requireNonNull(builder);
    FindOptions options = builder.build();
    Supplier<FindIterable<JsObj>> supplier = decorateWithEvent(() -> {
      var hint = options.hint() != null ? toBson(options.hint()) : null;
      var max = options.max() != null ? toBson(options.max()) : null;
      var projection = options.projection() != null ? toBson(options.projection()) : null;
      var sort = options.sort() != null ? toBson(options.sort()) : null;
      var min = options.min() != null ? toBson(options.min()) : null;
      var collection = requireNonNull(this.collection.get());
      var iter = session == null ? collection.find(toBson(options.filter())) : collection.find(session,
                                                                                               toBson(options.filter()));
      return iter
                 .hint(hint)
                 .max(max)
                 .projection(projection)
                 .sort(sort)
                 .min(min)
                 .batchSize(options.batchSize())
                 .comment(options.comment())
                 .hintString(options.hintString())
                 .limit(options.limit())
                 .skip(options.skip())
                 .maxTime(options.maxTime(),
                          MILLISECONDS
                 )
                 .maxAwaitTime(options.maxAwaitTime(),
                               MILLISECONDS
                 )
                 .partial(options.partial())
                 .showRecordId(options.showRecordId())
                 .noCursorTimeout(options.noCursorTimeout());
    },
                                                               FIND
    );
    return IO.lazy(supplier);
  }

}
