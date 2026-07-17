package io.github.sinri.keel.aigc.api.llm.openai.chatcompletions;

import io.github.sinri.keel.base.async.Keel;
import io.vertx.core.Vertx;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * OpenAIChatCompletionsClient 测试类
 */
class OpenAIChatCompletionsLLMTest {

    private final Keel keel = Keel.create(Vertx.vertx());

    @Test
    void testBuilderRequiresKeel() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
            OpenAIChatCompletionsLLM.builder().build());
        assertEquals("keel is required", exception.getMessage());
    }

    @Test
    void testBuilderRequiresHttpClient() {
        assertThrows(IllegalArgumentException.class, () -> {
            OpenAIChatCompletionsLLM.builder()
                                    .keel(keel)
                                    .apiKey("test-key")
                                    .build();
        });
    }

    @Test
    void testBuilderRequiresApiKey() {
        assertThrows(IllegalArgumentException.class, () -> {
            OpenAIChatCompletionsLLM.builder()
                                    .keel(keel)
                                    .httpClient(io.vertx.core.Vertx.vertx().createHttpClient())
                                    .build();
        });
    }

    @Test
    void testBuilderWithEmptyApiKey() {
        assertThrows(IllegalArgumentException.class, () -> {
            OpenAIChatCompletionsLLM.builder()
                                    .keel(keel)
                                    .httpClient(io.vertx.core.Vertx.vertx().createHttpClient())
                                    .apiKey("")
                                    .build();
        });
    }

    @Test
    void testBuilderCreatesClientWithDefaults() {
        OpenAIChatCompletionsLLM client = OpenAIChatCompletionsLLM.builder()
                                                                  .keel(keel)
                                                                  .httpClient(io.vertx.core.Vertx.vertx().createHttpClient())
                                                                  .apiKey("test-api-key")
                                                                  .build();

        assertNotNull(client);
    }

    @Test
    void testBuilderCreatesClientWithCustomBaseUrl() {
        OpenAIChatCompletionsLLM client = OpenAIChatCompletionsLLM.builder()
                                                                  .keel(keel)
                                                                  .httpClient(io.vertx.core.Vertx.vertx().createHttpClient())
                                                                  .apiKey("test-api-key")
                                                                  .baseUrl("https://custom.api.com/v1")
                                                                  .build();

        assertNotNull(client);
    }

    @Test
    void testClientImplementsCatholicLLM() {
        OpenAIChatCompletionsLLM client = OpenAIChatCompletionsLLM.builder()
                                                                  .keel(keel)
                                                                  .httpClient(io.vertx.core.Vertx.vertx().createHttpClient())
                                                                  .apiKey("test-api-key")
                                                                  .build();

        assertTrue(client instanceof io.github.sinri.keel.aigc.api.llm.catholic.CatholicLLM);
    }

}
