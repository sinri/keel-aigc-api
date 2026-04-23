package io.github.sinri.keel.aigc.api.llm.anthropic;

import io.github.sinri.keel.aigc.api.internal.anthropic.AnthropicRequestConverter;
import io.github.sinri.keel.aigc.api.llm.catholic.CatholicLLMRequest;
import io.github.sinri.keel.aigc.api.llm.catholic.message.CatholicAssistantMessage;
import io.github.sinri.keel.aigc.api.llm.catholic.message.CatholicSystemMessage;
import io.github.sinri.keel.aigc.api.llm.catholic.message.CatholicToolCallMessage;
import io.github.sinri.keel.aigc.api.llm.catholic.message.CatholicUserMessage;
import io.github.sinri.keel.aigc.api.llm.catholic.request.CatholicLLMRequestOptions;
import io.github.sinri.keel.aigc.api.llm.catholic.tool.definition.CatholicToolDefinition;
import io.github.sinri.keel.aigc.api.llm.catholic.tool.call.CatholicFunctionToolCall;
import io.github.sinri.keel.aigc.api.llm.catholic.tool.call.FunctionCall;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AnthropicRequestConverterTest {

    private final AnthropicRequestConverter converter = new AnthropicRequestConverter();

    @Test
    void testSystemAndUser() {
        CatholicLLMRequest request = CatholicLLMRequest.builder()
            .model("claude-3-5-haiku-20241022")
            .addMessage(CatholicSystemMessage.of("Be helpful."))
            .addMessage(CatholicUserMessage.ofText("Hi"))
            .build();

        JsonObject body = converter.convert(request);
        assertEquals("claude-3-5-haiku-20241022", body.getString("model"));
        assertEquals("Be helpful.", body.getString("system"));
        assertEquals(4096, body.getInteger("max_tokens"));

        JsonArray messages = body.getJsonArray("messages");
        assertEquals(1, messages.size());
        assertEquals("user", messages.getJsonObject(0).getString("role"));
        assertEquals("Hi", messages.getJsonObject(0).getString("content"));
    }

    @Test
    void testMultimodalUser() {
        CatholicLLMRequest request = CatholicLLMRequest.builder()
            .model("claude-3-5-haiku-20241022")
            .addMessage(CatholicUserMessage.ofTextAndImage("What?", "https://example.com/a.png"))
            .build();

        JsonArray content = converter.convert(request).getJsonArray("messages").getJsonObject(0).getJsonArray("content");
        assertEquals(2, content.size());
        assertEquals("text", content.getJsonObject(0).getString("type"));
        assertEquals("image", content.getJsonObject(1).getString("type"));
        assertEquals("url", content.getJsonObject(1).getJsonObject("source").getString("type"));
    }

    @Test
    void testTools() {
        JsonObject schema = new JsonObject().put("type", "object").put("properties", new JsonObject());
        CatholicLLMRequest request = CatholicLLMRequest.builder()
            .model("claude-3-5-haiku-20241022")
            .addMessage(CatholicUserMessage.ofText("Go"))
            .addTool(CatholicToolDefinition.function("f", "d", schema))
            .build();

        JsonArray tools = converter.convert(request).getJsonArray("tools");
        assertEquals(1, tools.size());
        assertEquals("f", tools.getJsonObject(0).getString("name"));
        assertEquals(schema, tools.getJsonObject(0).getJsonObject("input_schema"));
    }

    @Test
    void testOptions() {
        CatholicLLMRequest request = CatholicLLMRequest.builder()
            .model("claude-3-5-haiku-20241022")
            .addMessage(CatholicUserMessage.ofText("x"))
            .options(CatholicLLMRequestOptions.builder()
                .maxTokens(256)
                .temperature(0.5)
                .topP(0.9)
                .stop("END")
                .build())
            .build();

        JsonObject body = converter.convert(request);
        assertEquals(256, body.getInteger("max_tokens"));
        assertEquals(0.5, body.getDouble("temperature"));
        assertEquals(0.9, body.getDouble("top_p"));
        assertEquals("END", body.getJsonArray("stop_sequences").getString(0));
    }

    @Test
    void testAssistantToolUseAndToolResult() {
        CatholicLLMRequest request = CatholicLLMRequest.builder()
            .model("claude-3-5-haiku-20241022")
            .addMessage(CatholicUserMessage.ofText("Weather?"))
            .addMessage(new CatholicAssistantMessage(
                null,
                java.util.List.of(new CatholicFunctionToolCall(
                    "toolu_01",
                    "function",
                    new FunctionCall("get_weather", "{\"city\":\"NYC\"}")
                ))
            ))
            .addMessage(CatholicToolCallMessage.of("toolu_01", "{\"t\":1}"))
            .build();

        JsonArray messages = converter.convert(request).getJsonArray("messages");
        JsonObject assistant = messages.getJsonObject(1);
        assertEquals("assistant", assistant.getString("role"));
        JsonArray ac = assistant.getJsonArray("content");
        assertEquals(1, ac.size());
        assertEquals("tool_use", ac.getJsonObject(0).getString("type"));
        assertEquals("NYC", ac.getJsonObject(0).getJsonObject("input").getString("city"));

        JsonObject toolUser = messages.getJsonObject(2);
        JsonArray tc = toolUser.getJsonArray("content");
        assertEquals("tool_result", tc.getJsonObject(0).getString("type"));
        assertEquals("toolu_01", tc.getJsonObject(0).getString("tool_use_id"));
    }
}
