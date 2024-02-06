package jio.jdbc;

/**
 * Represents a successful transaction with a specific output.
 *
 * @param <Output> The type of the transaction output.
 */
public  record TxSuccess<Output>(Output output) implements TxResult {
}
