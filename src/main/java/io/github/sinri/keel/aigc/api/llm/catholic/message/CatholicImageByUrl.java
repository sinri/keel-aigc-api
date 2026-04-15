package io.github.sinri.keel.aigc.api.llm.catholic.message;

/**
 * 通过URL引用的图片内容。
 */
public record CatholicImageByUrl(
    String url
) implements CatholicImageContent {
}