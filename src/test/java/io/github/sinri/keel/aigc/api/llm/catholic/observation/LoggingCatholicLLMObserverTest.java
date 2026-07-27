package io.github.sinri.keel.aigc.api.llm.catholic.observation;

import io.github.sinri.keel.logger.api.LogLevel;
import io.github.sinri.keel.logger.api.adapter.LogWriterAdapter;
import io.github.sinri.keel.logger.api.log.Log;
import io.github.sinri.keel.logger.api.logger.Logger;
import io.github.sinri.keel.logger.api.logger.SpecificLogger;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LoggingCatholicLLMObserverTest {
    @Test
    void loggerNeverReceivesSecrets() {
        CapturingLogger logger = new CapturingLogger();
        LoggingCatholicLLMObserver observer = new LoggingCatholicLLMObserver(logger);

        observer.onRequest(
            "exchange", "provider", "https://example.test?token=query-secret",
            Map.of("Authorization", "Bearer header-secret"),
            "{\"password\":\"body-secret\",\"content\":\"Bearer content-secret\"}", false
        );
        observer.onStreamEvent(
            "exchange", "provider", 0, "data: {\"api_key\":\"stream-secret\"}", 10
        );

        String output = logger.logs.stream()
            .map(log -> log.message() + log.context().toMap())
            .reduce("", (left, right) -> left + "\n" + right);
        assertFalse(output.contains("query-secret"));
        assertFalse(output.contains("header-secret"));
        assertFalse(output.contains("body-secret"));
        assertFalse(output.contains("content-secret"));
        assertFalse(output.contains("stream-secret"));
        assertTrue(output.contains(DefaultCatholicLLMLogRedactor.REDACTED));
        assertEquals(List.of("LLM request", "LLM stream event"),
            logger.logs.stream().map(Log::message).toList());
        assertTrue(logger.logs.get(0).context().toMap().containsKey("body"));
        assertTrue(logger.logs.get(1).context().toMap().containsKey("event"));
    }

    private static final class CapturingLogger implements Logger {
        private final List<Log> logs = new ArrayList<>();
        private final LogWriterAdapter adapter = (topic, log) -> logs.add(new Log(log));
        private LogLevel visibleLevel = LogLevel.DEBUG;

        @Override
        public Supplier<Log> specificLogSupplier() {
            return Log::new;
        }

        @Override
        public LogWriterAdapter adapter() {
            return adapter;
        }

        @Override
        public LogLevel visibleLevel() {
            return visibleLevel;
        }

        @Override
        public SpecificLogger<Log> visibleLevel(LogLevel level) {
            visibleLevel = level;
            return this;
        }

        @Override
        public String topic() {
            return "test";
        }
    }
}
