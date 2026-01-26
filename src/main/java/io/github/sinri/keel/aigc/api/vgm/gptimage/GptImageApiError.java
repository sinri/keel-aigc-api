package io.github.sinri.keel.aigc.api.vgm.gptimage;

import io.github.sinri.keel.base.json.UnmodifiableJsonifiableEntityImpl;
import io.vertx.core.json.JsonObject;
import org.jspecify.annotations.Nullable;


/**
 * @since 1.3.3
 */
public class GptImageApiError extends UnmodifiableJsonifiableEntityImpl {

    public GptImageApiError(JsonObject jsonObject) {
        super(jsonObject);
    }

    public @Nullable String getMessage() {
        return readString("message");
    }

    public @Nullable String getType() {
        return readString("type");
    }

    public @Nullable Object getParam() {
        return readValue("param");
    }

    public @Nullable String getCode() {
        return readString("code");
    }
}
