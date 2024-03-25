package jio;

import java.time.Duration;

/**
 * A record with statistics about retries made so far. Use the {@link #ZERO} constant to represent the initial retry
 * status.
 *
 * @param counter         The retry counter, where 0 is the first try
 * @param cumulativeDelay The cumulative delay incurred from retries in milliseconds. The accumulativeDelay * in a retry
 *                        policy does not include the time spent computing values or performing the actual operations *
 *                        that are being retried. It primarily represents the time spent waiting due to retry delays
 *                        between retry attempts
 * @param previousDelay   The delay of the latest retry attempt. It will always be -1 on the first run
 * @see RetryPolicy
 */
public record RetryStatus(int counter,
                          Duration cumulativeDelay,
                          Duration previousDelay) {

  /**
   * The initial retry status representing no retries.
   */
  public static final RetryStatus ZERO = new RetryStatus(0,
                                                         Duration.ZERO,
                                                         Duration.ZERO);
}
