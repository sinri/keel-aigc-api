package io.github.sinri.keel.aigc.api.vgm.gptimage;

import io.github.sinri.keel.base.json.UnmodifiableJsonifiableEntityImpl;
import io.github.sinri.keel.core.utils.StringUtils;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import org.jspecify.annotations.Nullable;


/**
 * @since 1.3.3
 */
public class GptImageResultDatum extends UnmodifiableJsonifiableEntityImpl {

    public GptImageResultDatum(JsonObject jsonObject) {
        super(jsonObject);
    }

    public @Nullable String getB64Json() {
        return readString("b64_json");
    }

    public @Nullable Buffer transformToBuffer() {
        String b64Json = getB64Json();
        if (b64Json == null) return null;
        byte[] bytes = StringUtils.decodeWithBase64ToBytes(b64Json);
        return Buffer.buffer(bytes);
    }
}
