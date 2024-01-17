package jio.mongodb;

import com.mongodb.TransactionOptions;
import com.mongodb.client.ClientSession;
import jio.IO;
import jio.Lambda;

import static java.util.Objects.requireNonNull;

/**
 * Represents a MongoDB transaction that can be applied within a MongoDB client session.
 *
 * <p><b>Note:</b> MongoDB sessions are not multi-threaded. Only one thread should operate within a MongoDB session at
 * a time to avoid errors like "Only servers in a sharded cluster can start a new transaction at the active transaction
 * number."
 *
 * @param <I> the type of the input to the transaction
 * @param <O> the type of the transaction's output
 */
public final class Tx<I, O> implements Lambda<I, O> {

    final ClientSessionBuilder sessionBuilder;
    final MongoLambda<I, O> mongoLambda;

    final TransactionOptions transactionOptions;

    /**
     * Creates a new instance of `Tx`.
     *
     * @param sessionBuilder     a builder for creating MongoDB client sessions
     * @param mongoLambda        the MongoDB Lambda function representing the transaction
     * @param transactionOptions the transaction options to be used
     */
    Tx(final ClientSessionBuilder sessionBuilder,
       final MongoLambda<I, O> mongoLambda,
       final TransactionOptions transactionOptions
      ) {
        this.sessionBuilder = requireNonNull(sessionBuilder);
        this.mongoLambda = requireNonNull(mongoLambda);
        this.transactionOptions = requireNonNull(transactionOptions);
    }

    private static void fillError(MongoEvent event, Throwable exc) {
        event.result = MongoEvent.RESULT.FAILURE.name();
        event.exception = "%s:%s".formatted(exc.getClass().getName(),
                                            exc.getMessage()
                                           );
    }

    private static void abort(ClientSession session, Throwable exc, MongoEvent event) {
        try {
            session.abortTransaction();
            fillError(event, exc);
        }
        // if the transaction was already either aborted or committed
        catch (IllegalArgumentException e) {
            fillError(event, e);
        } finally {
            event.commit();
        }
    }

    private static void commit(ClientSession session, MongoEvent event) {
        try (session) {
            session.commitTransaction();
            event.result = MongoEvent.RESULT.SUCCESS.name();
        } catch (IllegalArgumentException exc) {
            fillError(event, exc);
        } finally {
            event.commit();
        }
    }

    /**
     * Applies the MongoDB transaction to the given input, executing it within a MongoDB client session.
     *
     * @param input the input to the transaction
     * @return an IO representing the result of the transaction
     */
    @Override
    public IO<O> apply(final I input) {
        return
                IO.resource(sessionBuilder::get,
                            session -> doTx(input, session));
    }

    //TODO tests!
    private IO<O> doTx(I input, ClientSession session) {


        return IO.lazy(() -> {
            var event = new MongoEvent(MongoEvent.OP.TX);
            event.begin();
            session.startTransaction(transactionOptions);
            return event;
        }).then(event -> mongoLambda.apply(session, input)
                                    .peekSuccess(it -> commit(session, event))
                                    .peekFailure(exc -> abort(session, exc, event)));
    }
}