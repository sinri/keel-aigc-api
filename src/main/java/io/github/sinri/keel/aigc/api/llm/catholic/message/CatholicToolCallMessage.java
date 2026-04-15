package io.github.sinri.keel.aigc.api.llm.catholic.message;

import java.util.List;

/**
 * 工具调用结果消息，用于反馈工具执行结果给LLM。
 */
public record CatholicToolCallMessage(
    String toolCallId,
    String content
) implements CatholicChatMessage {

    public static final String ROLE = "tool";

    @Override
    public String role() {
        return ROLE;
    }

    @Override
    public List<CatholicChatContent> contents() {
        return List.of(new CatholicTextContent(content));
    }

    /**
     * 便捷构造方法
     */
    public static CatholicToolCallMessage of(String toolCallId, String content) {
        return new CatholicToolCallMessage(toolCallId, content);
    }
}