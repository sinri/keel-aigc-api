package io.github.sinri.keel.aigc.api.llm.sect.dialect.openai.classic;

import io.github.sinri.keel.aigc.api.llm.catholic.message.MixChatMessage;
import io.github.sinri.keel.aigc.api.llm.catholic.request.MixChatRequest;
import io.github.sinri.keel.aigc.api.llm.catholic.request.MixChatRequestExtra;
import io.github.sinri.keel.aigc.api.llm.catholic.response.MixChatResponse;
import io.github.sinri.keel.aigc.api.llm.catholic.response.stream.MixChatResponseChunk;
import io.github.sinri.keel.aigc.api.llm.catholic.response.stream.MixChatResponseChunkChoice;
import io.github.sinri.keel.aigc.api.llm.catholic.tool.call.ToolCall;
import io.github.sinri.keel.aigc.api.llm.sect.MessageUtils;
import io.github.sinri.keel.aigc.api.llm.sect.dialect.openai.classic.message.GPTMessage;
import io.github.sinri.keel.aigc.api.llm.sect.dialect.openai.classic.message.GPTMessageInResponse;
import io.github.sinri.keel.aigc.api.llm.sect.dialect.openai.classic.message.GPTMessageInTextRequest;
import io.github.sinri.keel.aigc.api.llm.sect.dialect.openai.classic.message.GPTMessageInVisionRequest;
import io.github.sinri.keel.aigc.api.llm.sect.dialect.openai.classic.message.vision.GPTVisionMessageContent;
import io.github.sinri.keel.aigc.api.llm.sect.dialect.openai.classic.message.vision.GPTVisionMessageContentImageUrl;
import io.github.sinri.keel.aigc.api.llm.sect.dialect.openai.classic.request.GPTRequest;
import io.github.sinri.keel.aigc.api.llm.sect.dialect.openai.classic.request.GPTResponseFormat;
import io.github.sinri.keel.aigc.api.llm.sect.dialect.openai.classic.response.stream.GPTResponseChunk;
import io.github.sinri.keel.aigc.api.llm.sect.dialect.openai.classic.response.stream.GPTResponseChunkChoiceDelta;
import io.github.sinri.keel.aigc.api.llm.sect.dialect.openai.classic.response.stream.GPTResponseFragment;
import io.github.sinri.keel.aigc.api.llm.sect.dialect.openai.classic.response.sync.GPTResponse;
import io.github.sinri.keel.aigc.api.llm.sect.dialect.openai.classic.response.sync.GPTResponseChoice;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;

public class OpenAIClassicUtils {
    public static GPTMessage toGPTMessageInTextChatRequest(MixChatMessage mixChatMessage) {
        var j = MessageUtils.toCommonChatRequestJsonObject(mixChatMessage);
        // note: GPT vision chat is not implemented
        return GPTMessageInTextRequest.wrap(j);
    }

    public static GPTMessage toGPTMessageInVisionChatRequest(MixChatMessage mixChatMessage) {
        var x = GPTMessageInVisionRequest.create();

        x.setRole(mixChatMessage.getRole());
        x.setVisionContent(mixChatMessage.getVisionContent().stream().map(a -> {
            GPTVisionMessageContent b = GPTVisionMessageContent.create();
            switch (a.getType()) {
                case text -> b.setText(a.getText());
                case image -> b.setImageUrl(GPTVisionMessageContentImageUrl.create().setUrl(a.getImage()));
            }
            return b;
        }).toList());

        return x;
    }

    public static GPTMessage toGPTMessage(MixChatMessage mixChatMessage) {
        if (mixChatMessage.isForVision()) {
            return toGPTMessageInVisionChatRequest(mixChatMessage);
        } else {
            return toGPTMessageInTextChatRequest(mixChatMessage);
        }
    }

    public static GPTRequest toGPTRequest(MixChatRequest mixChatRequest) {
        GPTRequest request = GPTRequest.create();
        if (mixChatRequest.isStream()) {
            request.stream(true);
        }
        mixChatRequest.getMessages().forEach(m -> request.addMessage(toGPTMessage(m)));
        mixChatRequest.getTools().forEach(request::addTool);

        MixChatRequestExtra extra = mixChatRequest.getExtra();
        Float temperature = extra.getTemperature();
        if (temperature != null) {
            request.temperature(temperature);
        }

        Integer seed = extra.getSeed();
        if (seed != null) {
            request.seed(seed);
        }

        JsonObject responseFormat = extra.getResponseFormat();
        if (responseFormat != null) {
            request.responseFormat(GPTResponseFormat.wrap(responseFormat));
        }

        return request;
    }

    /**
     * 将 OpenAI GPTResponse 转换为 MixChatResponse。
     * 只取第一个 choice。
     *
     * @param resp OpenAI GPTResponse 响应对象
     * @return MixChatResponse 实例
     */
    public static MixChatResponse from(GPTResponse resp) {
        MixChatMessage mixChatMessage = MixChatMessage.create();

        List<GPTResponseChoice> choices = resp.getChoices();
        GPTResponseChoice choice = choices.get(0);
        GPTMessageInResponse message = choice.getMessage();
        if (message != null) {
            mixChatMessage.setRole(message.getRole());
            String content = message.getContent();
            if (content != null) {
                mixChatMessage.setTextContent(content);
            }
            List<ToolCall> toolCalls = message.getToolCalls();
            MixChatResponse.handleToolCalls(toolCalls, mixChatMessage);
        }

        return MixChatResponse.build(mixChatMessage);
    }

    public static Future<GPTResponseChunk> parseStreamFragmentToChunk(String fragment) {
        // AigcMix.getVerboseLogger().info("GPTKit::parseStreamFragmentToChunk for fragment:\n" + fragment);
        if (fragment.startsWith("{")) {
            throw new RuntimeException("!!!");
        }
        GPTResponseFragment f = GPTResponseFragment.wrap(fragment);
        try {
            JsonObject data = new JsonObject(Objects.requireNonNull(f.getRawData()));
            GPTResponseChunk chunk = GPTResponseChunk.wrap(data);
            return Future.succeededFuture(chunk);
        } catch (Throwable throwable) {
            // AigcMix.getVerboseLogger().exception(throwable, "GPTKit::parseStreamFragmentToChunk got null data");
            return Future.failedFuture(throwable);
        }
    }

    public static Future<Void> handleStreamFragment(String fragment, Function<GPTResponseChunk, Future<Void>> cutterProcessFunc) {
        return Future.succeededFuture()
                     .compose(v -> {
                         return parseStreamFragmentToChunk(fragment);
                     })
                     .compose(chunk -> {
                         return cutterProcessFunc.apply(chunk);
                     }, throwable -> {
                         // AigcMix.getVerboseLogger().warning("data in fragment is parsed to null");
                         return Future.succeededFuture();
                     })
                //                     .onFailure(e -> {
                //                         AigcMix.getVerboseLogger().exception(e);
                //                     })
                ;
    }

    public static MixChatResponseChunk from(GPTResponseChunk src) {
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

                                GPTResponseChunkChoiceDelta delta = c.getDelta();
                                if (delta != null) {
                                    x.setRole(delta.getRole());
                                    x.setContent(delta.getContent());

                                    x.setToolCalls(delta.getToolCalls());
                                }
                                return x;
                            })
                            .toList()
        );

        return chunk;
    }
}
