package io.github.sinri.keel.aigc.api.llm.openai.responses;

import io.github.sinri.keel.aigc.api.llm.catholic.CatholicLLMResponse;
import io.github.sinri.keel.aigc.api.llm.catholic.response.CatholicLLMUsage;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OpenAIResponsesResponseConverterTest {

    private final OpenAIResponsesResponseConverter converter = new OpenAIResponsesResponseConverter();

    @Test
    void testConvertSimpleAssistantMessage() {
        JsonObject responseJson = new JsonObject()
            .put("id", "resp_123")
            .put("output", new JsonArray()
                .add(new JsonObject()
                    .put("type", "message")
                    .put("id", "msg_1")
                    .put("role", "assistant")
                    .put("status", "completed")
                    .put("content", new JsonArray()
                        .add(new JsonObject()
                            .put("type", "output_text")
                            .put("text", "Hello!")
                            .put("annotations", new JsonArray())))))
            .put("usage", new JsonObject()
                .put("input_tokens", 10)
                .put("output_tokens", 20)
                .put("total_tokens", 30));

        CatholicLLMResponse response = converter.convert(responseJson);

        assertEquals("resp_123", response.id());
        assertEquals("Hello!", response.text());
        assertFalse(response.hasToolCalls());

        CatholicLLMUsage usage = response.usage();
        assertEquals(10, usage.promptTokens());
        assertEquals(20, usage.completionTokens());
        assertEquals(30, usage.totalTokens());
    }

    @Test
    void testConvertFunctionCallOnly() {
        JsonObject responseJson = new JsonObject()
            .put("id", "resp_fc")
            .put("output", new JsonArray()
                .add(new JsonObject()
                    .put("type", "function_call")
                    .put("id", "fc_1")
                    .put("call_id", "call_abc")
                    .put("name", "get_weather")
                    .put("arguments", "{\"location\":\"Boston\"}")
                    .put("status", "completed")))
            .put("usage", new JsonObject()
                .put("prompt_tokens", 1)
                .put("completion_tokens", 2)
                .put("total_tokens", 3));

        CatholicLLMResponse response = converter.convert(responseJson);

        assertEquals("resp_fc", response.id());
        assertNull(response.text());
        assertTrue(response.hasToolCalls());
        assertEquals("call_abc", response.message().toolCalls().get(0).id());
        assertEquals("get_weather", response.message().toolCalls().get(0).function().name());
        assertEquals("{\"location\":\"Boston\"}", response.message().toolCalls().get(0).function().arguments());
    }

    @Test
    void testConvertMessageAndFunctionCall() {
        JsonObject responseJson = new JsonObject()
            .put("id", "resp_mix")
            .put("output", new JsonArray()
                .add(new JsonObject()
                    .put("type", "message")
                    .put("role", "assistant")
                    .put("content", new JsonArray()
                        .add(new JsonObject()
                            .put("type", "output_text")
                            .put("text", "OK")
                            .put("annotations", new JsonArray()))))
                .add(new JsonObject()
                    .put("type", "function_call")
                    .put("call_id", "call_x")
                    .put("name", "f")
                    .put("arguments", "{}")));

        CatholicLLMResponse response = converter.convert(responseJson);
        assertEquals("OK", response.text());
        assertTrue(response.hasToolCalls());
        assertEquals("call_x", response.message().toolCalls().get(0).id());
    }

    @Test
    void testConvertEmptyOutput() {
        JsonObject responseJson = new JsonObject()
            .put("id", "resp_empty")
            .put("output", new JsonArray());

        CatholicLLMResponse response = converter.convert(responseJson);
        assertEquals("resp_empty", response.id());
        assertEquals("", response.text());
        assertFalse(response.hasToolCalls());
    }

    @Test
    void testConvertWithoutUsage() {
        JsonObject responseJson = new JsonObject()
            .put("id", "resp_nu")
            .put("output", new JsonArray()
                .add(new JsonObject()
                    .put("type", "message")
                    .put("role", "assistant")
                    .put("content", new JsonArray()
                        .add(new JsonObject()
                            .put("type", "output_text")
                            .put("text", "Hi")
                            .put("annotations", new JsonArray())))));

        CatholicLLMResponse response = converter.convert(responseJson);
        CatholicLLMUsage usage = response.usage();
        assertNull(usage.promptTokens());
        assertNull(usage.completionTokens());
        assertNull(usage.totalTokens());
    }

    @Test
    void testConvertRefusalPart() {
        JsonObject responseJson = new JsonObject()
            .put("id", "resp_ref")
            .put("output", new JsonArray()
                .add(new JsonObject()
                    .put("type", "message")
                    .put("role", "assistant")
                    .put("content", new JsonArray()
                        .add(new JsonObject()
                            .put("type", "refusal")
                            .put("refusal", "No.")))));

        CatholicLLMResponse response = converter.convert(responseJson);
        assertEquals("No.", response.text());
    }
}
