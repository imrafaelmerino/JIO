package jio.mongodb;

import com.mongodb.client.ClientSession;
import jio.BiLambda;
import jio.Lambda;

import java.util.Objects;
import java.util.function.Function;

/**
 * Represents a function that takes an input and produces an IO effect within a MongoDB client session. Using
 * transactions is optional, and you can leave them out by using the {@link #standalone()} method to create a Lambda
 * that produces effects independently of any transaction.
 *
 * @param <Input>  the type of the input
 * @param <Output> the type of the effect
 */
public interface MongoLambda<Input, Output> extends BiLambda<ClientSession, Input, Output> {

  /**
   * Creates a Lambda from this MongoLambda that is not associated with any transaction (null session).
   *
   * @return a Lambda that produces effects independently of any transaction.
   */
  default Lambda<Input, Output> standalone() {
    return input -> apply(null,
                          input);
  }

  /**
   * Chains this MongoLambda with another, producing a new MongoLambda. The resulting MongoLambda is executed within the
   * same MongoDB client session, and the output of this operation is passed as input to the next one.
   *
   * @param <B>   the type of the output produced by the other MongoLambda
   * @param other the next MongoDB operation to chain with
   * @return a new MongoLambda that represents the sequential execution of this and the other operation.
   */
  default <B> MongoLambda<Input, B> then(final MongoLambda<Output, B> other) {
    Objects.requireNonNull(other);
    return (session,
            input) -> this.apply(session,
                                 input)
                          .then(n -> other.apply(session,
                                                 n));
  }

  /**
   * Chains this MongoLambda with a non-transactional Lambda, producing a new MongoLambda. The resulting MongoLambda is
   * executed within the same MongoDB client session, and the output of this operation is passed as input to the
   * Lambda.
   *
   * @param <B>   the type of the output produced by the non-transactional Lambda
   * @param other the non-transactional Lambda to chain with
   * @return a new MongoLambda that represents the sequential execution of this and the non-transactional Lambda.
   */
  default <B> MongoLambda<Input, B> then(final Lambda<Output, B> other) {
    Objects.requireNonNull(other);
    return (session,
            input) -> this.apply(session,
                                 input)
                          .then(other);
  }

  /**
   * Maps the output of this MongoLambda using the given function, producing a new MongoLambda. The resulting
   * MongoLambda is executed within the same MongoDB client session and applies the function to the output of this
   * operation.
   *
   * @param <C> the type of the output after mapping
   * @param map the function to map the output of this MongoLambda
   * @return a new MongoLambda that represents the application of the mapping function to the output of this operation.
   */
  default <C> MongoLambda<Input, C> map(final Function<Output, C> map) {
    Objects.requireNonNull(map);
    return (session,
            input) -> this.apply(session,
                                 input)
                          .map(map);
  }

}
