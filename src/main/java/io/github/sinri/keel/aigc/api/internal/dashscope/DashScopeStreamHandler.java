package io.github.sinri.keel.aigc.api.internal.dashscope;

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
 * 处理 DashScope API SSE 流式数据，转换为 CatholicLLMResponseChunk。
 * <p>
 * DashScope SSE 格式：
 * id:xxx
 * event:add/result/error
 * data:{...}
 * <p>
 * 事件块用空行分隔
 *
 * @see <a href="https://help.aliyun.com/en/model-studio/qwen-api-via-dashscope">
 *     DashScope API Reference</a>
 * @see <a href="https://html.spec.whatwg.org/multipage/server-sent-events.html#parsing-an-event-stream">
 *     WHATWG HTML: Parsing an event stream</a>
 */
public class DashScopeStreamHandler {

    private CatholicResponseChunkCollector collector;
    private String currentEventId;
    private String currentEventType;
    private final StringBuilder currentEventData = new StringBuilder();
    private @Nullable String responseId;

    public DashScopeStreamHandler() {
        this.collector = new CatholicResponseChunkCollector();
        resetCurrentEvent();
    }

    private void resetCurrentEvent() {
        this.currentEventId = null;
        this.currentEventType = null;
        this.currentEventData.setLength(0);
    }

    /**
     * 处理 SSE 行，返回对应的 CatholicLLMResponseChunk（当事件块完成时）
     */
    public CatholicLLMResponseChunk processSseLine(String sseLine) {
        if (sseLine == null || sseLine.isEmpty()) {
            // 空行表示事件块结束，处理当前事件
            return processCurrentEvent();
        }

        // 解析 SSE 字段
        if (sseLine.startsWith("id:")) {
            currentEventId = sseLine.substring(3).trim();
        } else if (sseLine.startsWith("event:")) {
            currentEventType = sseLine.substring(6).trim();
        } else if (sseLine.startsWith("data:")) {
            if (!currentEventData.isEmpty()) {
                currentEventData.append('\n');
            }
            currentEventData.append(sseLine.substring(5).stripLeading());
        }
        // 其他行（如冒号开头的注释）忽略

        return null; // 事件未完成，不返回 chunk
    }

    /**
     * 处理当前事件块，返回对应的 chunk
     */
    private CatholicLLMResponseChunk processCurrentEvent() {
        if (currentEventData.isEmpty()) {
            resetCurrentEvent();
            return null;
        }

        try {
            JsonObject dataJson = new JsonObject(currentEventData.toString());
            if ("error".equals(currentEventType) || isErrorPayload(dataJson)) {
                throw dashScopeError(dataJson);
            }
            return convertEvent(currentEventId, currentEventType, dataJson);
        } catch (DashScopeStreamException e) {
            throw e;
        } catch (Exception e) {
            throw new DashScopeStreamException(
                "Invalid DashScope SSE data for event " + currentEventDescription(), e
            );
        } finally {
            resetCurrentEvent();
        }
    }

    private boolean isErrorPayload(JsonObject data) {
        String code = data.getString("code");
        Object statusCode = data.getValue("status_code");
        boolean failedStatus = statusCode instanceof Number number
            ? number.intValue() != 200
            : statusCode instanceof String string && !"200".equals(string);
        return (code != null && !code.isBlank()) || failedStatus;
    }

    private DashScopeStreamException dashScopeError(JsonObject data) {
        String requestId = data.getString("request_id", currentEventId);
        String code = data.getString("code", "unknown");
        String message = data.getString("message", "DashScope stream error");
        return new DashScopeStreamException(
            "DashScope SSE error [request_id=" + requestId + ", code=" + code + "]: " + message
        );
    }

    private String currentEventDescription() {
        return "[id=" + currentEventId + ", type=" + currentEventType + "]";
    }

    /**
     * 将 DashScope SSE 事件转换为 CatholicLLMResponseChunk
     */
    private CatholicLLMResponseChunk convertEvent(String eventId, String eventType, JsonObject dataJson) {
        // request_id 作为 id
        String id = dataJson.getString("request_id", eventId);
        if (id != null) {
            responseId = id;
        } else {
            id = responseId;
        }
        if (id == null) {
            return null;
        }

        JsonObject output = dataJson.getJsonObject("output");
        if (output == null) {
            return CatholicLLMResponseChunkImpl.builder()
                .id(id)
                .build();
        }

        JsonArray choices = output.getJsonArray("choices");
        if (choices == null || choices.isEmpty()) {
            return CatholicLLMResponseChunkImpl.builder()
                .id(id)
                .build();
        }

        JsonObject firstChoice = choices.getJsonObject(0);
        String finishReason = firstChoice.getString("finish_reason");
        JsonObject message = firstChoice.getJsonObject("message");

        // 提取增量内容
        String deltaText = null;
        List<CatholicToolCallChunkDelta> deltaToolCalls = null;
        int index = 0;

        if (message != null) {
            // 文本增量
            deltaText = DashScopeContentExtractor.extractText(message);

            // 工具调用增量
            JsonArray toolCalls = message.getJsonArray("tool_calls");
            if (toolCalls != null && !toolCalls.isEmpty()) {
                deltaToolCalls = new ArrayList<>();
                for (int i = 0; i < toolCalls.size(); i++) {
                    JsonObject tc = toolCalls.getJsonObject(i);
                    deltaToolCalls.add(convertToolCallChunkDelta(tc, i));
                }
            }
        }

        // 判断是否完成
        // DashScope: event=result 或 finish_reason 存在表示完成
        boolean finished = "result".equals(eventType) || finishReason != null;

        // usage（仅在 result 事件或 finishReason 存在时有）
        JsonObject usageJson = dataJson.getJsonObject("usage");
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
     * 转换 DashScope tool_call 为 CatholicToolCallChunkDelta
     */
    private CatholicToolCallChunkDelta convertToolCallChunkDelta(JsonObject tc, int index) {
        String id = tc.getString("id");
        String type = tc.getString("type");
        int toolCallIndex = tc.getInteger("index", index);

        JsonObject function = tc.getJsonObject("function");
        String name = null;
        String argumentsDelta = null;

        if (function != null) {
            name = function.getString("name");
            argumentsDelta = function.getString("arguments");
        }

        return new CatholicToolCallChunkDelta(
            id,
            type != null ? type : "function",
            toolCallIndex,
            new CatholicToolCallFunctionChunkDelta(name, argumentsDelta)
        );
    }

    /**
     * 转换 DashScope usage 为 CatholicLLMUsage
     */
    private CatholicLLMUsage convertUsage(JsonObject usage) {
        if (usage == null) {
            return CatholicLLMUsage.empty();
        }

        Integer inputTokens = usage.getInteger("input_tokens");
        Integer outputTokens = usage.getInteger("output_tokens");
        Integer totalTokens = usage.getInteger("total_tokens");

        if (totalTokens == null && inputTokens != null && outputTokens != null) {
            totalTokens = inputTokens + outputTokens;
        }

        return new CatholicLLMUsage(inputTokens, outputTokens, totalTokens);
    }

    /**
     * 处理剩余内容（流结束时调用）
     */
    public CatholicLLMResponseChunk flush() {
        return processCurrentEvent();
    }

    /**
     * 获取收集器
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
     * 重置收集器
     */
    public void reset() {
        this.collector = new CatholicResponseChunkCollector();
        this.responseId = null;
        resetCurrentEvent();
    }

    public static class DashScopeStreamException extends RuntimeException {
        public DashScopeStreamException(String message) {
            super(message);
        }

        public DashScopeStreamException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
