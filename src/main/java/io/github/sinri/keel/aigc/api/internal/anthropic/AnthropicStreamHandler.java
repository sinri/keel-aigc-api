package io.github.sinri.keel.aigc.api.internal.anthropic;

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

/**
 * 解析 Anthropic Messages API 的 SSE 事件，转为 {@link CatholicLLMResponseChunk}。
 */
public class AnthropicStreamHandler {

    private final Map<Integer, BlockContext> blockContextByIndex = new HashMap<>();
    private CatholicResponseChunkCollector collector = new CatholicResponseChunkCollector();
    private @Nullable String messageId;
    private @Nullable Integer inputTokensHint;
    private @Nullable Integer lastOutputTokens;

    private static @Nullable Integer computeTotal(@Nullable Integer in, @Nullable Integer out) {
        if (in == null || out == null) {
            return null;
        }
        return in + out;
    }

    /**
     * 处理单行 SSE（通常为 {@code data: {...}}）。
     */
    public @Nullable CatholicLLMResponseChunk processSseLine(String sseLine) {
        if (sseLine.isEmpty()) {
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

        if ("error".equals(type)) {
            JsonObject err = json.getJsonObject("error");
            String msg = err != null ? err.encode() : json.encode();
            throw new RuntimeException("Anthropic stream error: " + msg);
        }

        return switch (type) {
            case "message_start" -> {
                absorbMessageStart(json);
                yield null;
            }
            case "content_block_start" -> {
                absorbContentBlockStart(json);
                yield null;
            }
            case "content_block_delta" -> emitContentBlockDelta(json);
            case "message_delta" -> {
                absorbMessageDelta(json);
                yield null;
            }
            case "message_stop" -> emitMessageStop();
            default -> null;
        };
    }

    private void absorbMessageStart(JsonObject event) {
        JsonObject message = event.getJsonObject("message");
        if (message == null) {
            return;
        }
        String id = message.getString("id");
        if (id != null) {
            this.messageId = id;
        }
        JsonObject usage = message.getJsonObject("usage");
        if (usage != null && usage.getInteger("input_tokens") != null) {
            this.inputTokensHint = usage.getInteger("input_tokens");
        }
    }

    private void absorbContentBlockStart(JsonObject event) {
        JsonObject block = event.getJsonObject("content_block");
        if (block == null || !"tool_use".equals(block.getString("type"))) {
            return;
        }
        int index = event.getInteger("index", 0);
        String id = block.getString("id");
        String name = block.getString("name");
        blockContextByIndex.put(index, new BlockContext(id, name));
    }

    private @Nullable CatholicLLMResponseChunk emitContentBlockDelta(JsonObject event) {
        JsonObject delta = event.getJsonObject("delta");
        if (delta == null) {
            return null;
        }
        String deltaType = delta.getString("type");
        int index = event.getInteger("index", 0);

        if ("text_delta".equals(deltaType)) {
            String text = delta.getString("text");
            if (text == null || text.isEmpty()) {
                return null;
            }
            var builder = CatholicLLMResponseChunkImpl.builder();
            if (messageId != null) {
                builder.id(messageId);
            }
            CatholicLLMResponseChunkImpl chunk = builder
                    .index(index)
                    .deltaText(text)
                    .build();
            collector.collect(chunk);
            return chunk;
        }

        if ("input_json_delta".equals(deltaType)) {
            String partial = delta.getString("partial_json");
            if (partial == null || partial.isEmpty()) {
                return null;
            }
            BlockContext ctx = blockContextByIndex.get(index);
            String toolId = ctx != null ? ctx.toolUseId : null;
            String toolName = ctx != null ? ctx.toolName : null;
            CatholicToolCallChunkDelta toolDelta = new CatholicToolCallChunkDelta(
                    toolId,
                    "tool_use",
                    index,
                    new CatholicToolCallFunctionChunkDelta(toolName, partial)
            );
            var builder = CatholicLLMResponseChunkImpl.builder();
            if (messageId != null) {
                builder.id(messageId);
            }
            CatholicLLMResponseChunkImpl chunk = builder
                    .index(index)
                    .deltaToolCalls(List.of(toolDelta))
                    .build();
            collector.collect(chunk);
            return chunk;
        }

        return null;
    }

    private void absorbMessageDelta(JsonObject event) {
        JsonObject usage = event.getJsonObject("usage");
        if (usage != null && usage.getInteger("output_tokens") != null) {
            this.lastOutputTokens = usage.getInteger("output_tokens");
        }
    }

    private CatholicLLMResponseChunk emitMessageStop() {
        CatholicLLMUsage usage = new CatholicLLMUsage(
                inputTokensHint,
                lastOutputTokens,
                computeTotal(inputTokensHint, lastOutputTokens)
        );
        var builder = CatholicLLMResponseChunkImpl.builder();
        if (messageId != null) {
            builder.id(messageId);
        }
        CatholicLLMResponseChunkImpl chunk = builder
                .markFinished()
                .usage(usage)
                .build();
        collector.collect(chunk);
        return chunk;
    }

    public io.github.sinri.keel.aigc.api.llm.catholic.CatholicLLMResponse buildFinalResponse() {
        return collector.build();
    }

    public void reset() {
        this.collector = new CatholicResponseChunkCollector();
        messageId = null;
        inputTokensHint = null;
        lastOutputTokens = null;
        blockContextByIndex.clear();
    }

    private record BlockContext(String toolUseId, String toolName) {
    }
}
