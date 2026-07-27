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
        logger.debug("LLM request", context -> context
            .put("exchange_id", exchangeId)
            .put("provider", provider)
            .put("stream", stream)
            .put("endpoint", redactor.redactText(endpoint))
            .put("headers", redactHeaders(headers))
            .put("body", payload(body)));
    }

    @Override
    public void onResponseStarted(
        String exchangeId, String provider, int statusCode, Map<String, String> headers,
        long elapsedMillis
    ) {
        logger.debug("LLM response started", context -> context
            .put("exchange_id", exchangeId)
            .put("provider", provider)
            .put("status_code", statusCode)
            .put("elapsed_ms", elapsedMillis)
            .put("headers", redactHeaders(headers)));
    }

    @Override
    public void onResponse(
        String exchangeId, String provider, int statusCode, Map<String, String> headers,
        String body, long elapsedMillis
    ) {
        logger.debug("LLM response", context -> context
            .put("exchange_id", exchangeId)
            .put("provider", provider)
            .put("status_code", statusCode)
            .put("elapsed_ms", elapsedMillis)
            .put("headers", redactHeaders(headers))
            .put("body", payload(body)));
    }

    @Override
    public void onStreamEvent(
        String exchangeId, String provider, long sequence, String rawEvent, long elapsedMillis
    ) {
        logger.debug("LLM stream event", context -> context
            .put("exchange_id", exchangeId)
            .put("provider", provider)
            .put("sequence", sequence)
            .put("elapsed_ms", elapsedMillis)
            .put("event", payload(rawEvent)));
    }

    @Override
    public void onFailure(
        String exchangeId, String provider, CatholicLLMObservationStage stage,
        Throwable cause, long elapsedMillis
    ) {
        String causeType = cause == null ? null : cause.getClass().getName();
        String causeMessage = cause == null ? null :
            redactor.redactText(String.valueOf(cause.getMessage()));
        logger.warning("LLM failure", context -> context
            .put("exchange_id", exchangeId)
            .put("provider", provider)
            .put("stage", stage)
            .put("elapsed_ms", elapsedMillis)
            .put("cause_type", causeType)
            .put("cause_message", causeMessage));
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
