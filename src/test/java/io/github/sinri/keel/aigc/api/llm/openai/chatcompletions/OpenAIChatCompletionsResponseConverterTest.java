package io.github.sinri.keel.aigc.api.llm.openai.chatcompletions;

import io.github.sinri.keel.aigc.api.llm.catholic.CatholicLLMResponse;
import io.github.sinri.keel.aigc.api.llm.catholic.response.CatholicLLMUsage;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OpenAIChatCompletionsResponseConverterTest {

    private final OpenAIChatCompletionsResponseConverter converter = new OpenAIChatCompletionsResponseConverter();

    @Test
    void testConvertSimpleTextResponse() {
        JsonObject openaiResponse = new JsonObject()
            .put("id", "chatcmpl-123")
            .put("model", "gpt-4o")
            .put("choices", new JsonArray()
                .add(new JsonObject()
                    .put("index", 0)
                    .put("message", new JsonObject()
                        .put("role", "assistant")
                        .put("content", "Hello! How can I help you today?"))
                    .put("finish_reason", "stop")))
            .put("usage", new JsonObject()
                .put("prompt_tokens", 10)
                .put("completion_tokens", 20)
                .put("total_tokens", 30));

        CatholicLLMResponse response = converter.convert(openaiResponse);

        assertEquals("chatcmpl-123", response.id());
        assertEquals("Hello! How can I help you today?", response.text());
        assertFalse(response.hasToolCalls());

        CatholicLLMUsage usage = response.usage();
        assertEquals(10, usage.promptTokens());
        assertEquals(20, usage.completionTokens());
        assertEquals(30, usage.totalTokens());
    }

    @Test
    void testConvertResponseWithToolCalls() {
        JsonObject toolCall = new JsonObject()
            .put("id", "call_abc123")
            .put("type", "function")
            .put("function", new JsonObject()
                .put("name", "get_weather")
                .put("arguments", "{\"location\": \"Beijing\"}"));

        JsonObject message = new JsonObject()
            .put("role", "assistant")
            .put("content", (String) null)
            .put("tool_calls", new JsonArray().add(toolCall));

        JsonObject openaiResponse = new JsonObject()
            .put("id", "chatcmpl-456")
            .put("model", "gpt-4o")
            .put("choices", new JsonArray()
                .add(new JsonObject()
                    .put("index", 0)
                    .put("message", message)
                    .put("finish_reason", "tool_calls")))
            .put("usage", new JsonObject()
                .put("prompt_tokens", 15)
                .put("completion_tokens", 25)
                .put("total_tokens", 40));

        CatholicLLMResponse response = converter.convert(openaiResponse);

        assertEquals("chatcmpl-456", response.id());
        assertNull(response.text());
        assertTrue(response.hasToolCalls());

        var toolCalls = response.message().toolCalls();
        assertEquals(1, toolCalls.size());

        var tc = toolCalls.get(0);
        assertEquals("call_abc123", tc.id());
        assertEquals("function", tc.type());
        assertEquals("get_weather", tc.function().name());
        assertEquals("{\"location\": \"Beijing\"}", tc.function().arguments());
    }

    @Test
    void testConvertResponseWithMixedContent() {
        JsonObject toolCall = new JsonObject()
            .put("id", "call_xyz")
            .put("type", "function")
            .put("function", new JsonObject()
                .put("name", "get_weather")
                .put("arguments", "{\"city\": \"Shanghai\"}"));

        JsonObject message = new JsonObject()
            .put("role", "assistant")
            .put("content", "Let me check the weather for you.")
            .put("tool_calls", new JsonArray().add(toolCall));

        JsonObject openaiResponse = new JsonObject()
            .put("id", "chatcmpl-789")
            .put("model", "gpt-4o")
            .put("choices", new JsonArray()
                .add(new JsonObject()
                    .put("index", 0)
                    .put("message", message)
                    .put("finish_reason", "tool_calls")));

        CatholicLLMResponse response = converter.convert(openaiResponse);

        assertEquals("chatcmpl-789", response.id());
        assertEquals("Let me check the weather for you.", response.text());
        assertTrue(response.hasToolCalls());
    }

    @Test
    void testConvertEmptyResponse() {
        JsonObject openaiResponse = new JsonObject()
            .put("id", "chatcmpl-empty")
            .put("choices", new JsonArray());

        CatholicLLMResponse response = converter.convert(openaiResponse);

        assertEquals("chatcmpl-empty", response.id());
        assertEquals("", response.text());
    }

    @Test
    void testConvertResponseWithoutUsage() {
        JsonObject openaiResponse = new JsonObject()
            .put("id", "chatcmpl-no-usage")
            .put("choices", new JsonArray()
                .add(new JsonObject()
                    .put("index", 0)
                    .put("message", new JsonObject()
                        .put("role", "assistant")
                        .put("content", "Response without usage"))));

        CatholicLLMResponse response = converter.convert(openaiResponse);

        assertEquals("chatcmpl-no-usage", response.id());
        assertEquals("Response without usage", response.text());

        CatholicLLMUsage usage = response.usage();
        assertNull(usage.promptTokens());
        assertNull(usage.completionTokens());
        assertNull(usage.totalTokens());
    }

    @Test
    void testConvertMultipleToolCalls() {
        JsonObject toolCall1 = new JsonObject()
            .put("id", "call_1")
            .put("type", "function")
            .put("function", new JsonObject()
                .put("name", "func_a")
                .put("arguments", "{}"));

        JsonObject toolCall2 = new JsonObject()
            .put("id", "call_2")
            .put("type", "function")
            .put("function", new JsonObject()
                .put("name", "func_b")
                .put("arguments", "{\"x\": 1}"));

        JsonObject message = new JsonObject()
            .put("role", "assistant")
            .put("tool_calls", new JsonArray()
                .add(toolCall1)
                .add(toolCall2));

        JsonObject openaiResponse = new JsonObject()
            .put("id", "chatcmpl-multi")
            .put("choices", new JsonArray()
                .add(new JsonObject()
                    .put("index", 0)
                    .put("message", message)));

        CatholicLLMResponse response = converter.convert(openaiResponse);

        var toolCalls = response.message().toolCalls();
        assertEquals(2, toolCalls.size());

        assertEquals("call_1", toolCalls.get(0).id());
        assertEquals("func_a", toolCalls.get(0).function().name());

        assertEquals("call_2", toolCalls.get(1).id());
        assertEquals("func_b", toolCalls.get(1).function().name());
    }
}