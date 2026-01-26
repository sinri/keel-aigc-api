package io.github.sinri.keel.aigc.api.llm.sect.dialect.openai.classic.core;


import io.github.sinri.keel.base.annotations.SelfInterface;
import io.github.sinri.keel.base.json.JsonifiableDataUnit;

/**
 * 本接口定义了与 OpenAI 视觉模型兼容的内容结构。
 * 旨在表示符合 OpenAI API 规范的 text 和 image_url 两种类型的内容。
 * <p>
 * 典型用法为：实现该接口的内容对象可序列化为 JSON，并发送至 OpenAI 兼容的视觉端点。
 * 接口提供了 type、text、image_url 字段的默认处理方法，以及通用的 JSON 序列化机制。
 * <p>
 * <b>类型参数说明：</b>
 * <ul>
 *   <li><b>C</b> - 内容对象的具体类型，用于链式调用（fluent API）。</li>
 *   <li><b>P</b> - 表示 image_url 内容的类型，必须实现 {@link OpenAICompatibleVisionContentForImageUrl}。</li>
 * </ul>
 * <p>
 * <b>典型用法示例：</b>
 * <pre>{@code
 * // 文本内容：
 * content.setText("请描述这张图片");
 * // 图片内容：
 * content.setImageUrl(imageUrlContent);}
 * </pre>
 * <p>
 * <b>注意事项：</b>
 * <ul>
 *   <li>实现类需确保 type、text、image_url 字段的正确序列化。</li>
 *   <li>getImageUrl() 方法应返回封装后的 image_url 内容对象，如不适用可返回 null。</li>
 * </ul>
 *
 * @param <C> 内容对象的具体类型（用于链式 API）。
 * @param <P> image_url 内容的类型，需继承 OpenAICompatibleVisionContentForImageUrl。
 * @since 2.0.0
 */
public interface OpenAICompatibleVisionContent<C, P extends OpenAICompatibleVisionContentForImageUrl<P>> extends JsonifiableDataUnit, SelfInterface<C> {
    default String getType() {
        return readString("type");
    }

    default C setType(String type) {
        ensureEntry("type", type);
        return getImplementation();
    }

    default String getText() {
        return readString("text");
    }

    /**
     * For type {@code text}.
     */
    default C setText(String text) {
        setType("text");
        ensureEntry("text", text);
        return getImplementation();
    }


    /**
     * Sample code:
     * {@code return P.wrap(Objects.requireNonNull(readJsonObject("image_url")));}
     *
     * @return 图片消息的内容部分。
     */
    P getImageUrl();

    /**
     * For type {@code image_url}.
     */
    default <E> C setImageUrl(P imageUrl) {
        setType("image_url");
        ensureEntry("image_url", imageUrl.toJsonObject());
        return getImplementation();
    }
}
