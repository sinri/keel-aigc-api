package io.github.sinri.keel.aigc.api.vgm.seedream.response;

import io.github.sinri.keel.base.json.UnmodifiableJsonifiableEntity;
import io.vertx.core.json.JsonObject;
import org.jspecify.annotations.Nullable;

/**
 * @since 5.0.0
 */
public sealed interface SeedreamEvent extends UnmodifiableJsonifiableEntity
        permits SeedreamEventCompleted, SeedreamEventPartialFailed, SeedreamEventPartialSucceeded {

    default @Nullable Long getTimestamp() {
        return readLong("timestamp");
    }

    default @Nullable String getEvent() {
        return readString("event");
    }

    default @Nullable JsonObject getData() {
        return readJsonObject("data");
    }

    default @Nullable String getType() {
        return readString("data", "type");
    }

    /**
     *
     * @return 本次请求使用的模型 ID ，格式为 模型名称-版本。
     */
    default @Nullable String getModel() {
        return readString("data", "model");
    }

    /**
     *
     * @return 本次请求创建时间的 Unix 时间戳（秒）。
     */
    default @Nullable Integer getCreated() {
        return readInteger("data", "created");
    }
}
