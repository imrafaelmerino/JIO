package jio.jdbc;

/**
 * Represents the result of a transaction. It is a sealed interface permitting two possible outcomes: {@link TxSuccess}
 * and {@link TxPartialSuccess}.
 */
public sealed interface TxResult permits TxSuccess, TxPartialSuccess {

}
