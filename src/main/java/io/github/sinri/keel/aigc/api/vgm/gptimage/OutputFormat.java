package io.github.sinri.keel.aigc.api.vgm.gptimage;

/**
 * 输出格式枚举。
 * <p>
 * 用于指定生成图像的输出格式。
 * <p>
 * 注意：Azure OpenAI Service 不支持 WEBP 格式。
 *
 * @since 5.0.0
 */
public enum OutputFormat {
    /**
     * PNG 格式。
     */
    png,
    /**
     * JPEG 格式。
     */
    jpeg
    // WEBP images are not supported in the Azure OpenAI Service.
}
