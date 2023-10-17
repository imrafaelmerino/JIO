package jio.mongodb;

import java.util.concurrent.Executor;
import java.util.function.Supplier;

abstract class Op {

    final CollectionSupplier collection;
    boolean recordEvents;
    Executor executor;

    public Op(CollectionSupplier collection,
              boolean recordEvents
             ) {
        this.collection = collection;
        this.recordEvents = recordEvents;
    }

    <O> Supplier<O> jfrEventWrapper(final Supplier<O> task,
                                    final MongoDBEvent.OP op
                                   ) {
        if (recordEvents)
            return () -> {
                MongoDBEvent event = new MongoDBEvent(op);
                try {
                    event.begin();
                    O result = task.get();
                    event.result = MongoDBEvent.RESULT.SUCCESS.name();
                    return result;
                } catch (Throwable exc) {
                    event.result = MongoDBEvent.RESULT.FAILURE.name();
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
