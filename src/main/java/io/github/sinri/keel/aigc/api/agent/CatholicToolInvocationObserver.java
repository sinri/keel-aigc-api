package io.github.sinri.keel.aigc.api.agent;

/**
 * Receives audit events around execution of a {@link CatholicToolInvocationHandler}.
 * <p>
 * Observer failures are isolated and never alter the tool invocation result.
 */
public interface CatholicToolInvocationObserver {
    default void onStarted(CatholicToolInvocationObservation observation) {
    }

    default void onSucceeded(
        CatholicToolInvocationObservation observation, String result, long elapsedMillis
    ) {
    }

    default void onFailed(
        CatholicToolInvocationObservation observation, Throwable cause, long elapsedMillis
    ) {
    }

    static CatholicToolInvocationObserver noop() {
        return NoopCatholicToolInvocationObserver.INSTANCE;
    }

    enum NoopCatholicToolInvocationObserver implements CatholicToolInvocationObserver {
        INSTANCE
    }
}
