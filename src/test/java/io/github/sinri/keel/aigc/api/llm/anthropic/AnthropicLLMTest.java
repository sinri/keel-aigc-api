package io.github.sinri.keel.aigc.api.llm.anthropic;

import io.vertx.core.Vertx;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AnthropicLLMTest {

    @Test
    void testBuilderRequiresHttpClient() {
        assertThrows(IllegalArgumentException.class, () -> {
            AnthropicLLM.builder().apiKey("k").build();
        });
    }

    @Test
    void testBuilderRequiresApiKey() {
        assertThrows(IllegalArgumentException.class, () -> {
            AnthropicLLM.builder().httpClient(Vertx.vertx().createHttpClient()).build();
        });
    }

    @Test
    void testBuilderCreatesClient() {
        AnthropicLLM c = AnthropicLLM.builder()
                                     .httpClient(Vertx.vertx().createHttpClient())
                                     .apiKey("sk-ant-test")
                                     .build();
        assertNotNull(c);
        assertTrue(c instanceof io.github.sinri.keel.aigc.api.llm.catholic.CatholicLLM);
    }

    @Test
    void testBuilderCustomBaseUrlAndVersion() {
        AnthropicLLM c = AnthropicLLM.builder()
                                     .httpClient(Vertx.vertx().createHttpClient())
                                     .apiKey("k")
                                     .baseUrl("https://api.anthropic.com/v1")
                                     .anthropicVersion("2023-06-01")
                                     .build();
        assertNotNull(c);
    }
}
