package io.github.sinri.keel.aigc.api.internal.catholic.response;

import io.github.sinri.keel.aigc.api.llm.catholic.message.CatholicAssistantMessage;
import io.github.sinri.keel.aigc.api.llm.catholic.response.CatholicLLMUsage;
import io.github.sinri.keel.aigc.api.llm.catholic.response.CatholicToolCallChunkDelta;
import io.github.sinri.keel.aigc.api.llm.catholic.tool.call.CatholicFunctionToolCall;
import io.github.sinri.keel.aigc.api.llm.catholic.tool.call.FunctionCall;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 流式回复片段收集器，用于将多个CatholicLLMResponseChunk组装为完整的CatholicLLMResponse。
 */
public class CatholicResponseChunkCollector {

    private String id;
    private final StringBuilder textBuilder = new StringBuilder();
    private final Map<Integer, ToolCallCollector> toolCallCollectors = new HashMap<>();
    private CatholicLLMUsage usage;
    private boolean finished = false;

    /**
     * 处理一个片段，累积内容
     */
    public void collect(CatholicLLMResponseChunkImpl chunk) {
        if (id == null && chunk.id() != null) {
            this.id = chunk.id();
        }

        // 累积文本
        if (chunk.deltaText() != null) {
            textBuilder.append(chunk.deltaText());
        }

        // 累积工具调用
        if (chunk.deltaToolCalls() != null) {
            for (CatholicToolCallChunkDelta delta : chunk.deltaToolCalls()) {
                int index = delta.index();
                ToolCallCollector collector = toolCallCollectors.computeIfAbsent(index, ToolCallCollector::new);
                collector.collect(delta);
            }
        }

        // 标记完成
        if (chunk.isFinished()) {
            this.finished = true;
            this.usage = chunk.usage();
        }
    }

    /**
     * 构建最终的回复
     */
    public CatholicLLMResponseImpl build() {
        String text = textBuilder.length() > 0 ? textBuilder.toString() : null;

        List<CatholicFunctionToolCall> toolCalls = null;
        if (!toolCallCollectors.isEmpty()) {
            toolCalls = new ArrayList<>();
            for (int i = 0; toolCallCollectors.containsKey(i); i++) {
                ToolCallCollector collector = toolCallCollectors.get(i);
                toolCalls.add(collector.build());
            }
        }

        CatholicAssistantMessage message = new CatholicAssistantMessage(text, toolCalls);
        return new CatholicLLMResponseImpl(id, message, usage != null ? usage : CatholicLLMUsage.empty(), finished);
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
        private String id;
        private String type;
        private String name;
        private final StringBuilder argumentsBuilder = new StringBuilder();

        ToolCallCollector(int index) {
            this.index = index;
        }

        void collect(CatholicToolCallChunkDelta delta) {
            if (delta.id() != null) {
                this.id = delta.id();
            }
            if (delta.type() != null) {
                this.type = delta.type();
            }
            if (delta.function() != null) {
                if (delta.function().name() != null) {
                    this.name = delta.function().name();
                }
                if (delta.function().argumentsDelta() != null) {
                    argumentsBuilder.append(delta.function().argumentsDelta());
                }
            }
        }

        CatholicFunctionToolCall build() {
            return new CatholicFunctionToolCall(
                id,
                type != null ? type : "function",
                new FunctionCall(name, argumentsBuilder.toString())
            );
        }
    }
}