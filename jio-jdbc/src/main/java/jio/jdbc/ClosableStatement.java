/**
 * A functional interface representing a JDBC Lambda. The resulting {@link jio.BiLambda} can be used in JDBC operations
 * to perform any database interaction.
 *
 * @param <I> The type of the input parameter.
 * @param <O> The type of the output.
 */
package jio.jdbc;

import java.sql.Connection;
import java.util.function.Function;
import jio.BiLambda;
import jio.Lambda;

/**
 * Represents a lambda that takes two arguments as inputs (some parameters and a connection) and produces an IO effect
 * that executes a JDBC statement, producing a result of type {@code Output}.
 *
 * @param <Params> The type of the input parameters.
 * @param <Output> The type of the result produced by executing the JDBC statement.
 */
public interface ClosableStatement<Params, Output> extends BiLambda<Params, Connection, Output> {

  /**
   * Combines this ClosableStatement with another ClosableStatement, forming a chain of execution. The resulting
   * ClosableStatement will execute this statement and then execute the other statement using the output of this
   * statement as input for the other.
   *
   * @param other         The other ClosableStatement to be executed after this one.
   * @param <OtherOutput> The type of the output of the other ClosableStatement.
   * @return A new ClosableStatement representing the combined execution.
   */
  default <OtherOutput> ClosableStatement<Params, OtherOutput> then(ClosableStatement<Output, OtherOutput> other) {
    return (params,
            connection) -> this.apply(params,
                                      connection)
                               .then(output -> other.apply(output,
                                                           connection));
  }

  /**
   * Combines this ClosableStatement with a Lambda, forming a chain of execution. The resulting ClosableStatement will
   * execute this statement and then apply the Lambda to the output of this statement.
   *
   * @param other         The Lambda to be applied after this ClosableStatement.
   * @param <OtherOutput> The type of the output of the Lambda.
   * @return A new ClosableStatement representing the combined execution.
   */
  default <OtherOutput> ClosableStatement<Params, OtherOutput> then(Lambda<Output, OtherOutput> other) {
    return (params,
            connection) -> this.apply(params,
                                      connection)
                               .then(other);
  }

  /**
   * Maps the success output of this ClosableStatement using the provided Function.
   *
   * @param successMap    The Function to map the success output.
   * @param <OtherOutput> The type of the mapped output.
   * @return A new ClosableStatement with the mapped success output.
   */
  default <OtherOutput> ClosableStatement<Params, OtherOutput> map(Function<Output, OtherOutput> successMap) {
    return (params,
            connection) -> this.apply(params,
                                      connection)
                               .map(successMap);
  }

  /**
   * Maps the failure of this ClosableStatement using the provided Function.
   *
   * @param failureMap The Function to map the failure.
   * @return A new ClosableStatement with the mapped failure.
   */
  default ClosableStatement<Params, Output> mapFailure(Function<Exception, Exception> failureMap) {
    return (params,
            connection) -> this.apply(params,
                                      connection)
                               .mapFailure(failureMap);
  }

}
