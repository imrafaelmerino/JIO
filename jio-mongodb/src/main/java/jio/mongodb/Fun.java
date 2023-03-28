package jio.mongodb;


import java.util.function.Supplier;

class Fun {

    public static <O> Supplier<O> jfrEventWrapper(final Supplier<O> task,
                                                  final MongoDBEvent.OP op
                                                 ) {
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
    }


}
