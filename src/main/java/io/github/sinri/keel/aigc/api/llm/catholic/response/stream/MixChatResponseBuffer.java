package io.github.sinri.keel.aigc.api.llm.catholic.response.stream;


import io.github.sinri.keel.aigc.api.llm.catholic.message.MixChatMessage;
import io.github.sinri.keel.aigc.api.llm.catholic.response.MixChatResponse;
import io.github.sinri.keel.aigc.api.llm.catholic.tool.call.ToolCall;
import io.github.sinri.keel.aigc.api.llm.catholic.tool.call.ToolCallStreamPieceCollector;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 流式响应缓冲区。
 * <p>
 * 用于收集和聚合流式响应数据块，最终构建完整的 MixChatResponse。
 *
 * @since 5.0.0
 */
public class MixChatResponseBuffer implements StreamPieceCollector<MixChatResponseChunk, MixChatResponse> {
    private final MixChatResponseChunkChoiceBuffer choiceBuffer = new MixChatResponseChunkChoiceBuffer();

    @Override
    public void accept(MixChatResponseChunk piece) {
        List<MixChatResponseChunkChoice> choices = piece.getChoices();
        if (!choices.isEmpty()) {
            choiceBuffer.accept(choices.get(0));
        }
    }

    @Override
    public MixChatResponse build() {
        MixChatResponse mixChatResponse = MixChatResponse.create();
        mixChatResponse.setMessage(choiceBuffer.build());
        return mixChatResponse;
    }

    private static class MixChatResponseChunkChoiceBuffer
            implements StreamPieceCollector<MixChatResponseChunkChoice, MixChatMessage> {

        private @Nullable String role;
        private final StringBuilder contentBuffer = new StringBuilder();
        private final StringBuilder reasoningContentBuffer = new StringBuilder();
        private @Nullable Integer index;
        private @Nullable String finishReason;
        private final Map<Integer, ToolCallStreamPieceCollector> tcMap = new HashMap<>();

        @Override
        public void accept(MixChatResponseChunkChoice piece) {
            String content = piece.getContent();
            String reasoningContent = piece.getReasoningContent();
            List<ToolCall> toolCalls = piece.getToolCalls();

            if (role == null) {
                this.role = piece.getRole();
            }
            if (index == null) {
                this.index = piece.getIndex();
            }
            if (finishReason == null) {
                this.finishReason = piece.getFinishReason();
            }
            if (content != null) {
                this.contentBuffer.append(content);
            }
            if (reasoningContent != null) {
                this.reasoningContentBuffer.append(reasoningContent);
            }
            for (int i = 0; i < toolCalls.size(); i++) {
                ToolCall toolCall = toolCalls.get(i);
                tcMap.computeIfAbsent(i, x -> new ToolCallStreamPieceCollector())
                     .accept(toolCall);
            }
        }

        @Override
        public MixChatMessage build() {
            MixChatMessage message = MixChatMessage.create();
            message.setRole(role);
            String textContent = contentBuffer.toString();
            if (!textContent.isEmpty()) {
                message.setTextContent(textContent);
            }
            String reasoningContent = reasoningContentBuffer.toString();
            if (!reasoningContent.isEmpty()) {
                message.setReasoningContent(reasoningContent);
            }
            message.setIndex(index);
            message.setFinishReason(finishReason);

            List<ToolCall> tcList = new ArrayList<>();
            for (int i = 0; i < tcMap.size(); i++) {
                ToolCall tc = tcMap.get(i).build();
                tcList.add(tc);
            }
            message.setToolCalls(tcList);

            return message;
        }
    }
}
