package io.github.sinri.keel.llm.api.sect.dashscope.qwen;

import io.github.sinri.keel.base.json.JsonObjectConvertible;
import io.github.sinri.keel.llm.api.catholic.message.MixChatMessage;
import io.github.sinri.keel.llm.api.catholic.message.MixChatVisionContentElement;
import io.github.sinri.keel.llm.api.catholic.request.MixChatRequest;
import io.github.sinri.keel.llm.api.catholic.request.MixChatRequestExtra;
import io.github.sinri.keel.llm.api.catholic.response.MixChatResponse;
import io.github.sinri.keel.llm.api.catholic.response.stream.MixChatResponseChunk;
import io.github.sinri.keel.llm.api.catholic.response.stream.MixChatResponseChunkChoice;
import io.github.sinri.keel.llm.api.catholic.tool.call.ToolCall;
import io.github.sinri.keel.llm.api.sect.MessageUtils;
import io.github.sinri.keel.llm.api.sect.dashscope.qwen.message.QwenMessage;
import io.github.sinri.keel.llm.api.sect.dashscope.qwen.message.QwenMessageInChatRequest;
import io.github.sinri.keel.llm.api.sect.dashscope.qwen.message.QwenMessageInResponse;
import io.github.sinri.keel.llm.api.sect.dashscope.qwen.message.QwenMessageInVisionRequest;
import io.github.sinri.keel.llm.api.sect.dashscope.qwen.message.vision.QwenVisionContent;
import io.github.sinri.keel.llm.api.sect.dashscope.qwen.request.QwenRequest;
import io.github.sinri.keel.llm.api.sect.dashscope.qwen.response.stream.QwenResponseChunk;
import io.github.sinri.keel.llm.api.sect.dashscope.qwen.response.stream.QwenResponseFragment;
import io.github.sinri.keel.llm.api.sect.dashscope.qwen.response.sync.QwenResponse;
import io.github.sinri.keel.llm.api.sect.dashscope.qwen.response.sync.QwenResponseOutputChoice;
import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;

public class QwenUtils {
    public static QwenRequest toQwenRequest(MixChatRequest mixChatRequest) {
        QwenRequest request = QwenRequest.create();
        request.parameters(p -> p.resultFormat("message"));
        if (mixChatRequest.isStream()) {
            request.parameters(p -> p
                    .stream(true)
                    .incrementalOutput(true)
            );
        }
        mixChatRequest.getMessages().forEach(m -> request.input(i -> i
                .addMessage(toQwenMessage(m))
        ));
        mixChatRequest.getTools().forEach(t -> request
                .parameters(p -> p
                        .addTool(t)
                ));

        MixChatRequestExtra extra = mixChatRequest.getExtra();
        Float temperature = extra.getTemperature();
        if (temperature != null) {
            request.parameters(p -> p.temperature(temperature));
        }
        Integer seed = extra.getSeed();
        if (seed != null) {
            request.parameters(p -> p.seed(seed));
        }
        JsonObject responseFormat = extra.getResponseFormat();
        if (responseFormat != null) {
            request.parameters(p -> p.responseFormat(responseFormat));
        }

        return request;
    }

    public static QwenMessage toQwenMessage(MixChatMessage mixChatMessage) {
        if (mixChatMessage.isForVision()) {
            return toQwenMessageInVisionChatRequest(mixChatMessage);
        } else {
            return toQwenMessageInTextChatRequest(mixChatMessage);
        }
    }

    public static QwenMessage toQwenMessageInVisionChatRequest(MixChatMessage mixChatMessage) {
        var j = MessageUtils.toCommonChatRequestJsonObject(mixChatMessage);

        List<MixChatVisionContentElement> visionContent = mixChatMessage.getVisionContent();
        j.put(
                "content",
                new JsonArray(visionContent.stream()
                                           .map(QwenUtils::toQwenVisionContent)
                                           .map(JsonObjectConvertible::toJsonObject)
                                           .toList())
        );

        return QwenMessageInVisionRequest.wrap(j);
    }

    public static QwenVisionContent toQwenVisionContent(MixChatVisionContentElement mixChatVisionContentElement) {
        QwenVisionContent content = QwenVisionContent.create();
        MixChatVisionContentElement.ContentElementType type = mixChatVisionContentElement.getType();
        switch (type) {
            case text:
                content.setText(mixChatVisionContentElement.getText());
                break;
            case image:
                content.setImage(mixChatVisionContentElement.getImage());
                break;
        }
        return content;
    }

    public static QwenMessage toQwenMessageInTextChatRequest(MixChatMessage mixChatMessage) {
        var j = MessageUtils.toCommonChatRequestJsonObject(mixChatMessage);
        return QwenMessageInChatRequest.wrap(j);
    }

    public static MixChatResponse from(QwenResponse resp) {
        List<QwenResponseOutputChoice> choices = Objects.requireNonNull(resp.getOutput()).getChoices();
        QwenResponseOutputChoice choice = choices.get(0);
        QwenMessageInResponse message = Objects.requireNonNull(choice.getMessage());

        MixChatMessage mixChatMessage = MixChatMessage.create();
        mixChatMessage.setRole(Objects.requireNonNull(message.getRole()));
        List<String> contents = message.getContents();
        if (!contents.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (var c : contents) {
                MixChatVisionContentElement e = MixChatVisionContentElement.create()
                                                                           .setText(c);
                String text = e.getText();
                sb.append(text);
            }
            mixChatMessage.setTextContent(sb.toString());
        } else {
            String content = message.getContent();
            if (content != null) {
                mixChatMessage.setTextContent(content);
            }
        }

        List<ToolCall> toolCalls = message.getToolCalls();
        MixChatResponse.handleToolCalls(toolCalls, mixChatMessage);

        return MixChatResponse.build(mixChatMessage);
    }

    public static Future<QwenResponseChunk> parseStreamFragmentToChunk(String fragment) {
        QwenResponseFragment qwenResponseFragment = QwenResponseFragment.wrap(fragment);
        JsonObject data = qwenResponseFragment.getData();
        QwenResponseChunk chunk = QwenResponseChunk.wrap(data);
        return Future.succeededFuture(chunk);
    }

    public static Future<Void> handleStreamFragment(
            String fragment,
            Function<QwenResponseChunk, Future<Void>> chunkProcessFunc,
            Function<Throwable, Future<Void>> errorHandleFunc
    ) {
        return Future.succeededFuture()
                     .compose(v -> parseStreamFragmentToChunk(fragment))
                     .compose(chunkProcessFunc, errorHandleFunc);
    }

    public static MixChatResponseChunk from(QwenResponseChunk src) {
        MixChatResponseChunk chunk = MixChatResponseChunk.create();

        chunk.setId(Objects.requireNonNull(src.getRequestId()));

        chunk.setChoices(Objects.requireNonNull(src.getOutput())
                                .getChoices()
                                .stream()
                                .map(c -> {
                                    MixChatResponseChunkChoice x = MixChatResponseChunkChoice.create();

                                    x.setFinishReason(c.getFinishReason());
                                    var message = c.getMessage();
                                    Objects.requireNonNull(message);
                                    x.setRole(message.getRole());
                                    x.setContent(message.getContent());
                                    x.setReasoningContent(message.getReasoningContent());

                                    x.setToolCalls(message.getToolCalls());

                                    return x;
                                })
                                .toList()
        );

        return chunk;
    }
}
