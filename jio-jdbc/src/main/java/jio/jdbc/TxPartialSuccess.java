package jio.jdbc;

/**
 * Represents a partially successful transaction with information about the rollback to a savepoint.
 *
 * @param savePointName The name of the savepoint to which the transaction was rolled back.
 * @param result        The result of the transaction before the rollback.
 * @param cause         The cause of the rollback to savepoint, including the savepoint and the original exception.
 */
public record TxPartialSuccess(String savePointName,
                               Object result,
                               RollBackToSavePoint cause) implements TxResult {

}
