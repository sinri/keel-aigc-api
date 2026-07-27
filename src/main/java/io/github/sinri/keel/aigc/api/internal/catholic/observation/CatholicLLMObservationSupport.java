package io.github.sinri.keel.aigc.api.internal.catholic.observation;

import io.github.sinri.keel.aigc.api.llm.catholic.observation.CatholicLLMObserver;
import io.github.sinri.keel.aigc.api.llm.catholic.observation.CatholicLLMObservationStage;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClientResponse;
import io.vertx.core.MultiMap;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

public final class CatholicLLMObservationSupport {
    private CatholicLLMObservationSupport() {
    }

    public static Exchange exchange(String provider, String endpoint, boolean stream) {
        return new Exchange(UUID.randomUUID().toString(), provider, endpoint, stream, System.nanoTime());
    }

    public static void safely(Runnable observation) {
        try {
            observation.run();
        } catch (Throwable ignored) {
            // Observability is deliberately fail-open.
        }
    }

    public static Map<String, String> headers(MultiMap headers) {
        Map<String, String> result = new LinkedHashMap<>();
        headers.forEach(entry -> result.merge(entry.getKey(), entry.getValue(), (a, b) -> a + "," + b));
        return Map.copyOf(result);
    }

    public static void request(
        CatholicLLMObserver observer, Exchange exchange, Map<String, String> headers, String body
    ) {
        safely(() -> observer.onRequest(
            exchange.id(), exchange.provider(), exchange.endpoint(), headers, body, exchange.stream()
        ));
    }

    public static void responseStarted(
        CatholicLLMObserver observer, Exchange exchange, HttpClientResponse response
    ) {
        safely(() -> observer.onResponseStarted(
            exchange.id(), exchange.provider(), response.statusCode(), headers(response.headers()),
            exchange.elapsedMillis()
        ));
    }

    public static void response(
        CatholicLLMObserver observer, Exchange exchange, HttpClientResponse response, Buffer body
    ) {
        safely(() -> observer.onResponse(
            exchange.id(), exchange.provider(), response.statusCode(), headers(response.headers()),
            body.toString(), exchange.elapsedMillis()
        ));
    }

    public static void failure(
        CatholicLLMObserver observer, Exchange exchange, CatholicLLMObservationStage stage, Throwable cause
    ) {
        safely(() -> observer.onFailure(
            exchange.id(), exchange.provider(), stage, cause, exchange.elapsedMillis()
        ));
    }

    public record Exchange(
        String id, String provider, String endpoint, boolean stream, long startedNanos
    ) {
        public long elapsedMillis() {
            return (System.nanoTime() - startedNanos) / 1_000_000L;
        }
    }

    public static CatholicLLMObserver orNoop(CatholicLLMObserver observer) {
        return observer == null ? CatholicLLMObserver.noop() : observer;
    }
}
