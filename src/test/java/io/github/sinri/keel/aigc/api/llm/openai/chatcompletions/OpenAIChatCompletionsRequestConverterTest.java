package io.github.sinri.keel.aigc.api.llm.openai.chatcompletions;

import io.github.sinri.keel.aigc.api.llm.catholic.CatholicLLMRequest;
import io.github.sinri.keel.aigc.api.llm.catholic.message.*;
import io.github.sinri.keel.aigc.api.llm.catholic.request.CatholicLLMRequestOptions;
import io.github.sinri.keel.aigc.api.llm.catholic.tool.CatholicTool;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * OpenAIChatCompletionsRequestConverter 测试类
 */
class OpenAIChatCompletionsRequestConverterTest {

    private final OpenAIChatCompletionsRequestConverter converter = new OpenAIChatCompletionsRequestConverter();

    @Test
    void testConvertSimpleTextRequest() {
        CatholicLLMRequest request = CatholicLLMRequest.builder()
            .model("gpt-4o")
            .addMessage(CatholicSystemMessage.of("You are a helpful assistant."))
            .addMessage(CatholicUserMessage.ofText("Hello!"))
            .build();

        JsonObject openaiRequest = converter.convert(request);

        assertEquals("gpt-4o", openaiRequest.getString("model"));
        assertFalse(openaiRequest.getBoolean("stream"));

        JsonArray messages = openaiRequest.getJsonArray("messages");
        assertEquals(2, messages.size());

        JsonObject systemMsg = messages.getJsonObject(0);
        assertEquals("system", systemMsg.getString("role"));
        assertEquals("You are a helpful assistant.", systemMsg.getString("content"));

        JsonObject userMsg = messages.getJsonObject(1);
        assertEquals("user", userMsg.getString("role"));
        assertEquals("Hello!", userMsg.getString("content"));
    }

    @Test
    void testConvertMultimodalRequest() {
        CatholicLLMRequest request = CatholicLLMRequest.builder()
            .model("gpt-4o")
            .addMessage(CatholicUserMessage.ofTextAndImage("What's in this image?", "https://example.com/image.png"))
            .build();

        JsonObject openaiRequest = converter.convert(request);

        JsonArray messages = openaiRequest.getJsonArray("messages");
        assertEquals(1, messages.size());

        JsonObject userMsg = messages.getJsonObject(0);
        assertEquals("user", userMsg.getString("role"));

        JsonArray content = userMsg.getJsonArray("content");
        assertEquals(2, content.size());

        JsonObject textPart = content.getJsonObject(0);
        assertEquals("text", textPart.getString("type"));
        assertEquals("What's in this image?", textPart.getString("text"));

        JsonObject imagePart = content.getJsonObject(1);
        assertEquals("image_url", imagePart.getString("type"));
        assertEquals("https://example.com/image.png", imagePart.getJsonObject("image_url").getString("url"));
    }

    @Test
    void testConvertRequestWithTools() {
        JsonObject toolParams = new JsonObject()
            .put("type", "object")
            .put("properties", new JsonObject()
                .put("location", new JsonObject()
                    .put("type", "string")
                    .put("description", "City name")));

        CatholicLLMRequest request = CatholicLLMRequest.builder()
            .model("gpt-4o")
            .addMessage(CatholicUserMessage.ofText("What's the weather in Beijing?"))
            .addTool(CatholicTool.function("get_weather", "Get weather info", toolParams))
            .build();

        JsonObject openaiRequest = converter.convert(request);

        JsonArray tools = openaiRequest.getJsonArray("tools");
        assertNotNull(tools);
        assertEquals(1, tools.size());

        JsonObject tool = tools.getJsonObject(0);
        assertEquals("function", tool.getString("type"));
        assertEquals("get_weather", tool.getJsonObject("function").getString("name"));
        assertEquals("Get weather info", tool.getJsonObject("function").getString("description"));
    }

    @Test
    void testConvertRequestWithOptions() {
        CatholicLLMRequest request = CatholicLLMRequest.builder()
            .model("gpt-4o")
            .addMessage(CatholicUserMessage.ofText("Hello!"))
            .options(CatholicLLMRequestOptions.builder()
                .temperature(0.7)
                .maxTokens(1000)
                .topP(0.9)
                .stop("STOP")
                .build())
            .build();

        JsonObject openaiRequest = converter.convert(request);

        assertEquals(0.7, openaiRequest.getDouble("temperature"));
        assertEquals(1000, openaiRequest.getInteger("max_tokens"));
        assertEquals(0.9, openaiRequest.getDouble("top_p"));

        JsonArray stop = openaiRequest.getJsonArray("stop");
        assertNotNull(stop);
        assertEquals(1, stop.size());
        assertEquals("STOP", stop.getString(0));
    }

    @Test
    void testConvertAssistantMessageWithToolCalls() {
        CatholicLLMRequest request = CatholicLLMRequest.builder()
            .model("gpt-4o")
            .addMessage(CatholicUserMessage.ofText("What's the weather?"))
            .addMessage(new CatholicAssistantMessage(
                null,
                java.util.List.of(new io.github.sinri.keel.aigc.api.llm.catholic.tool.CatholicToolCall(
                    "call_123",
                    "function",
                    new io.github.sinri.keel.aigc.api.llm.catholic.tool.CatholicToolCall.CatholicToolCallFunction(
                        "get_weather",
                        "{\"location\": \"Beijing\"}"
                    )
                ))
            ))
            .addMessage(CatholicToolCallMessage.of("call_123", "{\"temp\": 25}"))
            .build();

        JsonObject openaiRequest = converter.convert(request);

        JsonArray messages = openaiRequest.getJsonArray("messages");
        assertEquals(3, messages.size());

        // assistant message with tool calls
        JsonObject assistantMsg = messages.getJsonObject(1);
        assertEquals("assistant", assistantMsg.getString("role"));
        assertNull(assistantMsg.getString("content"));

        JsonArray toolCalls = assistantMsg.getJsonArray("tool_calls");
        assertNotNull(toolCalls);
        assertEquals(1, toolCalls.size());

        JsonObject toolCall = toolCalls.getJsonObject(0);
        assertEquals("call_123", toolCall.getString("id"));
        assertEquals("function", toolCall.getString("type"));
        assertEquals("get_weather", toolCall.getJsonObject("function").getString("name"));

        // tool message
        JsonObject toolMsg = messages.getJsonObject(2);
        assertEquals("tool", toolMsg.getString("role"));
        assertEquals("call_123", toolMsg.getString("tool_call_id"));
        assertEquals("{\"temp\": 25}", toolMsg.getString("content"));
    }

    @Test
    void testConvertBase64ImageRequest() {
        CatholicLLMRequest request = CatholicLLMRequest.builder()
            .model("gpt-4o")
            .addMessage(CatholicUserMessage.ofTextAndBase64Image("Analyze this", "image/png", "iVBORw0KGgo..."))
            .build();

        JsonObject openaiRequest = converter.convert(request);

        JsonArray messages = openaiRequest.getJsonArray("messages");
        JsonObject userMsg = messages.getJsonObject(0);
        JsonArray content = userMsg.getJsonArray("content");

        JsonObject imagePart = content.getJsonObject(1);
        assertEquals("image_url", imagePart.getString("type"));
        assertTrue(imagePart.getJsonObject("image_url").getString("url").startsWith("data:image/png;base64,"));
    }
}