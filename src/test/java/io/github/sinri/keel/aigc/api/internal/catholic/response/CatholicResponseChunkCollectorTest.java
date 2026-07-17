package io.github.sinri.keel.aigc.api.internal.catholic.response;

import io.github.sinri.keel.aigc.api.llm.catholic.response.CatholicToolCallChunkDelta;
import io.github.sinri.keel.aigc.api.llm.catholic.tool.call.CatholicFunctionToolCall;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CatholicResponseChunkCollectorTest {

    @Test
    void buildsToolCallsWithContinuousIndexes() {
        CatholicResponseChunkCollector collector = new CatholicResponseChunkCollector();
        collector.collect(chunk(null, toolCall(0, "call_0", "first", "{}"), toolCall(1, "call_1", "second", "{}")));

        List<CatholicFunctionToolCall> toolCalls = collector.build().message().toolCalls();

        assertEquals(List.of("call_0", "call_1"), toolCalls.stream().map(CatholicFunctionToolCall::id).toList());
    }

    @Test
    void buildsToolCallWhenFirstIndexIsNonZero() {
        CatholicResponseChunkCollector collector = new CatholicResponseChunkCollector();
        collector.collect(chunk(null, toolCall(1, "call_1", "lookup", "{}")));

        List<CatholicFunctionToolCall> toolCalls = collector.build().message().toolCalls();

        assertEquals(1, toolCalls.size());
        assertEquals("call_1", toolCalls.get(0).id());
    }

    @Test
    void buildsSparseToolCallsInIndexOrder() {
        CatholicResponseChunkCollector collector = new CatholicResponseChunkCollector();
        collector.collect(chunk(null, toolCall(4, "call_4", "fourth", "{}"), toolCall(1, "call_1", "first", "{}")));

        List<CatholicFunctionToolCall> toolCalls = collector.build().message().toolCalls();

        assertEquals(List.of("call_1", "call_4"), toolCalls.stream().map(CatholicFunctionToolCall::id).toList());
    }

    @Test
    void preservesTextAndJoinsSparseToolCallArgumentChunks() {
        CatholicResponseChunkCollector collector = new CatholicResponseChunkCollector();
        collector.collect(chunk("some text", toolCall(2, "call_2", "search", "{\"query\":")));
        collector.collect(chunk(null, toolCall(2, null, null, "\"value\"}")));

        CatholicLLMResponseImpl response = collector.build();

        assertEquals("some text", response.message().text());
        assertTrue(response.message().hasToolCalls());
        assertEquals("search", response.message().toolCalls().get(0).functionName());
        assertEquals("{\"query\":\"value\"}", response.message().toolCalls().get(0).function().arguments());
    }

    private static CatholicLLMResponseChunkImpl chunk(String text, CatholicToolCallChunkDelta... toolCalls) {
        return CatholicLLMResponseChunkImpl.builder()
                .id("response_1")
                .deltaText(text)
                .deltaToolCalls(List.of(toolCalls))
                .build();
    }

    private static CatholicToolCallChunkDelta toolCall(int index, String id, String name, String argumentsDelta) {
        return new CatholicToolCallChunkDelta(
                id,
                "function",
                index,
                new CatholicToolCallChunkDelta.CatholicToolCallFunctionChunkDelta(name, argumentsDelta)
        );
    }
}
