package jio.test.junit;

import jdk.jfr.consumer.RecordedThread;

final class DebuggerUtils {

    static String getThreadName(final RecordedThread thread) {
        return thread.isVirtual() ? "virtual-%s".formatted(thread.getJavaThreadId())
                : thread.getJavaName();
    }
}
