package io.github.sinri.keel.aigc.api.llm.dashscope.textgeneration;

import io.github.sinri.keel.aigc.api.llm.catholic.CatholicLLM;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DashScopeTextGenerationClientTest {

    @Test
    void testBuilderRequiresHttpClient() {
        assertThrows(IllegalArgumentException.class, () -> {
            DashScopeTextGenerationClient.builder()
                .apiKey("test-key")
                .build();
        });
    }

    @Test
    void testBuilderRequiresApiKey() {
        assertThrows(IllegalArgumentException.class, () -> {
            DashScopeTextGenerationClient.builder()
                .httpClient(io.vertx.core.Vertx.vertx().createHttpClient())
                .build();
        });
    }

    @Test
    void testBuilderCreatesClient() {
        DashScopeTextGenerationClient client = DashScopeTextGenerationClient.builder()
            .httpClient(io.vertx.core.Vertx.vertx().createHttpClient())
            .apiKey("test-api-key")
            .build();

        assertNotNull(client);
        assertTrue(client instanceof CatholicLLM);
    }
}