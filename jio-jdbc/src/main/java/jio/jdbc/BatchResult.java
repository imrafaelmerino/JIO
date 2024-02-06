package jio.jdbc;

/**
 * A sealed interface that defines the possible outcomes of a JDBC batch operation. Implementing classes represent
 * different states or results of the batch operation, such as success, partial success, or failure.
 * <p>
 * This interface is part of a sealed hierarchy, and the implementing classes permitted by this interface are:
 * {@link BatchSuccess}, {@link BatchPartialSuccess}, and {@link BatchFailure}.
 * </p>
 */
public sealed interface BatchResult permits BatchFailure, BatchPartialSuccess, BatchSuccess {

}
