package jio.jdbc;

import java.util.concurrent.Callable;


class JfrEventDecorator {

    private JfrEventDecorator() {
    }

    /**
     * Wraps the provided operation with JFR events if enabled.
     *
     * @param op        The operation to wrap.
     * @param sql       The SQL statement associated with the operation.
     * @param enableJFR Indicates whether to enable JFR events.
     * @param <O>       The type of the operation result.
     * @return The result of the operation.
     * @throws Exception If an exception occurs during the operation.
     */
    static <O> O decorate(Callable<O> op, String sql, boolean enableJFR) throws Exception {
        StmEvent event = null;
        if (enableJFR) {
            event = new StmEvent(sql);
            event.begin();
        }
        try {
            var result = op.call();
            if (enableJFR) event.result = StmEvent.RESULT.SUCCESS.name();
            return result;

        } catch (Exception e) {
            if (enableJFR) {
                var cause = findUltimateCause(e);
                event.result = StmEvent.RESULT.FAILURE.name();
                event.exception = String.format("%s:%s",
                                                cause.getClass().getName(),
                                                cause.getMessage()
                                               );
            }
            throw e;
        } finally {
            if (enableJFR) event.commit();

        }
    }

    private static Throwable findUltimateCause(Throwable exception) {
        Throwable ultimateCause = exception;

        // Iterate through the exception chain until the ultimate cause is found
        while (ultimateCause.getCause() != null) {
            ultimateCause = ultimateCause.getCause();
        }

        return ultimateCause;
    }
}
