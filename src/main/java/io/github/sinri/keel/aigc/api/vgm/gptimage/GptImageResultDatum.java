package io.github.sinri.keel.aigc.api.vgm.gptimage;

import io.github.sinri.keel.base.json.UnmodifiableJsonifiableEntityImpl;
import io.github.sinri.keel.core.utils.StringUtils;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;


/**
 * @since 1.3.3
 */
public class GptImageResultDatum extends UnmodifiableJsonifiableEntityImpl {

    public GptImageResultDatum(JsonObject jsonObject) {
        super(jsonObject);
    }

    public String getB64Json() {
        return readString("b64_json");
    }

    public Buffer transformToBuffer() {
        return Buffer.buffer(StringUtils.decodeWithBase64ToBytes(getB64Json()));
    }
}
