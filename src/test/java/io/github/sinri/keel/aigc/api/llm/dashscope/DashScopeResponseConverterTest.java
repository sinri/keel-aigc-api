package io.github.sinri.keel.aigc.api.llm.dashscope;

import io.github.sinri.keel.aigc.api.llm.catholic.CatholicLLMResponse;
import io.github.sinri.keel.aigc.api.llm.catholic.response.CatholicLLMUsage;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DashScopeResponseConverterTest {

    private final DashScopeResponseConverter converter = new DashScopeResponseConverter();

    @Test
    void testConvertSimpleTextResponse() {
        JsonObject message = new JsonObject()
            .put("role", "assistant")
            .put("content", "Hello! How can I help you?");

        JsonObject choice = new JsonObject()
            .put("finish_reason", "stop")
            .put("message", message);

        JsonObject output = new JsonObject()
            .put("choices", new JsonArray().add(choice));

        JsonObject usage = new JsonObject()
            .put("input_tokens", 10)
            .put("output_tokens", 20)
            .put("total_tokens", 30);

        JsonObject dashscopeResponse = new JsonObject()
            .put("request_id", "req-123")
            .put("output", output)
            .put("usage", usage);

        CatholicLLMResponse response = converter.convert(dashscopeResponse);

        assertEquals("req-123", response.id());
        assertEquals("Hello! How can I help you?", response.text());
        assertFalse(response.hasToolCalls());

        CatholicLLMUsage u = response.usage();
        assertEquals(10, u.promptTokens());
        assertEquals(20, u.completionTokens());
        assertEquals(30, u.totalTokens());
    }

    @Test
    void testConvertResponseWithToolCalls() {
        JsonObject toolCall = new JsonObject()
            .put("id", "call_abc")
            .put("type", "function")
            .put("function", new JsonObject()
                .put("name", "get_weather")
                .put("arguments", "{\"city\": \"Beijing\"}"));

        JsonObject message = new JsonObject()
            .put("role", "assistant")
            .put("content", (String) null)
            .put("tool_calls", new JsonArray().add(toolCall));

        JsonObject choice = new JsonObject()
            .put("finish_reason", "tool_calls")
            .put("message", message);

        JsonObject output = new JsonObject()
            .put("choices", new JsonArray().add(choice));

        JsonObject usage = new JsonObject()
            .put("input_tokens", 15)
            .put("output_tokens", 25);

        JsonObject dashscopeResponse = new JsonObject()
            .put("request_id", "req-456")
            .put("output", output)
            .put("usage", usage);

        CatholicLLMResponse response = converter.convert(dashscopeResponse);

        assertEquals("req-456", response.id());
        assertNull(response.text());
        assertTrue(response.hasToolCalls());

        var toolCalls = response.message().toolCalls();
        assertEquals(1, toolCalls.size());

        var tc = toolCalls.get(0);
        assertEquals("call_abc", tc.id());
        assertEquals("function", tc.type());
        assertEquals("get_weather", tc.function().name());
        assertEquals("{\"city\": \"Beijing\"}", tc.function().arguments());

        CatholicLLMUsage u = response.usage();
        assertEquals(40, u.totalTokens());
    }

    @Test
    void testConvertEmptyResponse() {
        JsonObject output = new JsonObject()
            .put("choices", new JsonArray());

        JsonObject dashscopeResponse = new JsonObject()
            .put("request_id", "req-empty")
            .put("output", output);

        CatholicLLMResponse response = converter.convert(dashscopeResponse);

        assertEquals("req-empty", response.id());
        assertEquals("", response.text());
    }
}