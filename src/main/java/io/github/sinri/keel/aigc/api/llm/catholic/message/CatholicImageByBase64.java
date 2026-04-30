package io.github.sinri.keel.aigc.api.llm.catholic.message;

/**
 * 通过Base64编码的图片内容。
 */
public record CatholicImageByBase64(
    String mediaType,
    String base64Data
) implements CatholicImageContent {
}