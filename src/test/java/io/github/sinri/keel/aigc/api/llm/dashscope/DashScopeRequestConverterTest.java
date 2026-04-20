package io.github.sinri.keel.aigc.api.llm.dashscope;

import io.github.sinri.keel.aigc.api.internal.dashscope.DashScopeRequestConverter;
import io.github.sinri.keel.aigc.api.llm.catholic.CatholicLLMRequest;
import io.github.sinri.keel.aigc.api.llm.catholic.message.*;
import io.github.sinri.keel.aigc.api.llm.catholic.request.CatholicLLMRequestOptions;
import io.github.sinri.keel.aigc.api.llm.catholic.tool.CatholicTool;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DashScopeRequestConverterTest {

    private final DashScopeRequestConverter converter = new DashScopeRequestConverter();

    @Test
    void testConvertSimpleTextRequest() {
        CatholicLLMRequest request = CatholicLLMRequest.builder()
            .model("qwen-turbo")
            .addMessage(CatholicSystemMessage.of("You are a helpful assistant."))
            .addMessage(CatholicUserMessage.ofText("Hello!"))
            .build();

        JsonObject dashscopeRequest = converter.convert(request);

        assertEquals("qwen-turbo", dashscopeRequest.getString("model"));
        assertFalse(dashscopeRequest.getJsonObject("parameters").getBoolean("stream"));

        JsonObject input = dashscopeRequest.getJsonObject("input");
        JsonArray messages = input.getJsonArray("messages");
        assertEquals(2, messages.size());

        JsonObject systemMsg = messages.getJsonObject(0);
        assertEquals("system", systemMsg.getString("role"));
        assertEquals("You are a helpful assistant.", systemMsg.getString("content"));

        JsonObject userMsg = messages.getJsonObject(1);
        assertEquals("user", userMsg.getString("role"));
        assertEquals("Hello!", userMsg.getString("content"));
    }

    @Test
    void testConvertRequestWithOptions() {
        CatholicLLMRequest request = CatholicLLMRequest.builder()
            .model("qwen-max")
            .addMessage(CatholicUserMessage.ofText("Hello!"))
            .options(CatholicLLMRequestOptions.builder()
                .temperature(0.7)
                .maxTokens(1000)
                .topP(0.9)
                .stop("STOP")
                .build())
            .build();

        JsonObject dashscopeRequest = converter.convert(request);

        JsonObject parameters = dashscopeRequest.getJsonObject("parameters");
        assertEquals(0.7, parameters.getDouble("temperature"));
        assertEquals(1000, parameters.getInteger("max_tokens"));
        assertEquals(0.9, parameters.getDouble("top_p"));

        JsonArray stop = parameters.getJsonArray("stop");
        assertNotNull(stop);
        assertEquals(1, stop.size());
        assertEquals("STOP", stop.getString(0));
    }

    @Test
    void testConvertRequestWithTools() {
        JsonObject toolParams = new JsonObject()
            .put("type", "object")
            .put("properties", new JsonObject()
                .put("city", new JsonObject()
                    .put("type", "string")
                    .put("description", "City name")))
            .put("required", new JsonArray().add("city"));

        CatholicLLMRequest request = CatholicLLMRequest.builder()
            .model("qwen-max")
            .addMessage(CatholicUserMessage.ofText("What's the weather?"))
            .addTool(CatholicTool.function("get_weather", "Get weather", toolParams))
            .build();

        JsonObject dashscopeRequest = converter.convert(request);

        JsonArray tools = dashscopeRequest.getJsonObject("parameters").getJsonArray("tools");
        assertNotNull(tools);
        assertEquals(1, tools.size());

        JsonObject tool = tools.getJsonObject(0);
        assertEquals("function", tool.getString("type"));
        assertEquals("get_weather", tool.getJsonObject("function").getString("name"));
        assertEquals("Get weather", tool.getJsonObject("function").getString("description"));
    }

    @Test
    void testConvertStreamRequest() {
        CatholicLLMRequest request = CatholicLLMRequest.builder()
            .model("qwen-turbo")
            .addMessage(CatholicUserMessage.ofText("Hello!"))
            .enableStream()
            .build();

        JsonObject dashscopeRequest = converter.convert(request);

        JsonObject parameters = dashscopeRequest.getJsonObject("parameters");
        assertTrue(parameters.getBoolean("stream"));
        assertTrue(parameters.getBoolean("incremental_output"));
    }
}