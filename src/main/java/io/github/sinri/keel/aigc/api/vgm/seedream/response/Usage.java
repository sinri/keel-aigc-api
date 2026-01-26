package io.github.sinri.keel.aigc.api.vgm.seedream.response;

import io.github.sinri.keel.aigc.api.internal.vgm.seedream.UsageImpl;
import io.github.sinri.keel.base.json.UnmodifiableJsonifiableEntity;
import io.vertx.core.json.JsonObject;
import org.jspecify.annotations.Nullable;

/**
 * @since 5.0.0
 */
public interface Usage extends UnmodifiableJsonifiableEntity {
    static Usage wrap(JsonObject jsonObject) {
        return new UsageImpl(jsonObject);
    }

    /**
     *
     * @return 模型成功生成的图片张数，不包含生成失败的图片。
     *         仅对成功生成图片按张数进行计费。
     */
    default @Nullable Integer getGeneratedImages() {
        return readInteger("generated_images");
    }

    /**
     *
     * @return 模型生成的图片花费的 token 数量。
     *         计算逻辑为：计算sum(图片长*图片宽)/256 ，然后取整。
     */
    default @Nullable Integer getOutputTokens() {
        return readInteger("output_tokens");
    }

    /**
     *
     * @return 本次请求消耗的总 token 数量。
     *         当前不计算输入 token，故与 output_tokens 值一致。
     */
    default @Nullable Integer getTotalTokens() {
        return readInteger("total_tokens");
    }
}
