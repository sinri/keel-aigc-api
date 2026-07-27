package io.github.sinri.keel.aigc.api.llm.catholic.observation;

import io.github.sinri.keel.logger.api.logger.Logger;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Writes redacted provider-native LLM I/O at DEBUG level and failures at WARNING level.
 */
public final class LoggingCatholicLLMObserver implements CatholicLLMObserver {
    private static final int DEFAULT_MAX_PAYLOAD_LENGTH = 1_048_576;

    private final Logger logger;
    private final CatholicLLMLogRedactor redactor;
    private final int maxPayloadLength;

    public LoggingCatholicLLMObserver(Logger logger) {
        this(logger, CatholicLLMLogRedactor.secureDefault(), DEFAULT_MAX_PAYLOAD_LENGTH);
    }

    public LoggingCatholicLLMObserver(
        Logger logger, CatholicLLMLogRedactor redactor, int maxPayloadLength
    ) {
        this.logger = Objects.requireNonNull(logger);
        this.redactor = Objects.requireNonNull(redactor);
        if (maxPayloadLength < 1) {
            throw new IllegalArgumentException("maxPayloadLength must be positive");
        }
        this.maxPayloadLength = maxPayloadLength;
    }

    @Override
    public void onRequest(
        String exchangeId, String provider, String endpoint, Map<String, String> headers,
        String body, boolean stream
    ) {
        logger.debug("[LLM REQUEST] exchange_id=%s provider=%s stream=%s endpoint=%s headers=%s body=%s"
            .formatted(exchangeId, provider, stream, redactor.redactText(endpoint),
                redactHeaders(headers), payload(body)));
    }

    @Override
    public void onResponseStarted(
        String exchangeId, String provider, int statusCode, Map<String, String> headers,
        long elapsedMillis
    ) {
        logger.debug("[LLM RESPONSE STARTED] exchange_id=%s provider=%s status=%d elapsed_ms=%d headers=%s"
            .formatted(exchangeId, provider, statusCode, elapsedMillis, redactHeaders(headers)));
    }

    @Override
    public void onResponse(
        String exchangeId, String provider, int statusCode, Map<String, String> headers,
        String body, long elapsedMillis
    ) {
        logger.debug("[LLM RESPONSE] exchange_id=%s provider=%s status=%d elapsed_ms=%d headers=%s body=%s"
            .formatted(exchangeId, provider, statusCode, elapsedMillis, redactHeaders(headers), payload(body)));
    }

    @Override
    public void onStreamEvent(
        String exchangeId, String provider, long sequence, String rawEvent, long elapsedMillis
    ) {
        logger.debug("[LLM STREAM EVENT] exchange_id=%s provider=%s sequence=%d elapsed_ms=%d event=%s"
            .formatted(exchangeId, provider, sequence, elapsedMillis, payload(rawEvent)));
    }

    @Override
    public void onFailure(
        String exchangeId, String provider, CatholicLLMObservationStage stage,
        Throwable cause, long elapsedMillis
    ) {
        String detail = cause == null ? "unknown" :
            cause.getClass().getName() + ": " + redactor.redactText(String.valueOf(cause.getMessage()));
        logger.warning("[LLM FAILURE] exchange_id=%s provider=%s stage=%s elapsed_ms=%d cause=%s"
            .formatted(exchangeId, provider, stage, elapsedMillis, detail));
    }

    private Map<String, String> redactHeaders(Map<String, String> headers) {
        Map<String, String> result = new LinkedHashMap<>();
        headers.forEach((key, value) -> result.put(key, redactor.redactHeader(key, value)));
        return result;
    }

    private String payload(String body) {
        String value = redactor.redactBody(body);
        if (value == null || value.length() <= maxPayloadLength) {
            return value;
        }
        return value.substring(0, maxPayloadLength)
            + "<truncated,original_length=" + value.length() + ">";
    }
}
