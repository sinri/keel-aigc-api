package io.github.sinri.keel.aigc.api.llm.openai.responses;

import io.vertx.core.Vertx;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OpenAIResponsesLLMTest {

    @Test
    void testBuilderRequiresHttpClient() {
        assertThrows(IllegalArgumentException.class, () -> {
            OpenAIResponsesLLM.builder()
                              .apiKey("test-key")
                              .build();
        });
    }

    @Test
    void testBuilderRequiresApiKey() {
        assertThrows(IllegalArgumentException.class, () -> {
            OpenAIResponsesLLM.builder()
                              .httpClient(Vertx.vertx().createHttpClient())
                              .build();
        });
    }

    @Test
    void testBuilderWithEmptyApiKey() {
        assertThrows(IllegalArgumentException.class, () -> {
            OpenAIResponsesLLM.builder()
                              .httpClient(Vertx.vertx().createHttpClient())
                              .apiKey("")
                              .build();
        });
    }

    @Test
    void testBuilderCreatesClientWithDefaults() {
        OpenAIResponsesLLM client = OpenAIResponsesLLM.builder()
                                                      .httpClient(Vertx.vertx().createHttpClient())
                                                      .apiKey("test-api-key")
                                                      .build();

        assertNotNull(client);
    }

    @Test
    void testBuilderCreatesClientWithCustomBaseUrl() {
        OpenAIResponsesLLM client = OpenAIResponsesLLM.builder()
                                                      .httpClient(Vertx.vertx().createHttpClient())
                                                      .apiKey("test-api-key")
                                                      .baseUrl("https://custom.api.com/v1")
                                                      .build();

        assertNotNull(client);
    }

    @Test
    void testClientImplementsCatholicLLM() {
        OpenAIResponsesLLM client = OpenAIResponsesLLM.builder()
                                                      .httpClient(Vertx.vertx().createHttpClient())
                                                      .apiKey("test-api-key")
                                                      .build();

        assertTrue(client instanceof io.github.sinri.keel.aigc.api.llm.catholic.CatholicLLM);
    }
}
