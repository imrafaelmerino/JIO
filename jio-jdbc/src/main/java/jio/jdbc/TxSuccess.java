package jio.jdbc;

/**
 * Represents a successful transaction with a specific output.
 *
 * @param output   the output the transaction returns when ends successfully
 * @param <Output> The type of the transaction output.
 */
public record TxSuccess<Output>(Output output) implements TxResult {

}
