package io.github.sinri.keel.aigc.api.internal.openai.responses;

import io.github.sinri.keel.aigc.api.internal.catholic.response.CatholicLLMResponseChunkImpl;
import io.github.sinri.keel.aigc.api.internal.catholic.response.CatholicResponseChunkCollector;
import io.github.sinri.keel.aigc.api.llm.catholic.CatholicLLMResponseChunk;
import io.github.sinri.keel.aigc.api.llm.catholic.response.CatholicLLMUsage;
import io.github.sinri.keel.aigc.api.llm.catholic.response.CatholicToolCallChunkDelta;
import io.github.sinri.keel.aigc.api.llm.catholic.response.CatholicToolCallChunkDelta.CatholicToolCallFunctionChunkDelta;
import io.vertx.core.json.JsonObject;
import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 解析 OpenAI Responses API 的 SSE 事件流，并转换为 {@link CatholicLLMResponseChunk}。
 */
public class OpenAIResponsesStreamHandler {

    private CatholicResponseChunkCollector collector = new CatholicResponseChunkCollector();
    private @Nullable String responseId;
    private final Map<Integer, FunctionCallStreamState> functionCallsByOutputIndex = new HashMap<>();

    /**
     * 处理单行 SSE（通常为 {@code data: {...}}；忽略 {@code event:} 行）。
     */
    public @Nullable CatholicLLMResponseChunk processSseLine(@Nullable String sseLine) {
        if (sseLine == null || sseLine.isEmpty()) {
            return null;
        }
        if (sseLine.startsWith("event:")) {
            return null;
        }
        if (!sseLine.startsWith("data: ")) {
            return null;
        }

        String data = sseLine.substring(6).trim();
        if (data.isEmpty() || "[DONE]".equals(data)) {
            return null;
        }

        final JsonObject json;
        try {
            json = new JsonObject(data);
        } catch (Exception ignored) {
            return null;
        }

        String type = json.getString("type");
        if (type == null) {
            return null;
        }

        if (type.equals("error")) {
            throw new RuntimeException("OpenAI Responses stream error: " + data);
        }

        switch (type) {
            case "response.created", "response.in_progress" -> {
                absorbResponseEnvelope(json);
                return null;
            }
            case "response.output_item.added" -> {
                absorbResponseEnvelope(json);
                handleOutputItemAdded(json);
                return null;
            }
            case "response.output_text.delta" -> {
                absorbResponseEnvelope(json);
                return emitTextDelta(json);
            }
            case "response.function_call_arguments.delta" -> {
                absorbResponseEnvelope(json);
                return emitFunctionArgumentsDelta(json);
            }
            case "response.completed" -> {
                absorbResponseEnvelope(json);
                return emitCompleted(json);
            }
            default -> {
                absorbResponseEnvelope(json);
                return null;
            }
        }
    }

    private void absorbResponseEnvelope(JsonObject event) {
        JsonObject response = event.getJsonObject("response");
        if (response != null) {
            String id = response.getString("id");
            if (id != null) {
                this.responseId = id;
            }
        }
    }

    private void handleOutputItemAdded(JsonObject event) {
        JsonObject item = event.getJsonObject("item");
        if (item == null) {
            return;
        }
        if (!"function_call".equals(item.getString("type"))) {
            return;
        }
        int outputIndex = event.getInteger("output_index", 0);
        String callId = item.getString("call_id");
        String name = item.getString("name");
        functionCallsByOutputIndex.put(outputIndex, new FunctionCallStreamState(callId, name));
    }

    private @Nullable CatholicLLMResponseChunk emitTextDelta(JsonObject event) {
        String delta = event.getString("delta");
        if (delta == null || delta.isEmpty()) {
            return null;
        }
        int outputIndex = event.getInteger("output_index", 0);
        if (responseId == null) {
            return null;
        }
        CatholicLLMResponseChunkImpl chunk = CatholicLLMResponseChunkImpl.builder()
            .id(responseId)
            .index(outputIndex)
            .deltaText(delta)
            .build();
        collector.collect(chunk);
        return chunk;
    }

    private @Nullable CatholicLLMResponseChunk emitFunctionArgumentsDelta(JsonObject event) {
        String delta = event.getString("delta");
        if (delta == null || delta.isEmpty()) {
            return null;
        }
        int outputIndex = event.getInteger("output_index", 0);
        FunctionCallStreamState state = functionCallsByOutputIndex.get(outputIndex);
        String callId = state != null ? state.callId : null;
        String name = state != null ? state.name : null;

        CatholicToolCallChunkDelta toolDelta = new CatholicToolCallChunkDelta(
            callId,
            "function",
            outputIndex,
            new CatholicToolCallFunctionChunkDelta(name, delta)
        );
        if (responseId == null) {
            return null;
        }
        CatholicLLMResponseChunkImpl chunk = CatholicLLMResponseChunkImpl.builder()
            .id(responseId)
            .index(outputIndex)
            .deltaToolCalls(List.of(toolDelta))
            .build();
        collector.collect(chunk);
        return chunk;
    }

    private CatholicLLMResponseChunk emitCompleted(JsonObject event) {
        JsonObject response = event.getJsonObject("response");
        if (response != null) {
            String id = response.getString("id");
            if (id != null) {
                this.responseId = id;
            }
        }
        CatholicLLMUsage usage = convertUsage(response != null ? response.getJsonObject("usage") : null);
        Objects.requireNonNull(responseId);
        CatholicLLMResponseChunkImpl chunk = CatholicLLMResponseChunkImpl.builder()
            .id(responseId)
            .markFinished()
            .usage(usage)
            .build();
        collector.collect(chunk);
        return chunk;
    }

    private CatholicLLMUsage convertUsage(JsonObject usage) {
        if (usage == null) {
            return CatholicLLMUsage.empty();
        }
        Integer prompt = firstNonNull(usage.getInteger("input_tokens"), usage.getInteger("prompt_tokens"));
        Integer completion = firstNonNull(usage.getInteger("output_tokens"), usage.getInteger("completion_tokens"));
        Integer total = usage.getInteger("total_tokens");
        return new CatholicLLMUsage(prompt, completion, total);
    }

    private static @Nullable Integer firstNonNull(@Nullable Integer a, @Nullable Integer b) {
        return a != null ? a : b;
    }

    /**
     * 由已收集的增量构建完整 {@link io.github.sinri.keel.aigc.api.llm.catholic.CatholicLLMResponse}。
     */
    public io.github.sinri.keel.aigc.api.llm.catholic.CatholicLLMResponse buildFinalResponse() {
        return collector.build();
    }

    /**
     * 重置内部状态，便于复用同一实例（本库默认每次请求新建实例）。
     */
    public void reset() {
        this.collector = new CatholicResponseChunkCollector();
        functionCallsByOutputIndex.clear();
        responseId = null;
    }

    private record FunctionCallStreamState(String callId, String name) {
    }
}
