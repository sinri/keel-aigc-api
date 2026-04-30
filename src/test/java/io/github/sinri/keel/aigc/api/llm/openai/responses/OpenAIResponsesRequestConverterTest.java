package io.github.sinri.keel.aigc.api.llm.openai.responses;

import io.github.sinri.keel.aigc.api.internal.openai.responses.OpenAIResponsesRequestConverter;
import io.github.sinri.keel.aigc.api.llm.catholic.CatholicLLMRequest;
import io.github.sinri.keel.aigc.api.llm.catholic.message.CatholicAssistantMessage;
import io.github.sinri.keel.aigc.api.llm.catholic.message.CatholicSystemMessage;
import io.github.sinri.keel.aigc.api.llm.catholic.message.CatholicToolCallMessage;
import io.github.sinri.keel.aigc.api.llm.catholic.message.CatholicUserMessage;
import io.github.sinri.keel.aigc.api.llm.catholic.request.CatholicLLMRequestOptions;
import io.github.sinri.keel.aigc.api.llm.catholic.tool.definition.CatholicToolDefinition;
import io.github.sinri.keel.aigc.api.llm.catholic.tool.definition.function.FunctionDefinition;
import io.github.sinri.keel.aigc.api.llm.catholic.tool.call.CatholicFunctionToolCallImpl;
import io.github.sinri.keel.aigc.api.llm.catholic.tool.call.FunctionCall;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OpenAIResponsesRequestConverterTest {

    private final OpenAIResponsesRequestConverter converter = new OpenAIResponsesRequestConverter();

    @Test
    void testConvertSimpleTextRequest() {
        CatholicLLMRequest request = CatholicLLMRequest.builder()
            .model("gpt-4.1")
            .addMessage(CatholicSystemMessage.of("You are a helpful assistant."))
            .addMessage(CatholicUserMessage.ofText("Hello!"))
            .build();

        JsonObject body = converter.convert(request);

        assertEquals("gpt-4.1", body.getString("model"));
        assertNull(body.getBoolean("stream"));

        JsonArray input = body.getJsonArray("input");
        assertEquals(2, input.size());

        JsonObject systemMsg = input.getJsonObject(0);
        assertEquals("message", systemMsg.getString("type"));
        assertEquals("system", systemMsg.getString("role"));
        assertEquals("You are a helpful assistant.", systemMsg.getString("content"));

        JsonObject userMsg = input.getJsonObject(1);
        assertEquals("message", userMsg.getString("type"));
        assertEquals("user", userMsg.getString("role"));
        assertEquals("Hello!", userMsg.getString("content"));
    }

    @Test
    void testConvertMultimodalUserRequest() {
        CatholicLLMRequest request = CatholicLLMRequest.builder()
            .model("gpt-4.1")
            .addMessage(CatholicUserMessage.ofTextAndImage("What's in this image?", "https://example.com/image.png"))
            .build();

        JsonObject body = converter.convert(request);
        JsonArray input = body.getJsonArray("input");
        assertEquals(1, input.size());

        JsonObject userMsg = input.getJsonObject(0);
        JsonArray content = userMsg.getJsonArray("content");
        assertEquals(2, content.size());
        assertEquals("input_text", content.getJsonObject(0).getString("type"));
        assertEquals("What's in this image?", content.getJsonObject(0).getString("text"));
        assertEquals("input_image", content.getJsonObject(1).getString("type"));
        assertEquals("https://example.com/image.png", content.getJsonObject(1).getString("image_url"));
    }

    @Test
    void testConvertTools() {
        JsonObject toolParams = new JsonObject()
            .put("type", "object")
            .put("properties", new JsonObject()
                .put("location", new JsonObject()
                    .put("type", "string")
                    .put("description", "City name")));

        CatholicLLMRequest request = CatholicLLMRequest.builder()
            .model("gpt-4.1")
            .addMessage(CatholicUserMessage.ofText("Weather?"))
            .addTool(CatholicToolDefinition.function(FunctionDefinition.of("get_weather", "Get weather info", toolParams)))
            .build();

        JsonObject body = converter.convert(request);
        JsonArray tools = body.getJsonArray("tools");
        assertNotNull(tools);
        assertEquals(1, tools.size());

        JsonObject tool = tools.getJsonObject(0);
        assertEquals("function", tool.getString("type"));
        assertEquals("get_weather", tool.getString("name"));
        assertEquals("Get weather info", tool.getString("description"));
        assertEquals(toolParams, tool.getJsonObject("parameters"));
    }

    @Test
    void testConvertOptions() {
        CatholicLLMRequest request = CatholicLLMRequest.builder()
            .model("gpt-4.1")
            .addMessage(CatholicUserMessage.ofText("Hi"))
            .options(CatholicLLMRequestOptions.builder()
                .temperature(0.7)
                .maxTokens(1000)
                .topP(0.9)
                .build())
            .build();

        JsonObject body = converter.convert(request);
        assertEquals(0.7, body.getDouble("temperature"));
        assertEquals(1000, body.getInteger("max_output_tokens"));
        assertEquals(0.9, body.getDouble("top_p"));
    }

    @Test
    void testConvertAssistantWithToolCallsAndToolOutput() {
        CatholicLLMRequest request = CatholicLLMRequest.builder()
            .model("gpt-4.1")
            .addMessage(CatholicUserMessage.ofText("What's the weather?"))
            .addMessage(new CatholicAssistantMessage(
                null,
                java.util.List.of(new CatholicFunctionToolCallImpl(
                    "call_123",
                    new FunctionCall("get_weather", "{\"location\": \"Beijing\"}")
                ))
            ))
            .addMessage(CatholicToolCallMessage.of("call_123", "{\"temp\":25}"))
            .build();

        JsonObject body = converter.convert(request);
        JsonArray input = body.getJsonArray("input");
        assertEquals(3, input.size());

        JsonObject fc = input.getJsonObject(1);
        assertEquals("function_call", fc.getString("type"));
        assertEquals("call_123", fc.getString("call_id"));
        assertEquals("get_weather", fc.getString("name"));
        assertEquals("{\"location\": \"Beijing\"}", fc.getString("arguments"));

        JsonObject out = input.getJsonObject(2);
        assertEquals("function_call_output", out.getString("type"));
        assertEquals("call_123", out.getString("call_id"));
        assertEquals("{\"temp\":25}", out.getString("output"));
    }

    @Test
    void testConvertBase64ImageRequest() {
        CatholicLLMRequest request = CatholicLLMRequest.builder()
            .model("gpt-4.1")
            .addMessage(CatholicUserMessage.ofTextAndBase64Image("Analyze this", "image/png", "iVBORw0KGgo"))
            .build();

        JsonObject body = converter.convert(request);
        JsonArray content = body.getJsonArray("input").getJsonObject(0).getJsonArray("content");
        JsonObject imagePart = content.getJsonObject(1);
        assertEquals("input_image", imagePart.getString("type"));
        assertTrue(imagePart.getString("image_url").startsWith("data:image/png;base64,"));
    }

    @Test
    void testConvertAssistantTextAndToolCalls() {
        CatholicLLMRequest request = CatholicLLMRequest.builder()
            .model("gpt-4.1")
            .addMessage(CatholicUserMessage.ofText("Go"))
            .addMessage(CatholicAssistantMessage.ofMixed(
                "Checking.",
                java.util.List.of(new CatholicFunctionToolCallImpl(
                    "call_1",
                    new FunctionCall("f", "{}")
                ))
            ))
            .build();

        JsonArray input = converter.convert(request).getJsonArray("input");
        assertEquals(3, input.size());
        assertEquals("assistant", input.getJsonObject(1).getString("role"));
        assertEquals("Checking.", input.getJsonObject(1).getString("content"));
        assertEquals("function_call", input.getJsonObject(2).getString("type"));
    }
}
