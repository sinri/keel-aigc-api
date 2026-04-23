package io.github.sinri.keel.aigc.api.llm.dashscope.multimodalgeneration;

import io.github.sinri.keel.aigc.api.llm.catholic.CatholicLLM;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DashScopeMultimodalGenerationLLMTest {

    @Test
    void testBuilderRequiresHttpClient() {
        assertThrows(IllegalArgumentException.class, () -> {
            DashScopeMultimodalGenerationLLM.builder()
                                            .apiKey("test-key")
                                            .build();
        });
    }

    @Test
    void testBuilderRequiresApiKey() {
        assertThrows(IllegalArgumentException.class, () -> {
            DashScopeMultimodalGenerationLLM.builder()
                                            .httpClient(io.vertx.core.Vertx.vertx().createHttpClient())
                                            .build();
        });
    }

    @Test
    void testBuilderCreatesClient() {
        DashScopeMultimodalGenerationLLM client = DashScopeMultimodalGenerationLLM.builder()
                                                                                  .httpClient(io.vertx.core.Vertx.vertx().createHttpClient())
                                                                                  .apiKey("test-api-key")
                                                                                  .build();

        assertNotNull(client);
        assertTrue(client instanceof CatholicLLM);
    }
}