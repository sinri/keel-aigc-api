package io.github.sinri.keel.aigc.api.llm.anthropic;

import io.github.sinri.keel.aigc.api.internal.anthropic.AnthropicResponseConverter;
import io.github.sinri.keel.aigc.api.llm.catholic.CatholicLLMResponse;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AnthropicResponseConverterTest {

    private final AnthropicResponseConverter converter = new AnthropicResponseConverter();

    @Test
    void testTextOnly() {
        JsonObject resp = new JsonObject()
            .put("id", "msg_1")
            .put("type", "message")
            .put("role", "assistant")
            .put("content", new JsonArray()
                .add(new JsonObject().put("type", "text").put("text", "Hello")))
            .put("usage", new JsonObject()
                .put("input_tokens", 3)
                .put("output_tokens", 2));

        CatholicLLMResponse r = converter.convert(resp);
        assertEquals("msg_1", r.id());
        assertEquals("Hello", r.text());
        assertFalse(r.hasToolCalls());
        assertEquals(3, r.usage().promptTokens());
        assertEquals(2, r.usage().completionTokens());
        assertEquals(5, r.usage().totalTokens());
    }

    @Test
    void testToolUse() {
        JsonObject resp = new JsonObject()
            .put("id", "msg_2")
            .put("content", new JsonArray()
                .add(new JsonObject()
                    .put("type", "tool_use")
                    .put("id", "toolu_x")
                    .put("name", "f")
                    .put("input", new JsonObject().put("a", 1))));

        CatholicLLMResponse r = converter.convert(resp);
        assertTrue(r.hasToolCalls());
        assertEquals("toolu_x", r.message().toolCalls().get(0).id());
        assertEquals("f", r.message().toolCalls().get(0).function().name());
        assertTrue(r.message().toolCalls().get(0).function().arguments().contains("\"a\":1"));
    }

    @Test
    void testEmptyContent() {
        JsonObject resp = new JsonObject()
            .put("id", "msg_e")
            .put("content", new JsonArray());

        CatholicLLMResponse r = converter.convert(resp);
        assertEquals("", r.text());
    }
}
