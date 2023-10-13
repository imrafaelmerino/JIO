package jio.test.junit;

import jdk.jfr.consumer.RecordedThread;

final class DebuggerUtils {

    static String getThreadName(final RecordedThread thread) {
        if(thread==null) return "not recorded";
        String javaName = thread.getJavaName();
        return javaName.isEmpty() ? "virtual-%s".formatted(thread.getOSThreadId()) : javaName;
    }
}
