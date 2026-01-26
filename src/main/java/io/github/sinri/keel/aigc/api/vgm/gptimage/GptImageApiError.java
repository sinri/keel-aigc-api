package io.github.sinri.keel.aigc.api.vgm.gptimage;

import io.github.sinri.keel.base.json.UnmodifiableJsonifiableEntityImpl;
import io.vertx.core.json.JsonObject;


/**
 * @since 1.3.3
 */
public class GptImageApiError extends UnmodifiableJsonifiableEntityImpl {

    public GptImageApiError(JsonObject jsonObject) {
        super(jsonObject);
    }

    public String getMessage() {
        return readString("message");
    }

    public String getType() {
        return readString("type");
    }

    public Object getParam() {
        return readValue("param");
    }

    public String getCode() {
        return readString("code");
    }
}
