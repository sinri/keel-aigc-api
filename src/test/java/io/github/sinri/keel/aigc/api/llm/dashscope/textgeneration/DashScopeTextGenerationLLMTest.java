package io.github.sinri.keel.aigc.api.llm.dashscope.textgeneration;

import io.github.sinri.keel.aigc.api.llm.catholic.CatholicLLM;
import io.github.sinri.keel.base.async.Keel;
import io.vertx.core.Vertx;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DashScopeTextGenerationLLMTest {

    private final Keel keel = Keel.create(Vertx.vertx());

    @Test
    void testBuilderRequiresKeel() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
            DashScopeTextGenerationLLM.builder().build());
        assertEquals("keel is required", exception.getMessage());
    }

    @Test
    void testBuilderRequiresHttpClient() {
        assertThrows(IllegalArgumentException.class, () -> {
            DashScopeTextGenerationLLM.builder()
                                      .keel(keel)
                                      .apiKey("test-key")
                                      .build();
        });
    }

    @Test
    void testBuilderRequiresApiKey() {
        assertThrows(IllegalArgumentException.class, () -> {
            DashScopeTextGenerationLLM.builder()
                                      .keel(keel)
                                      .httpClient(io.vertx.core.Vertx.vertx().createHttpClient())
                                      .build();
        });
    }

    @Test
    void testBuilderCreatesClient() {
        DashScopeTextGenerationLLM client = DashScopeTextGenerationLLM.builder()
                                                                      .keel(keel)
                                                                      .httpClient(io.vertx.core.Vertx.vertx().createHttpClient())
                                                                      .apiKey("test-api-key")
                                                                      .build();

        assertNotNull(client);
        assertTrue(client instanceof CatholicLLM);
    }
}
