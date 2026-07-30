package io.github.sinri.keel.aigc.api.llm.openai.responses;

import io.github.sinri.keel.aigc.api.internal.openai.responses.OpenAIResponsesStreamHandler;
import io.github.sinri.keel.aigc.api.llm.catholic.CatholicLLMResponse;
import io.github.sinri.keel.aigc.api.llm.catholic.CatholicLLMResponseChunk;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OpenAIResponsesStreamHandlerTest {

    private OpenAIResponsesStreamHandler handler;

    @BeforeEach
    void setUp() {
        handler = new OpenAIResponsesStreamHandler();
    }

    @Test
    void testProcessTextStreamUntilCompleted() {
        String created = new JsonObject()
            .put("type", "response.created")
            .put("response", new JsonObject()
                .put("id", "resp_stream")
                .put("output", new JsonArray()))
            .encode();

        String delta1 = new JsonObject()
            .put("type", "response.output_text.delta")
            .put("item_id", "msg_1")
            .put("output_index", 0)
            .put("content_index", 0)
            .put("delta", "Hello")
            .encode();

        String delta2 = new JsonObject()
            .put("type", "response.output_text.delta")
            .put("output_index", 0)
            .put("content_index", 0)
            .put("delta", " world")
            .encode();

        String completed = new JsonObject()
            .put("type", "response.completed")
            .put("response", new JsonObject()
                .put("id", "resp_stream")
                .put("usage", new JsonObject()
                    .put("input_tokens", 5)
                    .put("output_tokens", 10)
                    .put("total_tokens", 15)))
            .encode();

        assertNull(handler.processSseLine("event: response.created"));
        handler.processSseLine("data: " + created);
        handler.processSseLine("data: " + delta1);
        handler.processSseLine("data: " + delta2);

        CatholicLLMResponseChunk last = handler.processSseLine("data: " + completed);
        assertNotNull(last);
        assertTrue(last.isFinished());

        CatholicLLMResponse response = handler.buildFinalResponse();
        assertEquals("resp_stream", response.id());
        assertEquals("Hello world", response.text());
        assertEquals(5, response.usage().promptTokens());
        assertEquals(10, response.usage().completionTokens());
        assertEquals(15, response.usage().totalTokens());
    }

    @Test
    void testProcessFunctionCallArgumentsStream() {
        String created = new JsonObject()
            .put("type", "response.created")
            .put("response", new JsonObject().put("id", "resp_tool"))
            .encode();

        String added = new JsonObject()
            .put("type", "response.output_item.added")
            .put("output_index", 0)
            .put("item", new JsonObject()
                .put("type", "function_call")
                .put("call_id", "call_1")
                .put("name", "get_weather"))
            .encode();

        String arg1 = new JsonObject()
            .put("type", "response.function_call_arguments.delta")
            .put("output_index", 0)
            .put("delta", "{\"location\"")
            .encode();

        String arg2 = new JsonObject()
            .put("type", "response.function_call_arguments.delta")
            .put("output_index", 0)
            .put("delta", ":\"Boston\"}")
            .encode();

        String completed = new JsonObject()
            .put("type", "response.completed")
            .put("response", new JsonObject()
                .put("id", "resp_tool")
                .put("usage", new JsonObject()
                    .put("input_tokens", 1)
                    .put("output_tokens", 2)
                    .put("total_tokens", 3)))
            .encode();

        handler.processSseLine("data: " + created);
        handler.processSseLine("data: " + added);
        handler.processSseLine("data: " + arg1);
        handler.processSseLine("data: " + arg2);
        handler.processSseLine("data: " + completed);

        CatholicLLMResponse response = handler.buildFinalResponse();
        assertEquals("resp_tool", response.id());
        assertTrue(response.hasToolCalls());
        assertEquals("call_1", response.message().toolCalls().get(0).id());
        assertEquals("get_weather", response.message().toolCalls().get(0).function().name());
        assertEquals("{\"location\":\"Boston\"}", response.message().toolCalls().get(0).function().arguments());
    }

    @Test
    void testProcessEmptyLineAndEventLine() {
        assertNull(handler.processSseLine(""));
        assertNull(handler.processSseLine("event: ping"));
    }

    @Test
    void rejectsFunctionArgumentsWithoutPrecedingMetadata() {
        String created = new JsonObject()
            .put("type", "response.created")
            .put("response", new JsonObject().put("id", "resp_tool"))
            .encode();
        String arguments = new JsonObject()
            .put("type", "response.function_call_arguments.delta")
            .put("output_index", 0)
            .put("delta", "{\"query\":\"value\"}")
            .encode();

        handler.processSseLine("data: " + created);

        IllegalStateException exception = assertThrows(
            IllegalStateException.class,
            () -> handler.processSseLine("data: " + arguments)
        );
        assertEquals(
            "function call arguments received before metadata for output index 0",
            exception.getMessage()
        );
    }

    @Test
    void testProcessNonDataLine() {
        assertNull(handler.processSseLine(": comment"));
        assertNull(handler.processSseLine("something"));
    }

    @Test
    void testProcessInvalidJson() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> handler.processSseLine("data: {invalid")
        );
        assertEquals("Invalid OpenAI Responses SSE data", exception.getMessage());
        assertNotNull(exception.getCause());
    }

    @Test
    void testErrorEventThrows() {
        String err = new JsonObject()
            .put("type", "error")
            .put("message", "boom")
            .encode();
        assertThrows(RuntimeException.class, () -> handler.processSseLine("data: " + err));
    }

    @Test
    void testReset() {
        handler.processSseLine("data: " + new JsonObject()
            .put("type", "response.output_text.delta")
            .put("output_index", 0)
            .put("delta", "A")
            .encode());

        handler.reset();

        handler.processSseLine("data: " + new JsonObject()
            .put("type", "response.created")
            .put("response", new JsonObject().put("id", "resp_second"))
            .encode());
        handler.processSseLine("data: " + new JsonObject()
            .put("type", "response.output_text.delta")
            .put("output_index", 0)
            .put("delta", "B")
            .encode());
        handler.processSseLine("data: " + new JsonObject()
            .put("type", "response.completed")
            .put("response", new JsonObject().put("id", "resp_second"))
            .encode());

        CatholicLLMResponse response = handler.buildFinalResponse();
        assertEquals("resp_second", response.id());
        assertEquals("B", response.text());
    }

    @Test
    void testCompletedWithoutUsage() {
        CatholicLLMUsageAssertions.assertUsage(
            completeWithUsage("resp_without_usage", null),
            null, null, null
        );
    }

    @Test
    void testCompletedWithEmptyUsage() {
        CatholicLLMUsageAssertions.assertUsage(
            completeWithUsage("resp_empty_usage", new JsonObject()),
            null, null, null
        );
    }

    @Test
    void testCompletedWithOnlyInputTokens() {
        CatholicLLMUsageAssertions.assertUsage(
            completeWithUsage("resp_input_only", new JsonObject().put("input_tokens", 11)),
            11, null, null
        );
    }

    @Test
    void testCompletedWithOnlyOutputTokens() {
        CatholicLLMUsageAssertions.assertUsage(
            completeWithUsage("resp_output_only", new JsonObject().put("output_tokens", 12)),
            null, 12, null
        );
    }

    @Test
    void testCompletedWithLegacyTokenAliases() {
        CatholicLLMUsageAssertions.assertUsage(
            completeWithUsage("resp_aliases", new JsonObject()
                .put("prompt_tokens", 13)
                .put("completion_tokens", 14)),
            13, 14, null
        );
    }

    private CatholicLLMResponse completeWithUsage(String responseId, JsonObject usage) {
        JsonObject response = new JsonObject().put("id", responseId);
        if (usage != null) {
            response.put("usage", usage);
        }
        CatholicLLMResponseChunk completed = handler.processSseLine("data: " + new JsonObject()
            .put("type", "response.completed")
            .put("response", response)
            .encode());
        assertNotNull(completed);
        assertTrue(completed.isFinished());
        return handler.buildFinalResponse();
    }

    private static final class CatholicLLMUsageAssertions {
        private static void assertUsage(
            CatholicLLMResponse response,
            Integer promptTokens,
            Integer completionTokens,
            Integer totalTokens
        ) {
            assertEquals(promptTokens, response.usage().promptTokens());
            assertEquals(completionTokens, response.usage().completionTokens());
            assertEquals(totalTokens, response.usage().totalTokens());
        }
    }
}
