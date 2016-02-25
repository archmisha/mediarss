package rss.test.util;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

/**
 * User: dikmanm
 * Date: 13/02/2015 00:49
 */
public class WaitUtil {

    public static final int TIMEOUT_3_MIN = (int) TimeUnit.MINUTES.toMillis(3);
    public static final int SLEEP_500_MILLIS = 500;

    /**
     * Wait until the given runnable does not throw an exception when called or until default timeout
     */
    public static void waitFor(final Runnable runnable) {
        waitFor(120000, 100, new Callable<Object>() {
            @Override
            public Object call() {
                runnable.run();
                return null;
            }
        });
    }

    public static void waitFor(int timeout, int sleep, final Runnable runnable) {
        waitFor(timeout, sleep, new Callable<Object>() {
            @Override
            public Object call() {
                runnable.run();
                return null;
            }
        });
    }

    public static <T> T waitFor(Callable<T> callable, Predicate<T> stopWaitingPredicate) {
        return waitFor(120000, 100, callable, stopWaitingPredicate);
    }

    public static <T> T waitFor(int timeout, int sleep, Callable<T> callable) {
        return waitFor(timeout, sleep, callable, Predicates.<T>alwaysTrue());
    }

    public static <T> T waitFor(Callable<T> callable) {
        return waitFor(120000, 100, callable, Predicates.<T>alwaysTrue());
    }

    public static <T> T waitFor(int timeout, int sleep, Callable<T> callable, Predicate<T> stopWaitingPredicate) {
        Throwable caught = null;
        long start = System.currentTimeMillis();

        while (System.currentTimeMillis() - start < timeout) {
            try {
                T result = callable.call();

                if (stopWaitingPredicate.apply(result)) {
                    return result;
                }
            } catch (Throwable e) {
                caught = e;
            }
            //sleep added to not overload CPU
            sleep(sleep);
        }

        if (caught != null) {
            throw new RuntimeTimeoutException("The operation timed out with errors: " + caught.getMessage(), caught);
        } else {
            throw new RuntimeTimeoutException("The operation timed out before the condition was fulfilled");
        }
    }

    public static void sleep(int i) {
        try {
            Thread.sleep(i);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private static class RuntimeTimeoutException extends RuntimeException {

        public RuntimeTimeoutException(String message) {
            super(message);
        }

        private RuntimeTimeoutException(String message, Throwable cause) {
            super(message, cause);
        }

    }
}
