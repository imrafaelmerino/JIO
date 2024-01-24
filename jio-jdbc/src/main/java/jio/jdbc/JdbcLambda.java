/**
 * A functional interface representing a JDBC Lambda. The resulting {@link jio.BiLambda} can be used in JDBC operations
 * to perform any database interaction.
 *
 * @param <I> The type of the input parameter.
 * @param <O> The type of the output.
 */
package jio.jdbc;

import jio.Lambda;

import java.util.function.Function;

/**
 * Represents a functional interface for a JDBC Lambda. The resulting {@link jio.BiLambda} can be employed in JDBC
 * operations to execute various database interactions.
 *
 * @param <Params> The type of the input parameter.
 * @param <Output> The type of the output.
 */
public interface JdbcLambda<Params, Output> extends Function<DatasourceBuilder, Lambda<Params, Output>> {

}
