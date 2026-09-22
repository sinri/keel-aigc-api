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
 *
 * @see <a href="https://developers.openai.com/api/reference/resources/chat">OpenAI Chat API Reference</a>
 * @see <a href="https://html.spec.whatwg.org/multipage/server-sent-events.html#parsing-an-event-stream">
 *     WHATWG HTML: Parsing an event stream</a>
 */
public class OpenAIChatCompletionsStreamHandler {

    private CatholicResponseChunkCollector collector;
    private @Nullable String responseId;

    public OpenAIChatCompletionsStreamHandler() {
        this.collector = new CatholicResponseChunkCollector();
    }

    /**
     * 处理 SSE 数据行，返回对应的 CatholicLLMResponseChunk，并将结果累积到内部 collector。
     * <p>
     * 适用于外部已由 {@link #getCollector()} 管理收集的场景（如流式回调模式）。
     * </p>
     */
    public @Nullable CatholicLLMResponseChunk processSseLine(String sseLine) {
        CatholicLLMResponseChunk chunk = parseSseLineOnly(sseLine);
        if (chunk instanceof CatholicLLMResponseChunkImpl impl) collector.collect(impl);
        return chunk;
    }

    /** Parse an SSE line without collecting it. The caller owns aggregation. */
    public @Nullable CatholicLLMResponseChunk parseSseLineOnly(String sseLine) {
        if (sseLine == null || sseLine.isEmpty()) return null;
        if (!sseLine.startsWith("data: ")) return null;
        String data = sseLine.substring(6);
        if (data.equals("[DONE]")) return null;
        try {
            JsonObject chunkJson = new JsonObject(data);
            return parseChunkOnly(chunkJson);
        } catch (io.vertx.core.json.DecodeException e) {
            throw new IllegalArgumentException("Invalid OpenAI Chat Completions SSE data", e);
        }
    }

    /**
     * 转换 JSON chunk 为 {@link CatholicLLMResponseChunkImpl}，
     * 仅更新 {@link #responseId}，<strong>不</strong>调用 {@code collector.collect()}。
     */
    @Nullable
    private CatholicLLMResponseChunk parseChunkOnly(JsonObject chunkJson) {
        String id = chunkJson.getString("id");
        if (id != null) {
            responseId = id;
        } else {
            id = responseId;
        }
        if (id == null) return null;

        JsonObject usageJson = chunkJson.getJsonObject("usage");
        CatholicLLMUsage usage = convertUsage(usageJson);
        JsonArray choices = chunkJson.getJsonArray("choices");
        if (choices == null || choices.isEmpty()) {
            return CatholicLLMResponseChunkImpl.builder().id(id).usage(usage).build();
        }

        JsonObject firstChoice = choices.getJsonObject(0);
        int index = firstChoice.getInteger("index", 0);
        JsonObject delta = firstChoice.getJsonObject("delta");
        String finishReason = firstChoice.getString("finish_reason");
        if (finishReason != null) collector.terminal(finishReason);

        String deltaText = null;
        List<CatholicToolCallChunkDelta> deltaToolCalls = null;
        if (delta != null) {
            deltaText = delta.getString("content");
            JsonArray toolCallsDelta = delta.getJsonArray("tool_calls");
            if (toolCallsDelta != null && !toolCallsDelta.isEmpty()) {
                deltaToolCalls = new ArrayList<>();
                for (int i = 0; i < toolCallsDelta.size(); i++) {
                    deltaToolCalls.add(convertToolCallDelta(toolCallsDelta.getJsonObject(i)));
                }
            }
        }

        boolean finished = finishReason != null;
        return CatholicLLMResponseChunkImpl.builder()
            .id(id).index(index).deltaText(deltaText).deltaToolCalls(deltaToolCalls)
            .finished(finished).usage(usage).build();
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

    /** Build the response from chunks collected through processSseLine. */
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
