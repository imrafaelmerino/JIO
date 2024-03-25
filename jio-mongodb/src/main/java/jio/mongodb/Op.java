package jio.mongodb;

import java.util.function.Supplier;
import jio.ExceptionFun;

abstract class Op {

  final CollectionBuilder collection;
  boolean recordEvents;

  public Op(CollectionBuilder collection,
            boolean recordEvents
  ) {
    this.collection = collection;
    this.recordEvents = recordEvents;
  }

  <Output> Supplier<Output> decorateWithEvent(final Supplier<Output> task,
                                              final MongoOpEvent.OP op
  ) {
    if (recordEvents) {
      return () -> {
        MongoOpEvent event = new MongoOpEvent(op);
        try {
          event.begin();
          Output result = task.get();
          event.end();
          if (event.shouldCommit()) {
            event.result = MongoOpEvent.RESULT.SUCCESS.name();
            event.commit();
          }

          return result;
        } catch (Throwable exc) {
          if (event.shouldCommit()) {
            var cause = ExceptionFun.findUltimateCause(exc);
            event.result = MongoOpEvent.RESULT.FAILURE.name();
            event.exception = cause.toString();
            event.commit();
          }

          throw exc;
        }
      };
    } else {
      return task;
    }
  }

}
