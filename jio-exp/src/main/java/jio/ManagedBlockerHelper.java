package jio;


import java.util.concurrent.Callable;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ForkJoinPool;
import java.util.function.Supplier;

/**
 * A Java utility class that simplifies the integration of blocking Runnable and Suppliers with the common Java
 * fork/join thread pool. This class is a variant of the one that is described at
 * <a
 * href="http://stackoverflow.com/questions/37512662/is-there-anything-wrong">http://stackoverflow.com/questions/37512662/is-there-anything-wrong</a>
 * -with-using-i-o-managedblocker-in-java8-parallelstream.
 */
final class ManagedBlockerHelper {


    /**
     * This method enables blocking Suppliers to be used efficiently with the common Java fork/join thread pool.
     */
    static <T> T computeSupplier(final Supplier<T> supplier) {
        var managedBlocker = new ManagedBlockerSupplier<>(supplier);
        try {
            ForkJoinPool.managedBlock(managedBlocker);
            return managedBlocker.getResult();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new CompletionException(e);
        }

    }

    /**
     * This method enables blocking Callable to be used efficiently with the common Java fork/join thread pool.
     */
    static <T> T computeTask(final Callable<T> callable) {
        var managedBlocker = new TaskManagedBlockerTask<>(callable);
        try {
            ForkJoinPool.managedBlock(managedBlocker);
            return managedBlocker.getResult();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new CompletionException(e);
        }

    }


    /**
     * This class is an adapter that enables a blocking Supplier to be used efficient with the common fork/join thread
     * pool.
     */
    private static class ManagedBlockerSupplier<T>
            implements ForkJoinPool.ManagedBlocker {
        /**
         * The blocking task.
         */
        private final Supplier<T> task;

        /**
         * Result returned when the supplier is done.
         */
        private T result;

        /**
         * Keeps track of whether the blocking supplier is done.
         */
        private boolean isDone = false;

        /**
         * Constructor initializes the field.
         */
        private ManagedBlockerSupplier(final Supplier<T> supplier) {
            task = supplier;
        }

        /**
         * Calls the blocking Supplier's get() method.
         */
        @Override
        public boolean block() {
            result = task.get();
            isDone = true;
            return true;
        }

        /**
         * Returns true if blocking supplier has finished, else false.
         */
        @Override
        public boolean isReleasable() {
            return isDone;
        }

        /**
         * Returns the result obtained from the blocking supplier.
         */
        T getResult() {
            return result;
        }
    }

    /**
     * This class is an adapter that enables a blocking Callable to be used efficient with the common fork/join thread
     * pool.
     */
    private static class TaskManagedBlockerTask<T>
            implements ForkJoinPool.ManagedBlocker {
        /**
         * The blocking task.
         */
        private final Callable<T> task;

        /**
         * Result returned when the supplier is done.
         */
        private T result;

        /**
         * Keeps track of whether the blocking supplier is done.
         */
        private boolean isDone = false;

        /**
         * Constructor initializes the field.
         */
        private TaskManagedBlockerTask(final Callable<T> supplier) {
            task = supplier;
        }

        /**
         * Calls the blocking Supplier's get() method.
         */
        @Override
        public boolean block() {
            try {
                result = task.call();
                isDone = true;
                return true;
            } catch (Exception e) {
                throw new CompletionException(e);
            }
        }

        /**
         * Returns true if blocking supplier has finished, else false.
         */
        @Override
        public boolean isReleasable() {
            return isDone;
        }

        /**
         * Returns the result obtained from the blocking supplier.
         */
        T getResult() {
            return result;
        }
    }
}
