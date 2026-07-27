package io.github.sinri.keel.aigc.api.llm.dashscope;

import io.github.sinri.keel.aigc.api.internal.dashscope.DashScopeStreamHandler;
import io.github.sinri.keel.aigc.api.llm.catholic.CatholicLLMResponse;
import io.github.sinri.keel.aigc.api.llm.catholic.CatholicLLMResponseChunk;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DashScopeStreamHandlerTest {

    private DashScopeStreamHandler handler;

    @BeforeEach
    void setUp() {
        handler = new DashScopeStreamHandler();
    }

    @Test
    void testProcessTextStream() {
        // 模拟 DashScope SSE 文本流格式
        // DashScope 使用多行 SSE 格式：id、event、data，空行分隔事件块
        String[] sseLines = {
            "id:1",
            "event:add",
            "data:{\"request_id\":\"req-stream\",\"output\":{\"choices\":[{\"message\":{\"role\":\"assistant\",\"content\":\"Hello\"}}]}}",
            "",  // 空行表示事件块结束
            "id:2",
            "event:add",
            "data:{\"request_id\":\"req-stream\",\"output\":{\"choices\":[{\"message\":{\"role\":\"assistant\",\"content\":\" World\"}}]}}",
            "",
            "id:3",
            "event:result",
            "data:{\"request_id\":\"req-stream\",\"output\":{\"choices\":[{\"finish_reason\":\"stop\",\"message\":{\"role\":\"assistant\"}}]},\"usage\":{\"input_tokens\":5,\"output_tokens\":10,\"total_tokens\":15}}",
            ""
        };

        for (String line : sseLines) {
            CatholicLLMResponseChunk chunk = handler.processSseLine(line);
            // chunk 只有在空行时才返回（事件块完成）
        }

        // 处理最后的剩余内容
        handler.flush();

        CatholicLLMResponse response = handler.buildFinalResponse();

        assertEquals("req-stream", response.id());
        assertEquals("Hello World", response.text());
        assertFalse(response.hasToolCalls());
    }

    @Test
    void testProcessEmptyLine() {
        // 非空行不返回 chunk
        CatholicLLMResponseChunk chunk1 = handler.processSseLine("id:1");
        assertNull(chunk1);

        CatholicLLMResponseChunk chunk2 = handler.processSseLine("event:add");
        assertNull(chunk2);

        CatholicLLMResponseChunk chunk3 = handler.processSseLine("data:{\"test\":1}");
        assertNull(chunk3);

        // 空行触发事件处理，返回 chunk
        CatholicLLMResponseChunk chunk4 = handler.processSseLine("");
        // 这里的 chunk 可能因为缺少完整数据而为 null
    }

    @Test
    void testProcessInvalidJson() {
        handler.processSseLine("id:1");
        handler.processSseLine("event:add");
        handler.processSseLine("data:{invalid json}");
        CatholicLLMResponseChunk chunk = handler.processSseLine(""); // 空行触发处理

        // JSON 解析失败，应返回 null
        assertNull(chunk);
    }

    @Test
    void testReset() {
        // 处理第一个事件
        handler.processSseLine("id:1");
        handler.processSseLine("event:add");
        handler.processSseLine("data:{\"request_id\":\"first\",\"output\":{\"choices\":[{\"message\":{\"content\":\"A\"}}]}}");
        handler.processSseLine("");

        // 重置
        handler.reset();

        // 处理新事件
        handler.processSseLine("id:2");
        handler.processSseLine("event:add");
        handler.processSseLine("data:{\"request_id\":\"second\",\"output\":{\"choices\":[{\"message\":{\"content\":\"B\"}}]}}");
        handler.processSseLine("");
        handler.flush();

        CatholicLLMResponse response = handler.buildFinalResponse();

        assertEquals("second", response.id());
        assertEquals("B", response.text());
    }

    @Test
    void testEventResultMarksFinished() {
        handler.processSseLine("id:1");
        handler.processSseLine("event:result");
        handler.processSseLine("data:{\"request_id\":\"req-done\",\"output\":{\"choices\":[{\"finish_reason\":\"stop\",\"message\":{\"role\":\"assistant\"}}]},\"usage\":{\"input_tokens\":1,\"output_tokens\":2}}");
        CatholicLLMResponseChunk chunk = handler.processSseLine("");

        assertNotNull(chunk);
        assertTrue(chunk.isFinished());
    }

    @Test
    void preservesProtocolToolCallIndexAcrossSparseChunks() {
        String[] sseLines = {
            "id:1",
            "event:add",
            "data:{\"request_id\":\"req-tool\",\"output\":{\"choices\":[{\"message\":{\"tool_calls\":[{\"index\":3,\"id\":\"call_3\",\"type\":\"function\",\"function\":{\"name\":\"search\"}}]}}]}}",
            "",
            "id:2",
            "event:add",
            "data:{\"request_id\":\"req-tool\",\"output\":{\"choices\":[{\"message\":{\"tool_calls\":[{\"index\":3,\"function\":{\"arguments\":\"{\\\"query\\\":\\\"value\\\"}\"}}]}}]}}",
            ""
        };

        for (String line : sseLines) {
            handler.processSseLine(line);
        }

        CatholicLLMResponse response = handler.buildFinalResponse();
        assertEquals(1, response.message().toolCalls().size());
        assertEquals("call_3", response.message().toolCalls().get(0).id());
        assertEquals("search", response.message().toolCalls().get(0).functionName());
        assertEquals(
            "{\"query\":\"value\"}",
            response.message().toolCalls().get(0).function().arguments()
        );
    }
}
