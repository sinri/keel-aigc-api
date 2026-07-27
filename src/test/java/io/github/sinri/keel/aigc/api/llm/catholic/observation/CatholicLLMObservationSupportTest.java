package io.github.sinri.keel.aigc.api.llm.catholic.observation;

import io.github.sinri.keel.aigc.api.internal.catholic.observation.CatholicLLMObservationSupport;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class CatholicLLMObservationSupportTest {
    @Test
    void observerFailureIsFailOpen() {
        CatholicLLMObserver broken = new CatholicLLMObserver() {
            @Override
            public void onRequest(
                String exchangeId, String provider, String endpoint, Map<String, String> headers,
                String body, boolean stream
            ) {
                throw new IllegalStateException("logger unavailable");
            }
        };
        var exchange = CatholicLLMObservationSupport.exchange("test", "https://example.test", false);

        assertDoesNotThrow(() ->
            CatholicLLMObservationSupport.request(broken, exchange, Map.of(), "body"));
    }
}
