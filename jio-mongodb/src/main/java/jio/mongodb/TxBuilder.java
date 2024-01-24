package jio.mongodb;

import com.mongodb.TransactionOptions;

import java.util.Objects;

/**
 * A builder for creating transactions in a MongoDB client session.
 */
public final class TxBuilder {

  final ClientSessionBuilder sessionBuilder;

  private final TransactionOptions DEFAULT_TX_OPTIONS = TransactionOptions.builder()
                                                                          .build();
  TransactionOptions transactionOptions = DEFAULT_TX_OPTIONS;

  /**
   * Creates a new instance of `TxBuilder`.
   *
   * @param builder a supplier for providing MongoDB client sessions
   */
  TxBuilder(final ClientSessionBuilder builder) {
    this.sessionBuilder = Objects.requireNonNull(builder);
  }

  /**
   * Creates a new `TxBuilder` with the provided session supplier.
   *
   * @param sessionBuilder a supplier for providing MongoDB client sessions
   * @return a new `TxBuilder` instance
   */
  public static TxBuilder of(final ClientSessionBuilder sessionBuilder) {
    return new TxBuilder(sessionBuilder);
  }

  /**
   * Sets the transaction options for the transactions created with this builder.
   *
   * @param transactionOptions the transaction options to be used
   * @return this `TxBuilder` instance with the specified transaction options
   */
  public TxBuilder withTxOptions(final TransactionOptions transactionOptions) {
    this.transactionOptions = Objects.requireNonNull(transactionOptions);
    return this;
  }

  /**
   * Builds a transaction with the specified MongoDB Lambda function and transaction options.
   *
   * @param <Input>     the type of the input to the transaction
   * @param <Output>    the type of the transaction's output
   * @param mongoLambda the Lambda function representing the transaction
   * @return a new `Tx` instance representing the transaction
   */
  public <Input, Output> Tx<Input, Output> build(final MongoLambda<Input, Output> mongoLambda) {
    return new Tx<>(sessionBuilder,
                    mongoLambda,
                    transactionOptions
    );
  }
}
