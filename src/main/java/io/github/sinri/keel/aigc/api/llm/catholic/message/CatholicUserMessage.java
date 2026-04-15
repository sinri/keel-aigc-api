package io.github.sinri.keel.aigc.api.llm.catholic.message;

import java.util.ArrayList;
import java.util.List;

/**
 * 用户消息，支持多模态内容（文本和图片）。
 */
public class CatholicUserMessage implements CatholicChatMessage {

    public static final String ROLE = "user";

    private final List<CatholicChatContent> contents;

    public CatholicUserMessage(List<CatholicChatContent> contents) {
        this.contents = contents;
    }

    @Override
    public String role() {
        return ROLE;
    }

    @Override
    public List<CatholicChatContent> contents() {
        return contents;
    }

    // === 便捷构造方法 ===

    /**
     * 创建纯文本用户消息
     */
    public static CatholicUserMessage ofText(String text) {
        return new CatholicUserMessage(List.of(new CatholicTextContent(text)));
    }

    /**
     * 创建文本+图片URL的用户消息
     */
    public static CatholicUserMessage ofTextAndImage(String text, String imageUrl) {
        return new CatholicUserMessage(List.of(
            new CatholicTextContent(text),
            new CatholicImageByUrl(imageUrl)
        ));
    }

    /**
     * 创建文本+Base64图片的用户消息
     */
    public static CatholicUserMessage ofTextAndBase64Image(String text, String mediaType, String base64Data) {
        return new CatholicUserMessage(List.of(
            new CatholicTextContent(text),
            new CatholicImageByBase64(mediaType, base64Data)
        ));
    }

    /**
     * 创建Builder用于灵活构建多内容消息
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder类
     */
    public static class Builder {
        private final List<CatholicChatContent> contents = new ArrayList<>();

        public Builder addText(String text) {
            contents.add(new CatholicTextContent(text));
            return this;
        }

        public Builder addImageUrl(String url) {
            contents.add(new CatholicImageByUrl(url));
            return this;
        }

        public Builder addImageBase64(String mediaType, String base64Data) {
            contents.add(new CatholicImageByBase64(mediaType, base64Data));
            return this;
        }

        public CatholicUserMessage build() {
            return new CatholicUserMessage(List.copyOf(contents));
        }
    }
}