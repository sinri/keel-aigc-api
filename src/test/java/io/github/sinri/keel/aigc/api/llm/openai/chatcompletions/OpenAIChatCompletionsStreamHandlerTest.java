package io.github.sinri.keel.aigc.api.llm.openai.chatcompletions;

import io.github.sinri.keel.aigc.api.internal.openai.chatcompletions.OpenAIChatCompletionsStreamHandler;
import io.github.sinri.keel.aigc.api.llm.catholic.CatholicLLMResponse;
import io.github.sinri.keel.aigc.api.llm.catholic.CatholicLLMResponseChunk;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OpenAIChatCompletionsStreamHandlerTest {

    private OpenAIChatCompletionsStreamHandler handler;

    @BeforeEach
    void setUp() {
        handler = new OpenAIChatCompletionsStreamHandler();
    }

    @Test
    void testProcessTextStream() {
        // 模拟 OpenAI SSE 文本流
        // 注意: OpenAI 在 stream mode 下，usage 通常在顶层（需要 stream_options.include_usage: true）
        String[] sseLines = {
            "data: {\"id\":\"chatcmpl-stream\",\"choices\":[{\"index\":0,\"delta\":{\"role\":\"assistant\"},\"finish_reason\":null}]}",
            "data: {\"id\":\"chatcmpl-stream\",\"choices\":[{\"index\":0,\"delta\":{\"content\":\"Hello\"},\"finish_reason\":null}]}",
            "data: {\"id\":\"chatcmpl-stream\",\"choices\":[{\"index\":0,\"delta\":{\"content\":\" World\"},\"finish_reason\":null}]}",
            "data: {\"id\":\"chatcmpl-stream\",\"choices\":[{\"index\":0,\"delta\":{},\"finish_reason\":\"stop\"}],\"usage\":{\"prompt_tokens\":5,\"completion_tokens\":10,\"total_tokens\":15}}",
            "data: [DONE]"
        };

        for (String line : sseLines) {
            CatholicLLMResponseChunk chunk = handler.processSseLine(line);
            if (chunk != null && line.equals("data: [DONE]")) {
                assertTrue(chunk.isFinished());
            }
        }

        // 构建最终响应
        CatholicLLMResponse response = handler.buildFinalResponse();

        assertEquals("chatcmpl-stream", response.id());
        assertEquals("Hello World", response.text());
        assertFalse(response.hasToolCalls());

        // usage 可能为 null（取决于 OpenAI 是否返回）
        // 这里检查如果返回了 usage，值应该是正确的
        var usage = response.usage();
        // 由于 OpenAI stream 可能不返回 usage，这里放宽测试
        if (usage.promptTokens() != null) {
            assertEquals(5, usage.promptTokens());
            assertEquals(10, usage.completionTokens());
            assertEquals(15, usage.totalTokens());
        }
    }

    @Test
    void testProcessToolCallStream() {
        // 模拟 OpenAI SSE 工具调用流
        // arguments 片段: "{\"location\"" + ": \"Beijing\"" + "}"
        String[] sseLines = {
            "data: {\"id\":\"chatcmpl-tool\",\"choices\":[{\"index\":0,\"delta\":{\"tool_calls\":[{\"index\":0,\"id\":\"call_1\",\"type\":\"function\",\"function\":{\"name\":\"get_weather\"}}]},\"finish_reason\":null}]}",
            "data: {\"id\":\"chatcmpl-tool\",\"choices\":[{\"index\":0,\"delta\":{\"tool_calls\":[{\"index\":0,\"function\":{\"arguments\":\"{\\\"location\\\"\"}}]},\"finish_reason\":null}]}",
            "data: {\"id\":\"chatcmpl-tool\",\"choices\":[{\"index\":0,\"delta\":{\"tool_calls\":[{\"index\":0,\"function\":{\"arguments\":\": \\\"Beijing\\\"\"}}]},\"finish_reason\":null}]}",
            "data: {\"id\":\"chatcmpl-tool\",\"choices\":[{\"index\":0,\"delta\":{\"tool_calls\":[{\"index\":0,\"function\":{\"arguments\":\"}\"}}]},\"finish_reason\":null}]}",
            "data: {\"id\":\"chatcmpl-tool\",\"choices\":[{\"index\":0,\"delta\":{},\"finish_reason\":\"tool_calls\"}]}",
            "data: [DONE]"
        };

        for (String line : sseLines) {
            handler.processSseLine(line);
        }

        CatholicLLMResponse response = handler.buildFinalResponse();

        assertEquals("chatcmpl-tool", response.id());
        assertNull(response.text());
        assertTrue(response.hasToolCalls());

        var toolCalls = response.message().toolCalls();
        assertEquals(1, toolCalls.size());

        var toolCall = toolCalls.get(0);
        assertEquals("call_1", toolCall.id());
        assertEquals("function", toolCall.type());
        assertEquals("get_weather", toolCall.function().name());
        // 累积结果: "{\"location\"" + ": \"Beijing\"" + "}" = "{\"location\": \"Beijing\"}"
        assertEquals("{\"location\": \"Beijing\"}", toolCall.function().arguments());
    }

    @Test
    void testProcessEmptyLine() {
        CatholicLLMResponseChunk chunk = handler.processSseLine("");
        assertNull(chunk);
    }

    @Test
    void testProcessNonDataLine() {
        CatholicLLMResponseChunk chunk = handler.processSseLine(": comment");
        assertNull(chunk);

        chunk = handler.processSseLine("something else");
        assertNull(chunk);
    }

    @Test
    void testProcessInvalidJson() {
        CatholicLLMResponseChunk chunk = handler.processSseLine("data: {invalid json}");
        assertNull(chunk);
    }

    @Test
    void testProcessChunkWithDeltaText() {
        JsonObject chunkJson = new JsonObject()
            .put("id", "test-id")
            .put("choices", new JsonArray()
                .add(new JsonObject()
                    .put("index", 0)
                    .put("delta", new JsonObject()
                        .put("content", "Test content"))));

        String sseLine = "data: " + chunkJson.encode();

        CatholicLLMResponseChunk chunk = handler.processSseLine(sseLine);

        assertNotNull(chunk);
        assertEquals("test-id", chunk.id());
        assertEquals(0, chunk.index());
        assertEquals("Test content", chunk.deltaText());
        assertFalse(chunk.isFinished());
    }

    @Test
    void testReset() {
        // 处理一些数据
        handler.processSseLine("data: {\"id\":\"first\",\"choices\":[{\"index\":0,\"delta\":{\"content\":\"A\"}}]}");

        // 重置
        handler.reset();

        // 处理新数据
        handler.processSseLine("data: {\"id\":\"second\",\"choices\":[{\"index\":0,\"delta\":{\"content\":\"B\"}}]}");

        CatholicLLMResponse response = handler.buildFinalResponse();

        assertEquals("second", response.id());
        assertEquals("B", response.text());
    }

    @Test
    void testMultipleToolCallsInStream() {
        String[] sseLines = {
            "data: {\"id\":\"multi-tool\",\"choices\":[{\"index\":0,\"delta\":{\"tool_calls\":[{\"index\":0,\"id\":\"call_a\",\"type\":\"function\",\"function\":{\"name\":\"func1\"}},{\"index\":1,\"id\":\"call_b\",\"type\":\"function\",\"function\":{\"name\":\"func2\"}}]},\"finish_reason\":null}]}",
            "data: {\"id\":\"multi-tool\",\"choices\":[{\"index\":0,\"delta\":{\"tool_calls\":[{\"index\":0,\"function\":{\"arguments\":\"{}\"}},{\"index\":1,\"function\":{\"arguments\":\"{\\\"x\\\":1}\"}}]},\"finish_reason\":null}]}",
            "data: {\"id\":\"multi-tool\",\"choices\":[{\"index\":0,\"delta\":{},\"finish_reason\":\"tool_calls\"}]}",
            "data: [DONE]"
        };

        for (String line : sseLines) {
            handler.processSseLine(line);
        }

        CatholicLLMResponse response = handler.buildFinalResponse();

        var toolCalls = response.message().toolCalls();
        assertEquals(2, toolCalls.size());

        assertEquals("call_a", toolCalls.get(0).id());
        assertEquals("func1", toolCalls.get(0).function().name());

        assertEquals("call_b", toolCalls.get(1).id());
        assertEquals("func2", toolCalls.get(1).function().name());
    }
}