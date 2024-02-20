package jio.jdbc;

import java.util.function.Function;

/**
 * A functional interface representing a function that transforms an input of type {@code I} into a
 * {@link StatementSetter}. This interface extends {@link java.util.function.Function}.
 * <p>
 * This interface enables the creation of parameter-setting operations for a {@link java.sql.PreparedStatement} in a
 * functional programming style. Implementations should define how to convert an input object of type {@code I} into a
 * {@code PrStmSetter}.
 *
 * @param <Params> The type of the input object.
 * @see StatementSetter
 * @see java.util.function.Function
 */
public interface ParamsSetter<Params> extends Function<Params, StatementSetter> {

}
