package io.github.sinri.keel.aigc.api.llm.openai.chatcompletions;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * OpenAIChatCompletionsClient 测试类
 */
class OpenAIChatCompletionsClientTest {

    @Test
    void testBuilderRequiresWebClient() {
        assertThrows(IllegalArgumentException.class, () -> {
            OpenAIChatCompletionsClient.builder()
                .apiKey("test-key")
                .build();
        });
    }

    @Test
    void testBuilderRequiresApiKey() {
        assertThrows(IllegalArgumentException.class, () -> {
            OpenAIChatCompletionsClient.builder()
                .webClient(io.vertx.ext.web.client.WebClient.create(io.vertx.core.Vertx.vertx()))
                .build();
        });
    }

    @Test
    void testBuilderWithEmptyApiKey() {
        assertThrows(IllegalArgumentException.class, () -> {
            OpenAIChatCompletionsClient.builder()
                .webClient(io.vertx.ext.web.client.WebClient.create(io.vertx.core.Vertx.vertx()))
                .apiKey("")
                .build();
        });
    }

    @Test
    void testBuilderCreatesClientWithDefaults() {
        OpenAIChatCompletionsClient client = OpenAIChatCompletionsClient.builder()
            .webClient(io.vertx.ext.web.client.WebClient.create(io.vertx.core.Vertx.vertx()))
            .apiKey("test-api-key")
            .build();

        assertNotNull(client);
    }

    @Test
    void testBuilderCreatesClientWithCustomBaseUrl() {
        OpenAIChatCompletionsClient client = OpenAIChatCompletionsClient.builder()
            .webClient(io.vertx.ext.web.client.WebClient.create(io.vertx.core.Vertx.vertx()))
            .apiKey("test-api-key")
            .baseUrl("https://custom.api.com/v1")
            .build();

        assertNotNull(client);
    }

    @Test
    void testClientImplementsCatholicLLM() {
        OpenAIChatCompletionsClient client = OpenAIChatCompletionsClient.builder()
            .webClient(io.vertx.ext.web.client.WebClient.create(io.vertx.core.Vertx.vertx()))
            .apiKey("test-api-key")
            .build();

        assertTrue(client instanceof io.github.sinri.keel.aigc.api.llm.catholic.CatholicLLM);
    }

}