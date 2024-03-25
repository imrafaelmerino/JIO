package jio.mongodb;

import com.mongodb.MongoException;
import com.mongodb.MongoSocketReadTimeoutException;
import com.mongodb.MongoTimeoutException;
import java.util.function.IntPredicate;
import java.util.function.Predicate;
import jio.ExceptionFun;

/**
 * The {@code MongoFun} class provides utility functions for handling exceptions related to MongoDB operations. It
 * includes predicates to identify specific MongoDB exceptions, such as read timeouts and connection timeouts, as well
 * as a Predicate for capturing and handling MongoDB exceptions.
 *
 * <p>The class contains the following methods and predicates:</p>
 *
 * <ul>
 * <li>{@link #IS_READ_TIMEOUT}: A predicate to check if the given throwable is an instance of
 * {@link MongoSocketReadTimeoutException}, indicating a read timeout exception.</li>
 * <li>{@link #IS_CONNECTION_TIMEOUT}: A predicate to check if the given throwable is an instance of
 * {@link MongoTimeoutException}, indicating a connection timeout exception.</li>
 * <li>{@link #findMongoExceptionRecursively(IntPredicate)}: A predicate to check if the given throwable or its causes
 * contains a specific {@link MongoException} with a specified error code.</li>
 * </ul>
 *
 *
 * <p>The class is final and cannot be instantiated, as it only provides static utility methods.</p>
 *
 * @see MongoSocketReadTimeoutException
 * @see MongoTimeoutException
 * @see MongoException
 */
public final class MongoExceptionFun {

  private MongoExceptionFun() {
  }

  /**
   * Predicate to check if the given throwable or its causes contains an instance of
   * {@link MongoSocketReadTimeoutException}. This predicate is used to identify exceptions related to read timeouts
   * during MongoDB operations.
   *
   * @see MongoSocketReadTimeoutException
   */
  public static final Predicate<Throwable> IS_READ_TIMEOUT =
      exc -> ExceptionFun.findCauseRecursively(cause -> cause instanceof MongoSocketReadTimeoutException)
                         .apply(exc)
                         .isPresent();

  /**
   * Predicate to check if the given throwable or its causes contains an instance of {@link MongoTimeoutException}. This
   * predicate is used to identify exceptions related to connection timeouts during MongoDB operations.
   *
   * @see MongoTimeoutException
   */
  public static final Predicate<Throwable> IS_CONNECTION_TIMEOUT =
      exc -> ExceptionFun.findCauseRecursively(cause -> cause instanceof MongoTimeoutException)
                         .apply(exc)
                         .isPresent();

  /**
   * Predicate to check if the given throwable or its causes contains a specific {@link MongoException} with a specified
   * error code. This predicate allows you to identify MongoDB exceptions based on their error codes.
   *
   * <p>Example usage:</p>
   * <pre>
   * {@code
   *  // Create a Predicate that returns true for a specific error code (e.g., Duplicate Key Error - Code 11000)
   *  IntPredicate isDuplicatedKeyError = code -> code == 11000;
   *  Predicate<Throwable> isDuplicatedKey = findMongoExceptionRecursively(isDuplicatedKeyError);
   *  }
   * </pre>
   *
   * @param codePredicate The predicate to test the error code of the {@link MongoException}.
   * @return A predicate that checks if the error code of the {@link MongoException} satisfies the provided predicate.
   * @see MongoException
   */
  public static Predicate<Throwable> findMongoExceptionRecursively(IntPredicate codePredicate) {
    return e -> ExceptionFun.findCauseRecursively(cause -> cause instanceof MongoException me
                                                           && codePredicate.test(me.getCode()))
                            .apply(e)
                            .isPresent();

  }

}
