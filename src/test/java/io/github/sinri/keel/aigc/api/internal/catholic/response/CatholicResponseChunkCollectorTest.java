package io.github.sinri.keel.aigc.api.internal.catholic.response;

import io.github.sinri.keel.aigc.api.llm.catholic.response.CatholicToolCallChunkDelta;
import io.github.sinri.keel.aigc.api.llm.catholic.response.CatholicLLMUsage;
import io.github.sinri.keel.aigc.api.llm.catholic.tool.call.CatholicFunctionToolCall;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CatholicResponseChunkCollectorTest {

    @Test
    void mergesUsageFromTrailingNonFinishedChunkWithoutLosingFinishedState() {
        CatholicResponseChunkCollector collector = new CatholicResponseChunkCollector();
        collector.collect(CatholicLLMResponseChunkImpl.builder()
                .id("response_1")
                .markFinished()
                .usage(new CatholicLLMUsage(null, 7, null))
                .build());
        collector.collect(CatholicLLMResponseChunkImpl.builder()
                .id("response_1")
                .usage(new CatholicLLMUsage(11, null, null))
                .build());

        CatholicLLMResponseImpl response = collector.build();

        assertTrue(response.finished());
        assertEquals(11, response.usage().promptTokens());
        assertEquals(7, response.usage().completionTokens());
        assertEquals(null, response.usage().totalTokens());
    }

    @Test
    void emptyFinishedUsageDoesNotEraseEarlierUsage() {
        CatholicResponseChunkCollector collector = new CatholicResponseChunkCollector();
        collector.collect(CatholicLLMResponseChunkImpl.builder()
                .id("response_1")
                .usage(new CatholicLLMUsage(3, 4, 7))
                .build());
        collector.collect(CatholicLLMResponseChunkImpl.builder()
                .id("response_1")
                .markFinished()
                .build());

        CatholicLLMResponseImpl response = collector.build();

        assertEquals(3, response.usage().promptTokens());
        assertEquals(4, response.usage().completionTokens());
        assertEquals(7, response.usage().totalTokens());
    }

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

    @Test
    void preservesToolCallIdentityWhenLaterChunksContainBlankValues() {
        CatholicResponseChunkCollector collector = new CatholicResponseChunkCollector();
        collector.collect(chunk(null, toolCall(0, "call_0", "command", "")));
        collector.collect(chunk(null, toolCall(0, "", "", "{\"command\": ")));
        collector.collect(chunk(null, toolCall(0, "", null, "\"pwd\"}")));

        CatholicFunctionToolCall toolCall = collector.build().message().toolCalls().get(0);

        assertEquals("call_0", toolCall.id());
        assertEquals("command", toolCall.functionName());
        assertEquals("{\"command\": \"pwd\"}", toolCall.function().arguments());
    }

    @Test
    void rejectsToolCallWithArgumentsButNoFunctionName() {
        CatholicResponseChunkCollector collector = new CatholicResponseChunkCollector();
        collector.collect(chunk(null, toolCall(0, "call_0", null, "{\"query\":\"value\"}")));

        IllegalStateException exception = assertThrows(IllegalStateException.class, collector::build);

        assertEquals("incomplete streamed tool call at index 0: missing function name", exception.getMessage());
    }

    @Test
    void rejectsToolCallWithNoId() {
        CatholicResponseChunkCollector collector = new CatholicResponseChunkCollector();
        collector.collect(chunk(null, toolCall(2, null, "search", "{}")));

        IllegalStateException exception = assertThrows(IllegalStateException.class, collector::build);

        assertEquals("incomplete streamed tool call at index 2: missing id", exception.getMessage());
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
