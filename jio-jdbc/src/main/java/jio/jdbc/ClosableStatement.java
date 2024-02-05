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
import jio.Lambda;


public interface ClosableStatement<Params, Output> extends Function<Params, Lambda<Connection, Output>> {



}
