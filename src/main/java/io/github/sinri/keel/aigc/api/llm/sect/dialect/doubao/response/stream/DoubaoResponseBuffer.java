package io.github.sinri.keel.aigc.api.llm.sect.dialect.doubao.response.stream;

import io.github.sinri.keel.core.utils.StringUtils;
import io.github.sinri.keel.aigc.api.llm.catholic.response.stream.StreamPieceCollector;
import io.github.sinri.keel.aigc.api.llm.catholic.tool.call.ToolCall;
import io.github.sinri.keel.aigc.api.llm.catholic.tool.call.ToolCallStreamPieceCollector;
import io.github.sinri.keel.aigc.api.llm.sect.dialect.doubao.message.DoubaoMessageInResponse;
import io.github.sinri.keel.aigc.api.llm.sect.dialect.doubao.response.sync.DoubaoResponse;
import io.github.sinri.keel.aigc.api.llm.sect.dialect.doubao.response.sync.DoubaoResponseChoice;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class DoubaoResponseBuffer implements StreamPieceCollector<DoubaoResponseChunk, DoubaoResponse> {
    private final Map<Integer, DoubaoResponseChoiceBuffer> choiceBufferMap;
    private @Nullable String id;
    private @Nullable String model;
    private @Nullable String service_tier;
    private @Nullable Integer created;
    private @Nullable String object;

    public DoubaoResponseBuffer() {
        choiceBufferMap = new HashMap<>();
    }

    public void accept(DoubaoResponseChunk chunk) {
        if (id == null) {
            id = chunk.getId();
        }
        if (model == null) {
            model = chunk.getModel();
        }
        if (service_tier == null) {
            service_tier = chunk.getServiceTier();
        }
        if (created == null) {
            created = chunk.getCreated();
        }
        if (object == null) {
            object = chunk.getObject();
        }

        List<DoubaoResponseChunkChoice> choices = chunk.getChoices();
        for (int i = 0; i < choices.size(); i++) {
            choiceBufferMap.computeIfAbsent(i, x -> new DoubaoResponseChoiceBuffer())
                           .accept(choices.get(i));
        }
    }

    @Override
    public DoubaoResponse build() {
        JsonObject j = new JsonObject();
        j.put("id", id);
        j.put("model", model);
        j.put("service_tier", service_tier);
        j.put("created", created);
        j.put("object", object);

        JsonArray choices = new JsonArray();
        for (int i = 0; i < choiceBufferMap.size(); i++) {
            DoubaoResponseChoiceBuffer doubaoResponseChoiceBuffer = choiceBufferMap.get(i);
            DoubaoResponseChoice choice = doubaoResponseChoiceBuffer.build();
            choices.add(choice.cloneAsJsonObject());
        }
        j.put("choices", choices);

        return DoubaoResponse.wrap(j);
    }

    private static class DoubaoResponseChoiceBuffer implements StreamPieceCollector<DoubaoResponseChunkChoice, DoubaoResponseChoice> {
        private final DoubaoMessageInResponseBuffer messageBuffer;
        private @Nullable Integer index;
        private @Nullable String finishReason;

        public DoubaoResponseChoiceBuffer() {
            messageBuffer = new DoubaoMessageInResponseBuffer();
        }

        public void accept(DoubaoResponseChunkChoice chunkChoice) {
            if (index == null) {
                index = chunkChoice.getIndex();
            }
            if (finishReason == null) {
                finishReason = chunkChoice.getFinishReason();
            }

            DoubaoResponseChunkChoiceDelta delta = chunkChoice.getDelta();
            messageBuffer.accept(delta);
        }

        @Override
        public DoubaoResponseChoice build() {
            JsonObject j = new JsonObject();
            j.put("index", index);
            j.put("finish_reason", finishReason);
            j.put("message", messageBuffer.build().toJsonObject());

            return DoubaoResponseChoice.wrap(j);
        }

    }

    private static class DoubaoMessageInResponseBuffer implements StreamPieceCollector<DoubaoResponseChunkChoiceDelta, DoubaoMessageInResponse> {
        private final StringBuilder reasoningContentBuffer = new StringBuilder();
        private final StringBuilder contentBuffer = new StringBuilder();
        private final Map<Integer, ToolCallStreamPieceCollector> toolCallBufferMap;
        private @Nullable String role;

        public DoubaoMessageInResponseBuffer() {
            toolCallBufferMap = new HashMap<>();
        }

        public void accept(DoubaoResponseChunkChoiceDelta delta) {
            if (role == null) {
                role = delta.getRole();
            }
            String reasoningContent = delta.getReasoningContent();
            if (!StringUtils.isNullOrBlank(reasoningContent)) {
                reasoningContentBuffer.append(reasoningContent);
            }
            String content = delta.getContent();
            if (!StringUtils.isNullOrBlank(content)) {
                contentBuffer.append(content);
            }
            List<ToolCall> toolCalls = delta.getToolCalls();
            for (int i = 0; i < toolCalls.size(); i++) {
                toolCallBufferMap.computeIfAbsent(i, x -> new ToolCallStreamPieceCollector())
                                 .accept(toolCalls.get(i));
            }
        }

        @Override
        public DoubaoMessageInResponse build() {
            JsonObject j = new JsonObject();
            j.put("role", role);
            j.put("reasoning_content", reasoningContentBuffer.toString());
            j.put("content", contentBuffer.toString());

            if (!toolCallBufferMap.isEmpty()) {
                JsonArray a = new JsonArray();
                for (int i = 0; i < toolCallBufferMap.size(); i++) {
                    ToolCallStreamPieceCollector doubaoToolCallBuffer = toolCallBufferMap.get(i);
                    ToolCall doubaoToolCall = doubaoToolCallBuffer.build();
                    a.add(doubaoToolCall.toJsonObject());
                }
                j.put("tool_calls", a);
            }

            return DoubaoMessageInResponse.wrap(j);
        }
    }

}
