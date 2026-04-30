package io.github.sinri.keel.aigc.api.llm.catholic.message;

import java.util.List;

/**
 * 系统消息，用于设置LLM的行为和上下文。
 */
public record CatholicSystemMessage(
    String text
) implements CatholicChatMessage {

    public static final String ROLE = "system";

    @Override
    public String role() {
        return ROLE;
    }

    @Override
    public List<CatholicChatContent> contents() {
        return List.of(new CatholicTextContent(text));
    }

    /**
     * 便捷构造方法
     */
    public static CatholicSystemMessage of(String text) {
        return new CatholicSystemMessage(text);
    }
}