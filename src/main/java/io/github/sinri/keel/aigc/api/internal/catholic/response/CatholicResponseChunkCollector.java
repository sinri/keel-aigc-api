package io.github.sinri.keel.aigc.api.internal.catholic.response;

import io.github.sinri.keel.aigc.api.trace.CatholicTraceContext;

import io.github.sinri.keel.aigc.api.llm.catholic.message.CatholicAssistantMessage;
import io.github.sinri.keel.aigc.api.llm.catholic.response.CatholicLLMUsage;
import io.github.sinri.keel.aigc.api.llm.catholic.response.CatholicToolCallChunkDelta;
import io.github.sinri.keel.aigc.api.llm.catholic.tool.call.CatholicFunctionToolCall;
import io.github.sinri.keel.aigc.api.llm.catholic.tool.call.CatholicFunctionToolCallImpl;
import io.github.sinri.keel.aigc.api.llm.catholic.tool.call.FunctionCall;
import io.github.sinri.keel.logger.api.LateObject;
import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 流式回复片段收集器，用于将多个CatholicLLMResponseChunk组装为完整的CatholicLLMResponse。
 */
public class CatholicResponseChunkCollector {

    private CatholicTraceContext trace = CatholicTraceContext.none();

    public void trace(CatholicTraceContext trace) { this.trace = trace; }

    public void terminal(String reason) { trace.event("provider_terminal", Map.of("reason", reason == null ? "" : reason)); }

    private final LateObject<String> lateId = new LateObject<>();
    private final StringBuilder textBuilder = new StringBuilder();
    private final Map<Integer, ToolCallCollector> toolCallCollectors = new HashMap<>();
    private @Nullable CatholicLLMUsage usage;
    private boolean finished = false;
    private long collectedChunks;

    /**
     * 处理一个片段，累积内容
     */
    public void collect(CatholicLLMResponseChunkImpl chunk) {
        lateId.ensure(chunk::id);
        collectedChunks++;

        // 累积文本
        if (chunk.deltaText() != null) {
            textBuilder.append(chunk.deltaText());
        }

        // 累积工具调用
        for (CatholicToolCallChunkDelta delta : chunk.deltaToolCalls()) {
            int index = delta.index();
            ToolCallCollector collector = toolCallCollectors.computeIfAbsent(index, ToolCallCollector::new);
            int before = collector.argumentsBuilder.length();
            collector.collect(delta);
            trace.event("arguments_appended", Map.of("chunk_sequence", collectedChunks - 1,
                    "tool_index", index, "before_characters", before,
                    "after_characters", collector.argumentsBuilder.length()));
        }

        // Usage may arrive in a dedicated trailing chunk after the chunk carrying
        // finish_reason. Merge every non-empty usage update instead of coupling it
        // to the finished flag.
        mergeUsage(chunk.usage());

        // 标记完成
        if (chunk.isFinished()) {
            this.finished = true;
        }
    }

    public long collectedChunkCount() {
        return collectedChunks;
    }

    /** Snapshot contains metadata only, never generated text or tool arguments. */
    public Map<String, Object> diagnosticSnapshot() {
        return Map.of("collector", Integer.toHexString(System.identityHashCode(this)),
            "collected_chunks", collectedChunks, "id_initialized", lateId.isInitialized(),
            "text_characters", textBuilder.length(), "tool_calls", toolCallCollectors.size(),
            "finished", finished);
    }

    private void mergeUsage(CatholicLLMUsage update) {
        if (update.promptTokens() == null
            && update.completionTokens() == null
            && update.totalTokens() == null) {
            return;
        }

        Integer promptTokens = update.promptTokens();
        Integer completionTokens = update.completionTokens();
        Integer totalTokens = update.totalTokens();
        if (usage != null) {
            if (promptTokens == null) promptTokens = usage.promptTokens();
            if (completionTokens == null) completionTokens = usage.completionTokens();
            if (totalTokens == null) totalTokens = usage.totalTokens();
        }
        this.usage = new CatholicLLMUsage(promptTokens, completionTokens, totalTokens);
    }

    /** Builds a snapshot. Callers requiring a complete response must validate its state first. */
    public CatholicLLMResponseImpl build() {
        if (trace.enabled()) toolCallCollectors.forEach((index, call) -> trace.payload("aggregated_arguments",
                Map.of("tool_index", index, "tool_call_id", call.id == null ? "" : call.id,
                        "function_name", call.name == null ? "" : call.name), call.argumentsBuilder.toString(), true));
        String text = !textBuilder.isEmpty() ? textBuilder.toString() : null;
        List<CatholicFunctionToolCall> toolCalls = null;
        if (!toolCallCollectors.isEmpty()) {
            toolCalls = toolCallCollectors.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(Map.Entry::getValue)
                .map(ToolCallCollector::build)
                .toList();
        }
        CatholicAssistantMessage message = new CatholicAssistantMessage(text, toolCalls);
        String id = lateId.isInitialized() ? lateId.get() : "";
        return new CatholicLLMResponseImpl(id, message, usage != null ? usage : CatholicLLMUsage.empty(), finished);
    }

    public boolean isIdInitialized() {
        return lateId.isInitialized();
    }

    /**
     * 是否已完成收集
     */
    public boolean isFinished() {
        return finished;
    }

    /**
     * 获取当前累积的文本
     */
    public String getCurrentText() {
        return textBuilder.toString();
    }

    /**
     * 工具调用收集器
     */
    private static class ToolCallCollector {
        private final int index;
        private final StringBuilder argumentsBuilder = new StringBuilder();
        private String id;
        private String name;

        ToolCallCollector(int index) {
            this.index = index;
        }

        void collect(CatholicToolCallChunkDelta delta) {
            if (delta.id() != null && !delta.id().isBlank()) {
                this.id = delta.id();
            }
            if (delta.function() != null) {
                if (delta.function().name() != null && !delta.function().name().isBlank()) {
                    this.name = delta.function().name();
                }
                if (delta.function().argumentsDelta() != null) {
                    argumentsBuilder.append(delta.function().argumentsDelta());
                }
            }
        }

        CatholicFunctionToolCall build() {
            if (id == null || id.isBlank()) {
                throw new IllegalStateException(
                        "incomplete streamed tool call at index " + index + ": missing id");
            }
            if (name == null || name.isBlank()) {
                throw new IllegalStateException(
                        "incomplete streamed tool call at index " + index + ": missing function name");
            }
            return new CatholicFunctionToolCallImpl(
                    id,
                    new FunctionCall(name, argumentsBuilder.toString())
            );
        }
    }
}
