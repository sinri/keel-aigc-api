package io.github.sinri.keel.llm.api.sect.dialect.qwen.response.stream;

import io.github.sinri.keel.llm.api.catholic.response.stream.StreamPieceCollector;
import io.github.sinri.keel.llm.api.catholic.tool.call.ToolCall;
import io.github.sinri.keel.llm.api.catholic.tool.call.ToolCallStreamPieceCollector;
import io.github.sinri.keel.llm.api.sect.dialect.qwen.message.QwenMessageInResponse;
import io.github.sinri.keel.llm.api.sect.dialect.qwen.message.vision.QwenVisionContent;
import io.github.sinri.keel.llm.api.sect.dialect.qwen.response.sync.QwenResponse;
import io.github.sinri.keel.llm.api.sect.dialect.qwen.response.sync.QwenResponseOutput;
import io.github.sinri.keel.llm.api.sect.dialect.qwen.response.sync.QwenResponseOutputChoice;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class QwenResponseBuffer implements StreamPieceCollector<QwenResponseChunk, QwenResponse> {
    private final UsageBuffer usageBuffer;
    private final OutputBuffer outputBuffer;
    private @Nullable String requestId;

    public QwenResponseBuffer() {
        usageBuffer = new UsageBuffer();
        outputBuffer = new OutputBuffer();
    }

    public void accept(QwenResponseChunk chunk) {
        if (requestId == null) {
            requestId = chunk.getRequestId();
        }
        JsonObject usage = chunk.getUsage();
        if(usage!=null) {
            usageBuffer.accept(usage);
        }

        QwenResponseOutput output = chunk.getOutput();
        if(output!=null) {
            outputBuffer.accept(output);
        }
    }

    @Override
    public QwenResponse build() {
        return QwenResponse.wrap(new JsonObject()
                .put("request_id", requestId)
                .put("usage", usageBuffer.build())
                .put("output", outputBuffer.build().cloneAsJsonObject())
        );
    }

    public static class OutputBuffer implements StreamPieceCollector<QwenResponseOutput, QwenResponseOutput> {
        private final Map<Integer, ChoiceBuffer> choiceBufferMap = new HashMap<>();

        public OutputBuffer() {

        }

        public void accept(QwenResponseOutput output) {
            List<QwenResponseOutputChoice> choices = output.getChoices();
            for (int i = 0; i < choices.size(); i++) {
                var choice = choices.get(i);
                choiceBufferMap.computeIfAbsent(i, x -> new ChoiceBuffer())
                               .accept(choice);
            }
        }

        @Override
        public QwenResponseOutput build() {
            JsonArray choices = new JsonArray();
            for (int i = 0; i < choiceBufferMap.size(); i++) {
                ChoiceBuffer choiceBuffer = choiceBufferMap.get(i);
                QwenResponseOutputChoice choice = choiceBuffer.build();
                choices.add(choice.cloneAsJsonObject());
            }
            return QwenResponseOutput.wrap(new JsonObject()
                    .put("choices", choices));
        }
    }

    public static class ChoiceBuffer implements StreamPieceCollector<QwenResponseOutputChoice, QwenResponseOutputChoice> {
        private final MessageBuffer messageBuffer;
        private @Nullable String finishReason;

        public ChoiceBuffer() {
            messageBuffer = new MessageBuffer();
        }

        public void accept(QwenResponseOutputChoice choice) {
            finishReason = choice.getFinishReason();

            QwenMessageInResponse message = choice.getMessage();
            if (message != null) {
                messageBuffer.accept(message);
            }
        }

        @Override
        public QwenResponseOutputChoice build() {
            return QwenResponseOutputChoice.wrap(new JsonObject()
                    .put("finish_reason", finishReason)
                    .put("message", messageBuffer.build().cloneAsJsonObject())
            );
        }
    }

    public static class MessageBuffer implements StreamPieceCollector<QwenMessageInResponse, QwenMessageInResponse> {
        private final Map<Integer, ContentOfTextBuffer> contentBufferMap;
        private final StringBuilder content;
        private final StringBuilder reasoningContent;
        private final Map<Integer, ToolCallStreamPieceCollector> toolCallBufferMap;
        private @Nullable String role;

        public MessageBuffer() {
            content = new StringBuilder();
            contentBufferMap = new HashMap<>();
            reasoningContent = new StringBuilder();
            toolCallBufferMap = new HashMap<>();
        }

        public void accept(QwenMessageInResponse message) {
            role = message.getRole();

            String contentPiece = message.getContent();
            if (contentPiece != null) {
                content.append(contentPiece);
            }

            List<String> contents = message.getContents();
            for (int i = 0; i < contents.size(); i++) {
                String contentTextPiece = contents.get(i);
                contentBufferMap.computeIfAbsent(i, x -> new ContentOfTextBuffer())
                                .accept(contentTextPiece);
            }

            String reasoningContentPiece = message.getReasoningContent();
            if (reasoningContentPiece != null) {
                reasoningContent.append(reasoningContentPiece);
            }

            List<ToolCall> toolCalls = message.getToolCalls();
            for (int i = 0; i < toolCalls.size(); i++) {
                ToolCall toolCall = toolCalls.get(i);
                toolCallBufferMap.computeIfAbsent(i, x -> new ToolCallStreamPieceCollector())
                                 .accept(toolCall);
            }
        }

        @Override
        public QwenMessageInResponse build() {
            JsonObject x = new JsonObject()
                    .put("role", role);
            if (contentBufferMap.isEmpty()) {
                x.put("content", content.toString());
            } else {
                JsonArray array = new JsonArray();
                for (int i = 0; i < contentBufferMap.size(); i++) {
                    var cb = contentBufferMap.get(i);
                    QwenVisionContent built = cb.build();
                    array.add(built.toJsonObject());
                }
                x.put("content", array);
            }
            if (!reasoningContent.isEmpty()) {
                x.put("reasoning_content", reasoningContent.toString());
            }
            if (!toolCallBufferMap.isEmpty()) {
                JsonArray toolCallArray = new JsonArray();
                for (int i = 0; i < toolCallBufferMap.size(); i++) {
                    ToolCallStreamPieceCollector toolCallBuffer = toolCallBufferMap.get(i);
                    toolCallArray.add(toolCallBuffer.build().toJsonObject());
                }
                x.put("tool_calls", toolCallArray);
            }
            return QwenMessageInResponse.wrap(x);
        }
    }

    /**
     * For Vision Model, i.e. QwenVL series.
     */
    public static class ContentOfTextBuffer implements StreamPieceCollector<String, QwenVisionContent> {
        private final StringBuilder textBuilder = new StringBuilder();

        @Override
        public void accept(String piece) {
            textBuilder.append(piece);
        }

        @Override
        public QwenVisionContent build() {
            return QwenVisionContent.wrap(new JsonObject()
                    .put("text", textBuilder.toString()));
        }
    }

    public static class UsageBuffer implements StreamPieceCollector<JsonObject, JsonObject> {
        private int total_tokens = 0;
        private int input_tokens = 0;
        private int output_tokens = 0;

        public UsageBuffer() {

        }

        public void accept(JsonObject usage) {
            total_tokens += usage.getInteger("total_tokens");
            input_tokens += usage.getInteger("input_tokens");
            output_tokens += usage.getInteger("output_tokens");
        }

        @Override
        public JsonObject build() {
            return new JsonObject()
                    .put("total_tokens", total_tokens)
                    .put("input_tokens", input_tokens)
                    .put("output_tokens", output_tokens);
        }
    }
}
