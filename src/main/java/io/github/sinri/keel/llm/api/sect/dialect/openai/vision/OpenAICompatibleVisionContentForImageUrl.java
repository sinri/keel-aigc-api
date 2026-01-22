package io.github.sinri.keel.llm.api.sect.dialect.openai.vision;

import io.github.sinri.keel.base.annotations.SelfInterface;
import io.github.sinri.keel.base.json.JsonifiableDataUnit;

/**
 * OpenAICompatibleVisionContentForImageUrl 接口用于描述兼容 OpenAI Vision API 的图片内容实体，
 * 主要用于封装图片的 URL 或 Base64 编码信息，以便在多种视觉模型（如 OpenAI、火山引擎等）中统一处理图片输入。
 * <p>
 * 该接口继承自 JsonifiableEntity，支持 JSON 序列化与反序列化。
 * <ul>
 *   <li>通过 {@link #getUrl()} 获取图片的 URL 或 Base64 字符串。</li>
 *   <li>通过 {@link #setUrl(String)} 设置图片的 URL 或 Base64 字符串。</li>
 * </ul>
 * <p>
 * 适用场景：
 * <ul>
 *   <li>需要将图片以 URL 或 Base64 格式传递给兼容 OpenAI Vision API 的服务时。</li>
 *   <li>需要统一处理不同视觉模型图片输入参数时。</li>
 * </ul>
 *
 * @param <C> 实现类自身类型，用于链式调用
 */
public interface OpenAICompatibleVisionContentForImageUrl<C> extends JsonifiableDataUnit, SelfInterface<C> {
    default String getUrl() {
        return readString("url");
    }

    /**
     * @param url 支持传入图片链接或图片的Base64编码，不同模型支持图片大小略有不同，具体请参见使用说明。<br>
     *            传入图片URL：传入图片的可访问链接，推荐使用 TOS（火山引擎对象存储） 存储图片，并生成图片链接。<br>
     *            传入Base64编码：请遵循格式 {@code data:image/[图片格式];base64,[Base64编码]}，可见示例。<br>
     * @see <a href="https://www.volcengine.com/docs/82379/1362931#%E4%BD%BF%E7%94%A8%E8%AF%B4%E6%98%8E">使用说明</a>
     * @see <a
     *         href="https://www.volcengine.com/docs/82379/1362931#base64-%E7%BC%96%E7%A0%81%E8%BE%93%E5%85%A5">示例</a>
     */
    default C setUrl(String url) {
        ensureEntry("url", url);
        return getImplementation();
    }

}
