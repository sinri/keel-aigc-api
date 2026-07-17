package io.github.sinri.keel.aigc.api.llm.openai.responses;

import io.github.sinri.keel.base.async.Keel;
import io.vertx.core.Vertx;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OpenAIResponsesLLMTest {

    private final Keel keel = Keel.create(Vertx.vertx());

    @Test
    void testBuilderRequiresKeel() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
            OpenAIResponsesLLM.builder().build());
        assertEquals("keel is required", exception.getMessage());
    }

    @Test
    void testBuilderRequiresHttpClient() {
        assertThrows(IllegalArgumentException.class, () -> {
            OpenAIResponsesLLM.builder()
                              .keel(keel)
                              .apiKey("test-key")
                              .build();
        });
    }

    @Test
    void testBuilderRequiresApiKey() {
        assertThrows(IllegalArgumentException.class, () -> {
            OpenAIResponsesLLM.builder()
                              .keel(keel)
                              .httpClient(Vertx.vertx().createHttpClient())
                              .build();
        });
    }

    @Test
    void testBuilderWithEmptyApiKey() {
        assertThrows(IllegalArgumentException.class, () -> {
            OpenAIResponsesLLM.builder()
                              .keel(keel)
                              .httpClient(Vertx.vertx().createHttpClient())
                              .apiKey("")
                              .build();
        });
    }

    @Test
    void testBuilderCreatesClientWithDefaults() {
        OpenAIResponsesLLM client = OpenAIResponsesLLM.builder()
                                                      .keel(keel)
                                                      .httpClient(Vertx.vertx().createHttpClient())
                                                      .apiKey("test-api-key")
                                                      .build();

        assertNotNull(client);
    }

    @Test
    void testBuilderCreatesClientWithCustomBaseUrl() {
        OpenAIResponsesLLM client = OpenAIResponsesLLM.builder()
                                                      .keel(keel)
                                                      .httpClient(Vertx.vertx().createHttpClient())
                                                      .apiKey("test-api-key")
                                                      .baseUrl("https://custom.api.com/v1")
                                                      .build();

        assertNotNull(client);
    }

    @Test
    void testClientImplementsCatholicLLM() {
        OpenAIResponsesLLM client = OpenAIResponsesLLM.builder()
                                                      .keel(keel)
                                                      .httpClient(Vertx.vertx().createHttpClient())
                                                      .apiKey("test-api-key")
                                                      .build();

        assertTrue(client instanceof io.github.sinri.keel.aigc.api.llm.catholic.CatholicLLM);
    }
}
