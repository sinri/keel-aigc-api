package io.github.sinri.keel.aigc.api.llm.catholic.message;

/**
 * 文本内容，用于消息中的文本部分。
 */
public record CatholicTextContent(
    String text
) implements CatholicChatContent {
}