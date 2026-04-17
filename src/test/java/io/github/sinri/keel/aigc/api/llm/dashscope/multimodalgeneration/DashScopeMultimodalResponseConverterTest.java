package io.github.sinri.keel.aigc.api.llm.dashscope.multimodalgeneration;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DashScopeMultimodalResponseConverterTest {

    private final DashScopeMultimodalResponseConverter converter = new DashScopeMultimodalResponseConverter();

    @Test
    void testConvertSimpleTextResponse() {
        JsonObject message = new JsonObject()
            .put("role", "assistant")
            .put("content", "Hello, how can I help you?");

        JsonObject choice = new JsonObject()
            .put("finish_reason", "stop")
            .put("message", message);

        JsonObject output = new JsonObject()
            .put("choices", new JsonArray().add(choice));

        JsonObject usage = new JsonObject()
            .put("input_tokens", 10)
            .put("output_tokens", 20)
            .put("total_tokens", 30);

        JsonObject response = new JsonObject()
            .put("request_id", "test-123")
            .put("output", output)
            .put("usage", usage);

        DashScopeMultimodalResponse result = converter.convert(response);

        assertEquals("test-123", result.catholicResponse().id());
        assertEquals("Hello, how can I help you?", result.catholicResponse().text());
        assertEquals(10, result.catholicResponse().usage().promptTokens());
        assertEquals(20, result.catholicResponse().usage().completionTokens());
        assertEquals(30, result.catholicResponse().usage().totalTokens());
        assertNull(result.reasoningContent());
    }

    @Test
    void testConvertResponseWithReasoningContent() {
        JsonObject message = new JsonObject()
            .put("role", "assistant")
            .put("content", "The answer is 42.")
            .put("reasoning_content", "Let me think about this...");

        JsonObject choice = new JsonObject()
            .put("finish_reason", "stop")
            .put("message", message);

        JsonObject output = new JsonObject()
            .put("choices", new JsonArray().add(choice));

        JsonObject usage = new JsonObject()
            .put("input_tokens", 50)
            .put("output_tokens", 100);

        JsonObject response = new JsonObject()
            .put("request_id", "test-456")
            .put("output", output)
            .put("usage", usage);

        DashScopeMultimodalResponse result = converter.convert(response);

        assertEquals("The answer is 42.", result.catholicResponse().text());
        assertEquals("Let me think about this...", result.reasoningContent());
    }

    @Test
    void testConvertResponseWithMultimodalUsage() {
        JsonObject message = new JsonObject()
            .put("role", "assistant")
            .put("content", "This image shows...");

        JsonObject choice = new JsonObject()
            .put("finish_reason", "stop")
            .put("message", message);

        JsonObject output = new JsonObject()
            .put("choices", new JsonArray().add(choice));

        JsonObject usage = new JsonObject()
            .put("input_tokens", 100)
            .put("output_tokens", 50)
            .put("total_tokens", 150)
            .put("image_tokens", 80);

        JsonObject response = new JsonObject()
            .put("request_id", "test-789")
            .put("output", output)
            .put("usage", usage);

        DashScopeMultimodalResponse result = converter.convert(response);

        assertEquals(80, result.imageTokens());
        assertNull(result.videoTokens());
        assertNull(result.audioTokens());
        assertEquals(100, result.catholicResponse().usage().promptTokens());
        assertEquals(50, result.catholicResponse().usage().completionTokens());
    }

    @Test
    void testConvertResponseWithContentArray() {
        JsonArray contentArray = new JsonArray()
            .add(new JsonObject().put("text", "Part 1"))
            .add(new JsonObject().put("text", " Part 2"));

        JsonObject message = new JsonObject()
            .put("role", "assistant")
            .put("content", contentArray);

        JsonObject choice = new JsonObject()
            .put("finish_reason", "stop")
            .put("message", message);

        JsonObject output = new JsonObject()
            .put("choices", new JsonArray().add(choice));

        JsonObject usage = new JsonObject()
            .put("input_tokens", 10)
            .put("output_tokens", 20);

        JsonObject response = new JsonObject()
            .put("request_id", "test-array")
            .put("output", output)
            .put("usage", usage);

        DashScopeMultimodalResponse result = converter.convert(response);

        // Content array should be concatenated
        assertEquals("Part 1 Part 2", result.catholicResponse().text());
    }

    @Test
    void testConvertEmptyOutput() {
        JsonObject response = new JsonObject()
            .put("request_id", "test-empty");

        DashScopeMultimodalResponse result = converter.convert(response);

        assertNotNull(result.catholicResponse());
        assertNull(result.reasoningContent());
    }
}