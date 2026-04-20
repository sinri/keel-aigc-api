package io.github.sinri.keel.aigc.api.llm.anthropic;

import io.github.sinri.keel.aigc.api.internal.anthropic.AnthropicStreamHandler;
import io.github.sinri.keel.aigc.api.llm.catholic.CatholicLLMResponse;
import io.github.sinri.keel.aigc.api.llm.catholic.CatholicLLMResponseChunk;
import io.vertx.core.json.JsonObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AnthropicStreamHandlerTest {

    private AnthropicStreamHandler handler;

    @BeforeEach
    void setUp() {
        handler = new AnthropicStreamHandler();
    }

    @Test
    void testTextStream() {
        String start = new JsonObject()
            .put("type", "message_start")
            .put("message", new JsonObject()
                .put("id", "msg_s")
                .put("usage", new JsonObject().put("input_tokens", 10)))
            .encode();

        String d1 = new JsonObject()
            .put("type", "content_block_delta")
            .put("index", 0)
            .put("delta", new JsonObject().put("type", "text_delta").put("text", "Hi"))
            .encode();

        String md = new JsonObject()
            .put("type", "message_delta")
            .put("usage", new JsonObject().put("output_tokens", 5))
            .encode();

        String stop = new JsonObject().put("type", "message_stop").encode();

        handler.processSseLine("data: " + start);
        handler.processSseLine("data: " + d1);
        handler.processSseLine("data: " + md);
        CatholicLLMResponseChunk last = handler.processSseLine("data: " + stop);
        assertNotNull(last);
        assertTrue(last.isFinished());

        CatholicLLMResponse r = handler.buildFinalResponse();
        assertEquals("msg_s", r.id());
        assertEquals("Hi", r.text());
        assertEquals(10, r.usage().promptTokens());
        assertEquals(5, r.usage().completionTokens());
    }

    @Test
    void testToolInputJsonStream() {
        handler.processSseLine("data: " + new JsonObject()
            .put("type", "message_start")
            .put("message", new JsonObject().put("id", "msg_t"))
            .encode());

        handler.processSseLine("data: " + new JsonObject()
            .put("type", "content_block_start")
            .put("index", 0)
            .put("content_block", new JsonObject()
                .put("type", "tool_use")
                .put("id", "toolu_1")
                .put("name", "get_x"))
            .encode());

        handler.processSseLine("data: " + new JsonObject()
            .put("type", "content_block_delta")
            .put("index", 0)
            .put("delta", new JsonObject().put("type", "input_json_delta").put("partial_json", "{\"a\":"))
            .encode());

        handler.processSseLine("data: " + new JsonObject()
            .put("type", "content_block_delta")
            .put("index", 0)
            .put("delta", new JsonObject().put("type", "input_json_delta").put("partial_json", "1}"))
            .encode());

        handler.processSseLine("data: " + new JsonObject()
            .put("type", "message_delta")
            .put("usage", new JsonObject().put("output_tokens", 3))
            .encode());

        handler.processSseLine("data: " + new JsonObject().put("type", "message_stop").encode());

        CatholicLLMResponse r = handler.buildFinalResponse();
        assertTrue(r.hasToolCalls());
        assertEquals("toolu_1", r.message().toolCalls().get(0).id());
        assertEquals("get_x", r.message().toolCalls().get(0).function().name());
        assertEquals("{\"a\":1}", r.message().toolCalls().get(0).function().arguments());
    }

    @Test
    void testErrorThrows() {
        String err = new JsonObject()
            .put("type", "error")
            .put("error", new JsonObject().put("type", "api_error").put("message", "bad"))
            .encode();
        assertThrows(RuntimeException.class, () -> handler.processSseLine("data: " + err));
    }
}
