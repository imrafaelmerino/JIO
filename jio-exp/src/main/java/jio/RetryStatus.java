package jio;

import java.time.Duration;

/**
 * A record with statistics about retries made so far.
 *
 * <p>The record includes the following fields:
 * - {@code counter}: The retry counter, where 0 is the first try.
 * - {@code cumulativeDelay}: The cumulative delay incurred from retries in milliseconds.
 * - {@code previousDelay}: The delay of the latest retry attempt. It will always be -1 on the first run.
 *
 * <p>Use the {@link #ZERO} constant to represent the initial retry status.
 *
 * @see RetryPolicy
 */

public record RetryStatus(int counter,
                          Duration cumulativeDelay,
                          Duration previousDelay
) {

    /**
     * The initial retry status representing no retries.
     */
    public static final RetryStatus ZERO = new RetryStatus(0, Duration.ZERO, Duration.ZERO);
}
