package jio.mongodb;

import static java.util.Objects.requireNonNull;

import com.mongodb.TransactionOptions;
import com.mongodb.client.ClientSession;
import jio.ExceptionFun;
import jio.IO;
import jio.Lambda;

/**
 * Represents a MongoDB transaction that can be applied within a MongoDB client session.
 *
 * <p><b>Note:</b> MongoDB sessions are not multithreading. Only one thread should operate within a MongoDB session at
 * a time to avoid errors like "Only servers in a sharded cluster can start a new transaction at the active transaction
 * number."
 *
 * @param <Input>  the type of the input to the transaction
 * @param <Output> the type of the transaction's output
 */
public final class Tx<Input, Output> implements Lambda<Input, Output> {

  final ClientSessionBuilder sessionBuilder;
  final MongoLambda<Input, Output> mongoLambda;

  final TransactionOptions transactionOptions;

  /**
   * Creates a new instance of `Tx`.
   *
   * @param sessionBuilder     a builder for creating MongoDB client sessions
   * @param mongoLambda        the MongoDB Lambda function representing the transaction
   * @param transactionOptions the transaction options to be used
   */
  Tx(final ClientSessionBuilder sessionBuilder,
     final MongoLambda<Input, Output> mongoLambda,
     final TransactionOptions transactionOptions
  ) {
    this.sessionBuilder = requireNonNull(sessionBuilder);
    this.mongoLambda = requireNonNull(mongoLambda);
    this.transactionOptions = requireNonNull(transactionOptions);
  }

  private static void fillError(MongoOpEvent event,
                                Throwable exc) {
    event.result = MongoOpEvent.RESULT.FAILURE.name();
    event.exception = ExceptionFun.findUltimateCause(exc)
                                  .toString();
  }

  private static void abort(ClientSession session,
                            Throwable exc,
                            MongoOpEvent event) {
    try {
      session.abortTransaction();
      event.end();
      if (event.shouldCommit()) {
        fillError(event,
                  exc);
        event.commit();
      }

    }
    // if the transaction was already either aborted or committed
    catch (IllegalArgumentException e) {
      if (event.shouldCommit()) {
        fillError(event,
                  e);
        event.commit();
      }
    }
  }

  private static void commit(ClientSession session,
                             MongoOpEvent event) {
    try {
      session.commitTransaction();
      event.end();
      event.result = MongoOpEvent.RESULT.SUCCESS.name();
    } catch (IllegalArgumentException exc) {
      fillError(event,
                exc);
    } finally {
      if (event.shouldCommit()) {
        event.commit();
      }
    }
  }

  /**
   * Applies the MongoDB transaction to the given input, executing it within a MongoDB client session.
   *
   * @param input the input to the transaction
   * @return an IO representing the result of the transaction
   */
  @Override
  public IO<Output> apply(final Input input) {
    return IO.resource(sessionBuilder::get,
                       session -> doTx(input,
                                       session));
  }

  //TODO tests!
  private IO<Output> doTx(Input input,
                          ClientSession session) {

    return IO.lazy(() -> {
      var event = new MongoOpEvent(MongoOpEvent.OP.TX);
      event.begin();
      session.startTransaction(transactionOptions);
      return event;
    })
             .then(event -> mongoLambda.apply(session,
                                              input)
                                       .peekSuccess(_ -> commit(session,
                                                                event))
                                       .peekFailure(exc -> abort(session,
                                                                 exc,
                                                                 event)));
  }
}
