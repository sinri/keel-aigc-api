package io.github.sinri.keel.aigc.api.internal.openai.chatcompletions;

import io.github.sinri.keel.aigc.api.internal.catholic.response.CatholicLLMResponseChunkImpl;
import io.github.sinri.keel.aigc.api.internal.catholic.response.CatholicResponseChunkCollector;
import io.github.sinri.keel.aigc.api.llm.catholic.CatholicLLMResponseChunk;
import io.github.sinri.keel.aigc.api.llm.catholic.response.CatholicLLMUsage;
import io.github.sinri.keel.aigc.api.llm.catholic.response.CatholicToolCallChunkDelta;
import io.github.sinri.keel.aigc.api.llm.catholic.response.CatholicToolCallChunkDelta.CatholicToolCallFunctionChunkDelta;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * 处理 OpenAI Chat Completions API SSE 流式数据，转换为 CatholicLLMResponseChunk。
 */
public class OpenAIChatCompletionsStreamHandler {

    private CatholicResponseChunkCollector collector;
    private @Nullable String responseId;

    public OpenAIChatCompletionsStreamHandler() {
        this.collector = new CatholicResponseChunkCollector();
    }

    /**
     * 处理 SSE 数据行，返回对应的 CatholicLLMResponseChunk
     */
    public CatholicLLMResponseChunk processSseLine(String sseLine) {
        // OpenAI SSE 格式: "data: {...}" 或 "data: [DONE]"
        if (sseLine == null || sseLine.isEmpty()) {
            return null;
        }

        if (!sseLine.startsWith("data: ")) {
            return null;
        }

        String data = sseLine.substring(6);

        // 流结束标记
        if (data.equals("[DONE]")) {
            return null;
        }

        try {
            JsonObject chunkJson = new JsonObject(data);
            return convertChunk(chunkJson);
        } catch (io.vertx.core.json.DecodeException e) {
            throw new IllegalArgumentException("Invalid OpenAI Chat Completions SSE data", e);
        }
    }

    /**
     * 转换 OpenAI chunk 为 CatholicLLMResponseChunk
     */
    private CatholicLLMResponseChunk convertChunk(JsonObject chunkJson) {
        String id = chunkJson.getString("id");
        if (id != null) {
            responseId = id;
        } else {
            id = responseId;
        }
        if (id == null) {
            return null;
        }

        JsonArray choices = chunkJson.getJsonArray("choices");
        if (choices == null || choices.isEmpty()) {
            return CatholicLLMResponseChunkImpl.builder()
                .id(id)
                .build();
        }

        JsonObject firstChoice = choices.getJsonObject(0);
        int index = firstChoice.getInteger("index", 0);
        JsonObject delta = firstChoice.getJsonObject("delta");
        String finishReason = firstChoice.getString("finish_reason");

        // 提取增量内容
        String deltaText = null;
        List<CatholicToolCallChunkDelta> deltaToolCalls = null;

        if (delta != null) {
            // 文本增量
            deltaText = delta.getString("content");

            // 工具调用增量
            JsonArray toolCallsDelta = delta.getJsonArray("tool_calls");
            if (toolCallsDelta != null && !toolCallsDelta.isEmpty()) {
                deltaToolCalls = new ArrayList<>();
                for (int i = 0; i < toolCallsDelta.size(); i++) {
                    JsonObject tcDelta = toolCallsDelta.getJsonObject(i);
                    deltaToolCalls.add(convertToolCallDelta(tcDelta));
                }
            }
        }

        // 是否完成
        boolean finished = finishReason != null;

        // usage (OpenAI 在最后一个 chunk 可能包含 usage)
        JsonObject usageJson = chunkJson.getJsonObject("usage");
        CatholicLLMUsage usage = convertUsage(usageJson);

        CatholicLLMResponseChunkImpl chunk = CatholicLLMResponseChunkImpl.builder()
            .id(id)
            .index(index)
            .deltaText(deltaText)
            .deltaToolCalls(deltaToolCalls)
            .finished(finished)
            .usage(usage)
            .build();

        // 累积到收集器
        collector.collect(chunk);

        return chunk;
    }

    /**
     * 转换 OpenAI tool_calls delta 为 CatholicToolCallChunkDelta
     */
    private CatholicToolCallChunkDelta convertToolCallDelta(JsonObject tcDelta) {
        String id = tcDelta.getString("id");
        String type = tcDelta.getString("type");
        Integer index = tcDelta.getInteger("index", 0);

        JsonObject functionDelta = tcDelta.getJsonObject("function");
        String name = null;
        String argumentsDelta = null;

        if (functionDelta != null) {
            name = functionDelta.getString("name");
            argumentsDelta = functionDelta.getString("arguments");
        }

        return new CatholicToolCallChunkDelta(
            id,
            type,
            index != null ? index : 0,
            new CatholicToolCallFunctionChunkDelta(name, argumentsDelta)
        );
    }

    /**
     * 转换 OpenAI usage 为 CatholicLLMUsage
     */
    private CatholicLLMUsage convertUsage(JsonObject usage) {
        if (usage == null) {
            return CatholicLLMUsage.empty();
        }

        Integer promptTokens = usage.getInteger("prompt_tokens");
        Integer completionTokens = usage.getInteger("completion_tokens");
        Integer totalTokens = usage.getInteger("total_tokens");

        return new CatholicLLMUsage(promptTokens, completionTokens, totalTokens);
    }

    /**
     * 获取收集器（用于构建最终响应）
     */
    public CatholicResponseChunkCollector getCollector() {
        return collector;
    }

    /**
     * 构建最终的完整响应
     */
    public io.github.sinri.keel.aigc.api.llm.catholic.CatholicLLMResponse buildFinalResponse() {
        return collector.build();
    }

    /**
     * 重置收集器（用于新的流式调用）
     */
    public void reset() {
        this.collector = new CatholicResponseChunkCollector();
        this.responseId = null;
    }
}
