package io.github.sinri.keel.aigc.api.llm.anthropic;

import io.github.sinri.keel.base.async.Keel;
import io.vertx.core.Vertx;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AnthropicLLMTest {

    private final Keel keel = Keel.create(Vertx.vertx());

    @Test
    void testBuilderRequiresKeel() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
            AnthropicLLM.builder().build());
        assertEquals("keel is required", exception.getMessage());
    }

    @Test
    void testBuilderRequiresHttpClient() {
        assertThrows(IllegalArgumentException.class, () -> {
            AnthropicLLM.builder().keel(keel).apiKey("k").build();
        });
    }

    @Test
    void testBuilderRequiresApiKey() {
        assertThrows(IllegalArgumentException.class, () -> {
            AnthropicLLM.builder().keel(keel).httpClient(Vertx.vertx().createHttpClient()).build();
        });
    }

    @Test
    void testBuilderCreatesClient() {
        AnthropicLLM c = AnthropicLLM.builder()
                                     .keel(keel)
                                     .httpClient(Vertx.vertx().createHttpClient())
                                     .apiKey("sk-ant-test")
                                     .build();
        assertNotNull(c);
        assertTrue(c instanceof io.github.sinri.keel.aigc.api.llm.catholic.CatholicLLM);
    }

    @Test
    void testBuilderCustomBaseUrlAndVersion() {
        AnthropicLLM c = AnthropicLLM.builder()
                                     .keel(keel)
                                     .httpClient(Vertx.vertx().createHttpClient())
                                     .apiKey("k")
                                     .baseUrl("https://api.anthropic.com/v1")
                                     .anthropicVersion("2023-06-01")
                                     .build();
        assertNotNull(c);
    }
}
