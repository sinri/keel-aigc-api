package io.github.sinri.keel.aigc.api.llm.catholic.observation;

import java.util.Map;

/**
 * Observes provider-native LLM HTTP input and output.
 * <p>
 * Implementations must return quickly. Provider implementations isolate observer failures, so an
 * observer cannot change the result of an LLM call.
 */
public interface CatholicLLMObserver {
    default void onRequest(
        String exchangeId, String provider, String endpoint, Map<String, String> headers,
        String body, boolean stream
    ) {
    }

    default void onResponseStarted(
        String exchangeId, String provider, int statusCode, Map<String, String> headers,
        long elapsedMillis
    ) {
    }

    default void onResponse(
        String exchangeId, String provider, int statusCode, Map<String, String> headers,
        String body, long elapsedMillis
    ) {
    }

    default void onStreamEvent(
        String exchangeId, String provider, long sequence, String rawEvent, long elapsedMillis
    ) {
    }

    default void onFailure(
        String exchangeId, String provider, CatholicLLMObservationStage stage,
        Throwable cause, long elapsedMillis
    ) {
    }

    static CatholicLLMObserver noop() {
        return NoopCatholicLLMObserver.INSTANCE;
    }

    enum NoopCatholicLLMObserver implements CatholicLLMObserver {
        INSTANCE
    }
}
