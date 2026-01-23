package io.github.sinri.keel.llm.api.sect.dialect.openai.classic.response.stream;

import io.github.sinri.keel.base.json.UnmodifiableJsonifiableEntity;
import io.github.sinri.keel.llm.api.catholic.response.stream.StreamPieceCollector;
import io.github.sinri.keel.llm.api.catholic.tool.call.ToolCallStreamPieceCollector;
import io.github.sinri.keel.llm.api.sect.dialect.openai.classic.message.GPTMessageInResponse;
import io.github.sinri.keel.llm.api.sect.dialect.openai.classic.response.sync.GPTResponse;
import io.github.sinri.keel.llm.api.sect.dialect.openai.classic.response.sync.GPTResponseChoice;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class GPTResponseBuffer implements StreamPieceCollector<GPTResponseChunk, GPTResponse> {
    private final Map<Integer, ChoiceBuffer> choiceBufferMap = new HashMap<>();
    private final JsonArray promptFilterResults = new JsonArray();
    private @Nullable String id;
    private @Nullable String model;
    private @Nullable String object;
    private @Nullable Integer created;

    public GPTResponseBuffer() {

    }

    @Override
    public void accept(GPTResponseChunk piece) {
        if (id == null) {
            id = piece.getId();
        }
        if (model == null) {
            model = piece.getModel();
        }
        if (object == null) {
            object = piece.getObject();
        }
        if (created == null) {
            created = piece.getCreated();
        }

        var choices = piece.getChoices();
        if (!choices.isEmpty()) {
            for (int i = 0; i < choices.size(); i++) {
                var choice = choices.get(i);
                choiceBufferMap.computeIfAbsent(i, k -> new ChoiceBuffer()).accept(choice);
            }
        }

        piece.getPromptFilterResults()
             .forEach(pfr -> this.promptFilterResults.add(pfr.cloneAsJsonObject()));
    }

    @Override
    public GPTResponse build() {
        JsonObject j = new JsonObject();
        j.put("id", id);
        j.put("model", model);
        j.put("object", object);
        j.put("created", created);
        j.put("choices", new JsonArray(choiceBufferMap.values().stream()
                                                      .map(ChoiceBuffer::build)
                                                      .map(UnmodifiableJsonifiableEntity::cloneAsJsonObject)
                                                      .toList()));
        j.put("prompt_filter_results", this.promptFilterResults);
        return GPTResponse.wrap(j);
    }

    public static class ChoiceBuffer implements StreamPieceCollector<GPTResponseChunkChoice, GPTResponseChoice> {
        private final MessageBuffer messageBuffer = new MessageBuffer();
        private @Nullable Integer index;
        private @Nullable String finishReason;

        @Override
        public void accept(GPTResponseChunkChoice piece) {
            if (index == null) {
                index = piece.getIndex();
            }
            if (finishReason == null) {
                finishReason = piece.getFinishReason();
            }

            var delta = piece.getDelta();
            if (delta != null) {
                messageBuffer.accept(delta);
            }
        }

        @Override
        public GPTResponseChoice build() {
            JsonObject j = new JsonObject();
            j.put("index", index);
            j.put("finish_reason", finishReason);
            j.put("message", messageBuffer.build().cloneAsJsonObject());
            return GPTResponseChoice.wrap(j);
        }
    }

    public static class MessageBuffer
            implements StreamPieceCollector<GPTResponseChunkChoiceDelta, GPTMessageInResponse> {

        private final StringBuilder contentBuilder = new StringBuilder();
        private final Map<Integer, ToolCallStreamPieceCollector> toolCallsMap = new HashMap<>();
        private @Nullable String role;

        @Override
        public void accept(GPTResponseChunkChoiceDelta piece) {
            if (role == null) {
                role = piece.getRole();
            }
            var content = piece.getContent();
            if (content != null) {
                contentBuilder.append(content);
            }

            var toolCalls = piece.getToolCalls();
            for (int i = 0; i < toolCalls.size(); i++) {
                var toolCall = toolCalls.get(i);
                toolCallsMap.computeIfAbsent(i, k -> new ToolCallStreamPieceCollector()).accept(toolCall);
            }
        }

        @Override
        public GPTMessageInResponse build() {
            JsonObject j = new JsonObject();
            j.put("role", role);
            j.put("content", contentBuilder.toString());

            j.put("tool_calls", new JsonArray(toolCallsMap.values().stream()
                                                          .map(x -> x.build().toJsonObject())
                                                          .toList()));

            return GPTMessageInResponse.wrap(j);
        }
    }
}
