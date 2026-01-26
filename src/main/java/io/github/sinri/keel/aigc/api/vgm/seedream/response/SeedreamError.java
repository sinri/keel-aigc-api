package io.github.sinri.keel.aigc.api.vgm.seedream.response;

import io.github.sinri.keel.aigc.api.internal.vgm.seedream.SeedreamErrorImpl;
import io.github.sinri.keel.base.json.UnmodifiableJsonifiableEntity;
import io.vertx.core.json.JsonObject;
import org.jspecify.annotations.Nullable;

/**
 * 错误信息结构体。
 *
 * @since 5.0.0
 */
public interface SeedreamError extends UnmodifiableJsonifiableEntity {
    static SeedreamError wrap(JsonObject jsonObject) {
        return new SeedreamErrorImpl(jsonObject);
    }

    /**
     *
     * @return 某张图片生成错误的错误码，请参见错误码。
     * @see <a href="https://www.volcengine.com/docs/82379/1299023">错误码</a>
     */
    default @Nullable String getCode() {
        return readString("code");
    }

    /**
     *
     * @return 某张图片生成错误的提示信息。
     */
    default @Nullable String getMessage() {
        return readString("message");
    }
}
