package io.github.sinri.keel.aigc.api.llm.dashscope.multimodalgeneration;

import io.github.sinri.keel.aigc.api.internal.dashscope.multimodalgeneration.DashScopeMultimodalRequestConverter;
import io.github.sinri.keel.aigc.api.llm.catholic.CatholicLLMRequest;
import io.github.sinri.keel.aigc.api.llm.catholic.request.CatholicLLMRequestOptions;
import io.github.sinri.keel.aigc.api.llm.catholic.message.CatholicSystemMessage;
import io.github.sinri.keel.aigc.api.llm.catholic.message.CatholicUserMessage;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DashScopeMultimodalRequestConverterTest {

    private final DashScopeMultimodalRequestConverter converter = new DashScopeMultimodalRequestConverter();

    @Test
    void testConvertSimpleTextRequest() {
        CatholicLLMRequest request = CatholicLLMRequest.builder()
            .model("qwen3-vl-plus")
            .addMessage(CatholicSystemMessage.of("You are a helpful assistant."))
            .addMessage(CatholicUserMessage.ofText("Hello"))
            .build();

        JsonObject result = converter.convert(request);

        assertEquals("qwen3-vl-plus", result.getString("model"));

        JsonObject input = result.getJsonObject("input");
        assertNotNull(input);
        JsonArray messages = input.getJsonArray("messages");
        assertEquals(2, messages.size());

        // System message
        JsonObject systemMsg = messages.getJsonObject(0);
        assertEquals("system", systemMsg.getString("role"));
        assertEquals("You are a helpful assistant.", systemMsg.getString("content"));

        // User message - simple text should be just a string
        JsonObject userMsg = messages.getJsonObject(1);
        assertEquals("user", userMsg.getString("role"));
        assertEquals("Hello", userMsg.getString("content"));
    }

    @Test
    void testConvertMultimodalContentRequest() {
        CatholicLLMRequest request = CatholicLLMRequest.builder()
            .model("qwen3-vl-plus")
            .addMessage(CatholicUserMessage.ofTextAndImage("Describe this image", "https://example.com/image.jpg"))
            .build();

        JsonObject result = converter.convert(request);

        JsonObject input = result.getJsonObject("input");
        JsonArray messages = input.getJsonArray("messages");
        JsonObject userMsg = messages.getJsonObject(0);

        assertEquals("user", userMsg.getString("role"));

        // Multimodal content should be array with DashScope native format
        Object content = userMsg.getValue("content");
        assertTrue(content instanceof io.vertx.core.json.JsonArray);

        io.vertx.core.json.JsonArray contentArray = (io.vertx.core.json.JsonArray) content;
        assertEquals(2, contentArray.size());

        // Text element uses DashScope native "text" key
        JsonObject textElement = contentArray.getJsonObject(0);
        assertEquals("Describe this image", textElement.getString("text"));

        // Image element uses DashScope native "image" key (not OpenAI's "type"/"image_url")
        JsonObject imageElement = contentArray.getJsonObject(1);
        assertEquals("https://example.com/image.jpg", imageElement.getString("image"));
    }

    @Test
    void testParametersWithResultFormat() {
        CatholicLLMRequest request = CatholicLLMRequest.builder()
            .model("qwen3-vl-plus")
            .addMessage(CatholicUserMessage.ofText("Hello"))
            .options(CatholicLLMRequestOptions.builder()
                .temperature(0.7)
                .maxTokens(1000)
                .putExtra("vl_high_resolution_images", true)
                .build())
            .build();

        JsonObject result = converter.convert(request);

        JsonObject parameters = result.getJsonObject("parameters");
        assertNotNull(parameters);
        assertEquals("message", parameters.getString("result_format"));
        assertEquals(0.7, parameters.getDouble("temperature"));
        assertEquals(1000, parameters.getInteger("max_tokens"));
        assertEquals(true, parameters.getBoolean("vl_high_resolution_images"));
    }
}