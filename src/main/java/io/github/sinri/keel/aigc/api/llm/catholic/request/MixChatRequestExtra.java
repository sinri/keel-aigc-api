package io.github.sinri.keel.aigc.api.llm.catholic.request;

import io.github.sinri.keel.base.json.JsonifiableDataUnit;
import io.github.sinri.keel.aigc.api.internal.llm.catholic.request.MixChatRequestExtraImpl;
import io.vertx.core.json.JsonObject;
import org.jspecify.annotations.Nullable;

/**
 * 混合聊天请求的额外参数接口。
 * <p>
 * 用于设置聊天请求的额外参数，如温度、随机种子和响应格式等。
 *
 * @since 5.0.0
 */
public interface MixChatRequestExtra extends JsonifiableDataUnit {
    /**
     * 创建新的额外参数实例。
     *
     * @return 额外参数实例
     */
    static MixChatRequestExtra create() {
        return new MixChatRequestExtraImpl();
    }

    /**
     * 从 JSON 对象包装为额外参数实例。
     *
     * @param jsonObject JSON 对象
     * @return 额外参数实例
     */
    static MixChatRequestExtra wrap(JsonObject jsonObject) {
        return new MixChatRequestExtraImpl(jsonObject);
    }

    /**
     * 获取温度参数。
     *
     * @return 温度值，如果未设置则返回 null
     */
    default @Nullable Float getTemperature() {
        return readFloat("temperature");
    }

    /**
     * 设置温度参数。
     * <p>
     * 温度控制输出的随机性，值越高输出越随机。
     *
     * @param temperature 温度值
     * @return 当前实例，用于链式调用
     */
    default MixChatRequestExtra setTemperature(float temperature) {
        ensureEntry("temperature", temperature);
        return this;
    }

    /**
     * 获取随机种子。
     *
     * @return 随机种子值，如果未设置则返回 null
     */
    default @Nullable Integer getSeed() {
        return readInteger("seed");
    }

    /**
     * 设置随机种子。
     * <p>
     * 设置随机种子可以确保相同输入产生相同输出。
     *
     * @param seed 随机种子值
     * @return 当前实例，用于链式调用
     */
    default MixChatRequestExtra setSeed(Integer seed) {
        ensureEntry("seed", seed);
        return this;
    }

    /**
     * 获取响应格式。
     *
     * @return 响应格式对象，如果未设置则返回 null
     */
    default @Nullable JsonObject getResponseFormat() {
        return readJsonObject("response_format");
    }

    /**
     * 设置响应格式。
     * <p>
     * 让此聊天模型请求的响应格式为有序的纯文本、JSON 对象，甚至遵循 JSON Schema。
     * 其效果因不同的聊天模型而异。
     *
     * @param responseFormat 响应格式对象
     * @return 当前实例，用于链式调用
     */
    default MixChatRequestExtra setResponseFormat(JsonObject responseFormat) {
        ensureEntry("response_format", responseFormat);
        return this;
    }
}
