package io.github.sinri.keel.aigc.api.llm.catholic.message;

/**
 * 图片内容的sealed接口，支持URL和Base64两种形式。
 */
public sealed interface CatholicImageContent
    extends CatholicChatContent
    permits CatholicImageByUrl, CatholicImageByBase64 {
}