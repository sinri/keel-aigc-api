package io.github.sinri.keel.aigc.api.llm.sect.dialect.doubao;

import io.github.sinri.keel.base.json.JsonObjectConvertible;
import io.github.sinri.keel.aigc.api.llm.catholic.message.MixChatMessage;
import io.github.sinri.keel.aigc.api.llm.catholic.message.MixChatVisionContentElement;
import io.github.sinri.keel.aigc.api.llm.catholic.request.MixChatRequest;
import io.github.sinri.keel.aigc.api.llm.catholic.request.MixChatRequestExtra;
import io.github.sinri.keel.aigc.api.llm.catholic.response.MixChatResponse;
import io.github.sinri.keel.aigc.api.llm.catholic.response.stream.MixChatResponseChunk;
import io.github.sinri.keel.aigc.api.llm.catholic.response.stream.MixChatResponseChunkChoice;
import io.github.sinri.keel.aigc.api.llm.catholic.tool.call.ToolCall;
import io.github.sinri.keel.aigc.api.llm.sect.MessageUtils;
import io.github.sinri.keel.aigc.api.llm.sect.dialect.doubao.message.DoubaoMessage;
import io.github.sinri.keel.aigc.api.llm.sect.dialect.doubao.message.DoubaoMessageInChatRequest;
import io.github.sinri.keel.aigc.api.llm.sect.dialect.doubao.message.DoubaoMessageInResponse;
import io.github.sinri.keel.aigc.api.llm.sect.dialect.doubao.message.DoubaoMessageInVisionRequest;
import io.github.sinri.keel.aigc.api.llm.sect.dialect.doubao.message.vision.ContentImageUrl;
import io.github.sinri.keel.aigc.api.llm.sect.dialect.doubao.message.vision.DoubaoVisionContent;
import io.github.sinri.keel.aigc.api.llm.sect.dialect.doubao.request.DoubaoRequest;
import io.github.sinri.keel.aigc.api.llm.sect.dialect.doubao.request.ResponseFormatOptions;
import io.github.sinri.keel.aigc.api.llm.sect.dialect.doubao.response.stream.DoubaoResponseChunk;
import io.github.sinri.keel.aigc.api.llm.sect.dialect.doubao.response.stream.DoubaoResponseFragment;
import io.github.sinri.keel.aigc.api.llm.sect.dialect.doubao.response.sync.DoubaoResponse;
import io.github.sinri.keel.aigc.api.llm.sect.dialect.doubao.response.sync.DoubaoResponseChoice;
import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.List;
import java.util.Objects;

public class DoubaoUtils {
    public static DoubaoMessage toDoubaoMessageInTextChatRequest(MixChatMessage mixChatMessage) {
        var j = MessageUtils.toCommonChatRequestJsonObject(mixChatMessage);
        return DoubaoMessageInChatRequest.wrap(j);
    }

    /**
     * 转换为火山 Doubao Vision 专用内容对象。
     *
     * @return DoubaoVisionContent 对象
     */
    public static DoubaoVisionContent toDoubaoVisionContent(MixChatVisionContentElement mixChatVisionContentElement) {
        DoubaoVisionContent content = DoubaoVisionContent.create();
        MixChatVisionContentElement.ContentElementType type = mixChatVisionContentElement.getType();
        switch (type) {
            case text:
                content.setText(mixChatVisionContentElement.getText());
                break;
            case image:
                content.setImageUrl(ContentImageUrl.create().setUrl(mixChatVisionContentElement.getImage()));
                break;
        }
        return content;
    }

    public static DoubaoMessage toDoubaoMessageInVisionChatRequest(MixChatMessage mixChatMessage) {
        var j = MessageUtils.toCommonChatRequestJsonObject(mixChatMessage);

        List<MixChatVisionContentElement> visionContent = mixChatMessage.getVisionContent();
        if (!visionContent.isEmpty()) {
            j.put(
                    "content",
                    new JsonArray(visionContent.stream()
                                               .map(DoubaoUtils::toDoubaoVisionContent)
                                               .map(JsonObjectConvertible::toJsonObject)
                                               .toList())
            );
        }

        return DoubaoMessageInVisionRequest.wrap(j);
    }

