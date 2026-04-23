package io.github.sinri.keel.aigc.api.llm.openai.chatcompletions;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * OpenAIChatCompletionsClient 测试类
 */
class OpenAIChatCompletionsLLMTest {

    @Test
    void testBuilderRequiresHttpClient() {
        assertThrows(IllegalArgumentException.class, () -> {
            OpenAIChatCompletionsLLM.builder()
                                    .apiKey("test-key")
                                    .build();
        });
    }

    @Test
    void testBuilderRequiresApiKey() {
        assertThrows(IllegalArgumentException.class, () -> {
            OpenAIChatCompletionsLLM.builder()
                                    .httpClient(io.vertx.core.Vertx.vertx().createHttpClient())
                                    .build();
        });
    }

    @Test
    void testBuilderWithEmptyApiKey() {
        assertThrows(IllegalArgumentException.class, () -> {
            OpenAIChatCompletionsLLM.builder()
                                    .httpClient(io.vertx.core.Vertx.vertx().createHttpClient())
                                    .apiKey("")
                                    .build();
        });
    }

    @Test
    void testBuilderCreatesClientWithDefaults() {
        OpenAIChatCompletionsLLM client = OpenAIChatCompletionsLLM.builder()
                                                                  .httpClient(io.vertx.core.Vertx.vertx().createHttpClient())
                                                                  .apiKey("test-api-key")
                                                                  .build();

        assertNotNull(client);
    }

    @Test
    void testBuilderCreatesClientWithCustomBaseUrl() {
        OpenAIChatCompletionsLLM client = OpenAIChatCompletionsLLM.builder()
                                                                  .httpClient(io.vertx.core.Vertx.vertx().createHttpClient())
                                                                  .apiKey("test-api-key")
                                                                  .baseUrl("https://custom.api.com/v1")
                                                                  .build();

        assertNotNull(client);
    }

    @Test
    void testClientImplementsCatholicLLM() {
        OpenAIChatCompletionsLLM client = OpenAIChatCompletionsLLM.builder()
                                                                  .httpClient(io.vertx.core.Vertx.vertx().createHttpClient())
                                                                  .apiKey("test-api-key")
                                                                  .build();

        assertTrue(client instanceof io.github.sinri.keel.aigc.api.llm.catholic.CatholicLLM);
    }

}
