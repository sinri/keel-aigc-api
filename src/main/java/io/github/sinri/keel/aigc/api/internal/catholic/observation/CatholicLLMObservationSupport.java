package io.github.sinri.keel.aigc.api.internal.catholic.observation;

import io.github.sinri.keel.aigc.api.trace.CatholicTraceContext;

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

    public static Exchange exchange(String provider, String endpoint, boolean stream,
                                    CatholicTraceContext trace) {
        String id = UUID.randomUUID().toString();
        return new Exchange(id, provider, endpoint, stream, System.nanoTime(),
                trace.child("exchange_id", id).child("provider", provider));
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
        exchange.trace().payload("provider_request", Map.of("provider", exchange.provider(),
                "endpoint", safeEndpoint(exchange.endpoint()), "stream", exchange.stream()), body, false);
        safely(() -> observer.onRequest(
            exchange.id(), exchange.provider(), exchange.endpoint(), headers, body, exchange.stream()
        ));
    }

    public static void responseStarted(
        CatholicLLMObserver observer, Exchange exchange, HttpClientResponse response
    ) {
        var metadata = new LinkedHashMap<String, Object>();
        metadata.put("status_code", response.statusCode());
        for (String name : new String[]{"x-request-id", "request-id", "content-type"}) {
            String value = response.getHeader(name);
            if (value != null) metadata.put(name, value);
        }
        exchange.trace().event("http_response", metadata);
        safely(() -> observer.onResponseStarted(
            exchange.id(), exchange.provider(), response.statusCode(), headers(response.headers()),
            exchange.elapsedMillis()
        ));
    }

    public static void response(
        CatholicLLMObserver observer, Exchange exchange, HttpClientResponse response, Buffer body
    ) {
        captureBuffer(exchange, "provider_response", body, 0);
        safely(() -> observer.onResponse(
            exchange.id(), exchange.provider(), response.statusCode(), headers(response.headers()),
            body.toString(), exchange.elapsedMillis()
        ));
    }

    public static void failure(
        CatholicLLMObserver observer, Exchange exchange, CatholicLLMObservationStage stage, Throwable cause
    ) {
        exchange.trace().failure(stage.name(), cause);
        safely(() -> observer.onFailure(
            exchange.id(), exchange.provider(), stage, cause, exchange.elapsedMillis()
        ));
    }

    public record Exchange(
        String id, String provider, String endpoint, boolean stream, long startedNanos,
        CatholicTraceContext trace
    ) {
        public Exchange(String id, String provider, String endpoint, boolean stream, long startedNanos) {
            this(id, provider, endpoint, stream, startedNanos,
                    CatholicTraceContext.none());
        }
        public long elapsedMillis() {
            return (System.nanoTime() - startedNanos) / 1_000_000L;
        }
    }

    public static io.vertx.core.json.JsonObject convertRequest(
            io.github.sinri.keel.aigc.api.llm.catholic.CatholicLLMRequest request,
            java.util.function.Supplier<io.vertx.core.json.JsonObject> converter) {
        try { return converter.get(); }
        catch (RuntimeException cause) {
            request.traceContext().failure("REQUEST_CONVERSION", cause);
            throw cause;
        }
    }

    public static <T> T convertResponse(Exchange exchange, java.util.function.Supplier<T> converter) {
        try { return converter.get(); }
        catch (RuntimeException cause) {
            exchange.trace().failure("RESPONSE_CONVERSION", cause);
            throw cause;
        }
    }

    private static String safeEndpoint(String endpoint) {
        try {
            var uri = java.net.URI.create(endpoint);
            return uri.getScheme() + "://" + uri.getHost() + (uri.getPort() < 0 ? "" : ":" + uri.getPort()) + uri.getPath();
        } catch (RuntimeException ignored) { return "invalid_endpoint"; }
    }

    /** Capture before SSE framing; cap copying even if a transport buffer is unexpectedly large. */
    public static void captureBuffer(Exchange exchange, String kind, Buffer buffer, long sequence) {
        if (!exchange.trace().enabled()) return;
        int retained = Math.min(buffer.length(), 8192);
        int cr = 0, lf = 0;
        for (int i = 0; i < retained; i++) {
            byte value = buffer.getByte(i);
            if (value == '\r') cr++;
            if (value == '\n') lf++;
        }
        exchange.trace().payload(kind, Map.of("buffer_sequence", sequence, "original_bytes", buffer.length(),
                "transport_truncated", retained != buffer.length(), "encoding", "base64",
                "sample_cr_count", cr, "sample_lf_count", lf),
                java.util.Base64.getEncoder().encodeToString(buffer.getBytes(0, retained)), false);
    }

    public static CatholicLLMObserver orNoop(CatholicLLMObserver observer) {
        return observer == null ? CatholicLLMObserver.noop() : observer;
    }
}