    public static DoubaoMessage toDoubaoMessage(MixChatMessage mixChatMessage) {
        if (mixChatMessage.isForVision()) {
            return toDoubaoMessageInVisionChatRequest(mixChatMessage);
        } else {
            return toDoubaoMessageInTextChatRequest(mixChatMessage);
        }
    }

    public static DoubaoRequest toDoubaoRequest(MixChatRequest mixChatRequest) {
        DoubaoRequest request = DoubaoRequest.create();
        if (mixChatRequest.isStream()) {
            request.stream(true);
        }
        mixChatRequest.getMessages().forEach(m -> request.addMessage(toDoubaoMessage(m)));
        mixChatRequest.getTools().forEach(request::addTool);

        MixChatRequestExtra extra = mixChatRequest.getExtra();
        Float temperature = extra.getTemperature();
        if (temperature != null) {
            request.temperature(temperature);
        }
        // volces doubao does not support customize SEED parameter.
        JsonObject responseFormat = extra.getResponseFormat();
        if (responseFormat != null) {
            request.responseFormat(ResponseFormatOptions.wrap(responseFormat));
        }

        return request;
    }

    /**
     * 将 DoubaoResponse 转换为 MixChatResponse。
     * 只取第一个 choice。
     *
     * @return MixChatResponse 实例
     */
    public static MixChatResponse from(DoubaoResponse doubaoResponse) {
        MixChatMessage mixChatMessage = MixChatMessage.create();

        List<DoubaoResponseChoice> choices = doubaoResponse.getChoices();
        DoubaoResponseChoice choice = choices.get(0);
        DoubaoMessageInResponse message = choice.getMessage();
        if (message != null) {
            mixChatMessage.setRole(message.getRole());
            String content = message.getContent();
            if (content != null) {
                mixChatMessage.setTextContent(content);
            }
            String reasoningContent = message.getReasoningContent();
            if(reasoningContent!=null){
                mixChatMessage.setReasoningContent(reasoningContent);
            }
            List<ToolCall> toolCalls = message.getToolCalls();
            MixChatResponse.handleToolCalls(toolCalls, mixChatMessage);
        }
        return MixChatResponse.build(mixChatMessage);
    }

    public static Future<DoubaoResponseChunk> parseStreamFragmentToChunk(String fragment) {
        try {
            DoubaoResponseFragment f = DoubaoResponseFragment.wrap(fragment);
            JsonObject data = f.getData();
            Objects.requireNonNull(data);
            DoubaoResponseChunk c = DoubaoResponseChunk.wrap(data);
            return Future.succeededFuture(c);
        } catch (Throwable throwable) {
            return Future.failedFuture(throwable);
        }
    }

    public static MixChatResponseChunk from(DoubaoResponseChunk src) {
        MixChatResponseChunk chunk = MixChatResponseChunk.create();

        chunk.setCreated(src.getCreated());
        chunk.setId(src.getId());
        chunk.setModel(src.getModel());
        chunk.setObject(src.getObject());

        chunk.setChoices(src.getChoices()
                            .stream()
                            .map(c -> {
                                MixChatResponseChunkChoice x = MixChatResponseChunkChoice.create();

                                x.setFinishReason(c.getFinishReason());
                                x.setIndex(c.getIndex());

                                var delta = c.getDelta();
                                x.setRole(delta.getRole());
                                x.setContent(delta.getContent());
                                x.setReasoningContent(delta.getReasoningContent());

                                x.setToolCalls(delta.getToolCalls()
                                                    .stream()
                                                    .map(tcc -> (ToolCall) tcc)
                                                    .toList());

                                return x;
                            })
                            .toList()
        );

        return chunk;
    }
}
