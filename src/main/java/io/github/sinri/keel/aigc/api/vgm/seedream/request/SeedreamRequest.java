package io.github.sinri.keel.aigc.api.vgm.seedream.request;


import io.github.sinri.keel.base.annotations.SelfInterface;
import io.github.sinri.keel.base.json.JsonifiableDataUnit;
import org.jspecify.annotations.Nullable;

/**
 * @since 5.0.0
 */
public interface SeedreamRequest<R> extends JsonifiableDataUnit, SelfInterface<R> {

    /**
     *
     * @return 本次请求使用模型的 Model ID 或推理接入点 (Endpoint ID)。
     */
    default @Nullable String getModel() {
        return readString("model");
    }

    /**
     * 必选
     *
     * @param model 本次请求使用模型的 Model ID 或推理接入点 (Endpoint ID)。
     * @see <a href="https://www.volcengine.com/docs/82379/1513689">Model ID</a>
     * @see <a href="https://www.volcengine.com/docs/82379/1099522">推理接入点 (Endpoint ID)</a>
     */
    default R setModel(String model) {
        ensureEntry("model", model);
        return getImplementation();
    }

    /**
     * 用于生成图像的提示词，支持中英文。
     * <p>建议不超过300个汉字或600个英文单词。字数过多信息容易分散，模型可能因此忽略细节，只关注重点，造成图片缺失部分元素。
     *
     * @return 用于生成图像的提示词，支持中英文。
     */
    default @Nullable String getPrompt() {
        return readString("prompt");
    }

    /**
     *
     * @param prompt 用于生成图像的提示词，支持中英文。
     *               建议不超过300个汉字或600个英文单词。字数过多信息容易分散，模型可能因此忽略细节，只关注重点，造成图片缺失部分元素。
     * @see <a href="https://www.volcengine.com/docs/82379/1829186">Seedream 4.0 提示词指南</a>
     * @see <a href="https://www.volcengine.com/docs/82379/1795150">Seedream 3.0 提示词指南</a>
     */
    default R setPrompt(String prompt) {
        ensureEntry("prompt", prompt);
        return getImplementation();
    }

    /**
     *
     * @return 输入的图片信息
     */
    default @Nullable String getImageSingle() {
        return readString("image");
    }

    /**
     * 仅 doubao-seedream-4.0、doubao-seededit-3.0-i2i 支持该参数。
     * <p>
     * 输入的图片信息，支持 URL 或 Base64 编码。
     * 其中，doubao-seedream-4.0 支持单图或多图输入（查看多图融合示例），doubao-seededit-3.0-i2 仅支持单图输入。
     * <p>
     * 图片URL：请确保图片URL可被访问。
     * <p>
     * Base64编码：请遵循此格式{@code data:image/<图片格式>;base64,<Base64编码>}。
     * 注意 图片格式 需小写，如 {@code data:image/png;base64,<base64_image>}。
     * <p>
     * 说明<br>
     * 传入图片需要满足以下条件：<br>
     * 图片格式：jpeg、png <br>
     * 宽高比（宽/高）范围：[1/3, 3] <br>
     * 宽高长度（px） > 14 <br>
     * 大小：不超过 10MB <br>
     * 总像素：不超过 6000×6000 px <br>
     * doubao-seedream-4.0 最多支持传入 10 张参考图。
     */
    default R setImage(String imageSingle) {
        ensureEntry("image", imageSingle);
        return getImplementation();
    }


    /**
     *
     * @return 指定生成图像的尺寸信息
     */
    default @Nullable String getSize() {
        return readString("size");
    }

    /**
     * 指定生成图像的尺寸信息，支持以下两种方式，不可混用。
     * <p>
     * 方式1：指定生成图像的分辨率，并在prompt中用自然语言描述图片宽高比、图片形状或图片用途，最终由模型判断生成图片的大小。<br>
     * 可选值：{@code 1K}、{@code 2K}、{@code 4K}
     * <p>
     * 方式2：指定生成图像的宽高像素值：<br>
     * 默认值：{@code 2048x2048}<br>
     * 总像素取值范围：{@code [1280x720, 4096x4096] }<br>
     * 宽高比取值范围：{@code [1/16, 16]}
     *
     * @see <a
     *         href="https://www.volcengine.com/docs/82379/1824121#%E6%8C%87%E5%AE%9A%E5%9B%BE%E5%83%8F%E5%88%86%E8%BE%A8%E7%8E%87">示例</a>
     */
    default R setSize(String size) {
        ensureEntry("size", size);
        return getImplementation();
    }

    /**
     * 指定生成图像的返回格式。
     *
     * @return 生成的图片为 jpeg 格式，支持以下两种返回方式：<br>
     *         {@code url}：返回图片下载链接；链接在图片生成后24小时内有效，请及时下载图片。
     *         {@code b64_json}：以 Base64 编码字符串的 JSON 格式返回图像数据。
     */
    default @Nullable String getResponseFormat() {
        return readString("response_format");
    }

    /**
     * 指定生成图像的返回格式。
     *
     * @param responseFormat 生成的图片为 jpeg 格式，支持以下两种返回方式：<br>
     *                       {@code url}：返回图片下载链接；链接在图片生成后24小时内有效，请及时下载图片。
     *                       {@code b64_json}：以 Base64 编码字符串的 JSON 格式返回图像数据。
     */
    default R setResponseFormat(String responseFormat) {
        ensureEntry("response_format", responseFormat);
        return getImplementation();
    }

    /**
     * 是否在生成的图片中添加水印。
     *
     * @return {@code false}：不添加水印。
     *         {@code true}：在图片右下角添加“AI生成”字样的水印标识。
     */
    default @Nullable Boolean getWatermark() {
        return readBoolean("watermark");
    }

    /**
     * 是否在生成的图片中添加水印。
     *
     * @param watermark {@code false}：不添加水印。
     *                  {@code true}：在图片右下角添加“AI生成”字样的水印标识。
     */
    default R setWatermark(Boolean watermark) {
        ensureEntry("watermark", watermark);
        return getImplementation();
    }
}
