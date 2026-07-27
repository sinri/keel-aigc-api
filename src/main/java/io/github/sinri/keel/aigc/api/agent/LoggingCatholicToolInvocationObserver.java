package io.github.sinri.keel.aigc.api.agent;

import io.github.sinri.keel.aigc.api.llm.catholic.observation.CatholicLLMLogRedactor;
import io.github.sinri.keel.logger.api.logger.Logger;

import java.util.Objects;

/**
 * Logger-backed tool execution audit observer.
 * <p>
 * Arguments, results and failure messages are redacted before being placed in the log context.
 */
public final class LoggingCatholicToolInvocationObserver implements CatholicToolInvocationObserver {
    private static final int DEFAULT_MAX_PAYLOAD_LENGTH = 1_048_576;

    private final Logger logger;
    private final CatholicLLMLogRedactor redactor;
    private final int maxPayloadLength;

    public LoggingCatholicToolInvocationObserver(Logger logger) {
        this(logger, CatholicLLMLogRedactor.secureDefault(), DEFAULT_MAX_PAYLOAD_LENGTH);
    }

    public LoggingCatholicToolInvocationObserver(
        Logger logger, CatholicLLMLogRedactor redactor, int maxPayloadLength
    ) {
        this.logger = Objects.requireNonNull(logger, "logger");
        this.redactor = Objects.requireNonNull(redactor, "redactor");
        if (maxPayloadLength < 1) {
            throw new IllegalArgumentException("maxPayloadLength must be positive");
        }
        this.maxPayloadLength = maxPayloadLength;
    }

    @Override
    public void onStarted(CatholicToolInvocationObservation observation) {
        logger.info("Tool invocation started", context -> commonContext(context, observation)
            .put("arguments", payload(observation.arguments())));
    }

    @Override
    public void onSucceeded(
        CatholicToolInvocationObservation observation, String result, long elapsedMillis
    ) {
        logger.info("Tool invocation succeeded", context -> commonContext(context, observation)
            .put("elapsed_ms", elapsedMillis)
            .put("result", payload(result)));
    }

    @Override
    public void onFailed(
        CatholicToolInvocationObservation observation, Throwable cause, long elapsedMillis
    ) {
        logger.warning("Tool invocation failed", context -> commonContext(context, observation)
            .put("elapsed_ms", elapsedMillis)
            .put("cause_type", cause == null ? null : cause.getClass().getName())
            .put("cause_message", cause == null ? null :
                redactor.redactText(String.valueOf(cause.getMessage()))));
    }

    private io.github.sinri.keel.logger.api.log.LogContext commonContext(
        io.github.sinri.keel.logger.api.log.LogContext context,
        CatholicToolInvocationObservation observation
    ) {
        return context
            .put("invocation_id", observation.invocationId())
            .put("tool_call_id", observation.toolCallId())
            .put("function_name", observation.functionName())
            .put("started_at", observation.startedAt().toString());
    }

    private String payload(String value) {
        String redacted = redactor.redactBody(value);
        if (redacted == null || redacted.length() <= maxPayloadLength) {
            return redacted;
        }
        return redacted.substring(0, maxPayloadLength)
            + "<truncated,original_length=" + redacted.length() + ">";
    }
}
