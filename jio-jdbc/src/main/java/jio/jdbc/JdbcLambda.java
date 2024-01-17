/**
 * A functional interface representing a JDBC Lambda. The resulting {@link jio.BiLambda} can be used in JDBC operations
 * to perform any database interaction.
 *
 * @param <I> The type of the input parameter
 * @param <O> The type of the output
 */
package jio.jdbc;

import jio.BiLambda;

import java.time.Duration;
import java.util.function.Function;

public interface JdbcLambda<I, O> extends Function<DatasourceBuilder, BiLambda<Duration, I, O>> {

}
