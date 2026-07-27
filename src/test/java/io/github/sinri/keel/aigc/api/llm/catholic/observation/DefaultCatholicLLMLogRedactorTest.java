package io.github.sinri.keel.aigc.api.llm.catholic.observation;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DefaultCatholicLLMLogRedactorTest {
    private final CatholicLLMLogRedactor redactor = CatholicLLMLogRedactor.secureDefault();

    @Test
    void redactsSensitiveHeaders() {
        assertFalse(redactor.redactHeader("Authorization", "Bearer top-secret").contains("top-secret"));
        assertFalse(redactor.redactHeader("x-api-key", "key-value").contains("key-value"));
    }

    @Test
    void recursivelyRedactsJsonAndTokensInsideContent() {
        String source = """
            {"api_key":"plain-secret","nested":{"client_secret":"nested-secret"},
             "content":"use Bearer bearer-secret and sk-abcdefghijklmnop"}
            """;
        String result = redactor.redactBody(source);

        assertFalse(result.contains("plain-secret"));
        assertFalse(result.contains("nested-secret"));
        assertFalse(result.contains("bearer-secret"));
        assertFalse(result.contains("sk-abcdefghijklmnop"));
        assertTrue(result.contains(DefaultCatholicLLMLogRedactor.REDACTED));
    }

    @Test
    void redactsJsonInsideSseData() {
        String result = redactor.redactBody("""
            event: message
            data: {"type":"delta","password":"sse-secret"}
            """);

        assertFalse(result.contains("sse-secret"));
        assertTrue(result.contains("\"password\":\"***REDACTED***\""));
    }

    @Test
    void replacesLargeBase64LikePayload() {
        String value = "A".repeat(600);
        String result = redactor.redactText(value);

        assertFalse(result.contains(value));
        assertTrue(result.contains("binary-like-data omitted"));
        assertTrue(result.contains("sha256="));
    }
}
