package jio;

import java.time.Duration;

/**
 * Record with stats about retries made so far.
 * cumulativeDelay: Delay incurred so far from retries in milliseconds
 * previousDelay: Latest attempt's delay. Will always be -1 on first run
 * counter: retries counter, where 0 is the first try
 */

public record RetryStatus(int counter,
                          Duration cumulativeDelay,
                          Duration previousDelay
) {

    /**
     * the initial status
     */
    public static final RetryStatus ZERO = new RetryStatus(0, Duration.ZERO, Duration.ZERO);
}
