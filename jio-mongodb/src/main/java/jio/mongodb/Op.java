package jio.mongodb;

import java.util.concurrent.Executor;
import java.util.function.Supplier;

abstract class Op {


    final CollectionBuilder collection;
    boolean recordEvents;
    Executor executor;

    public Op(CollectionBuilder collection,
              boolean recordEvents
             ) {
        this.collection = collection;
        this.recordEvents = recordEvents;
    }

    <O> Supplier<O> eventWrapper(final Supplier<O> task,
                                 final MongoEvent.OP op
                                ) {
        if (recordEvents)
            return () -> {
                MongoEvent event = new MongoEvent(op);
                try {
                    event.begin();
                    O result = task.get();
                    event.result = MongoEvent.RESULT.SUCCESS.name();
                    return result;
                } catch (Throwable exc) {
                    event.result = MongoEvent.RESULT.FAILURE.name();
                    event.exception = String.format("%s:%s",
                                                    exc.getClass().getName(),
                                                    exc.getMessage()
                                                   );
                    throw exc;
                } finally {
                    event.commit();
                }
            };
        else return task::get;
    }

}
